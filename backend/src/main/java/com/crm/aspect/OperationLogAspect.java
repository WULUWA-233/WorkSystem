package com.crm.aspect;

import com.crm.annotation.OperationLog;
import com.crm.common.BusinessException;
import com.crm.entity.SysOperationLog;
import com.crm.mapper.SysOperationLogMapper;
import com.crm.utils.LoginUserContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindingResult;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.security.Principal;
import java.util.*;

@Aspect
@Component
public class OperationLogAspect {

    private static final Logger log = LoggerFactory.getLogger(OperationLogAspect.class);
    private static final Set<String> SENSITIVE_KEYS = new HashSet<>(Arrays.asList(
            "password", "oldpassword", "newpassword", "confirmpassword", "passwordhash",
            "token", "accesstoken", "refreshtoken", "authorization", "secret", "apikey", "clientsecret"));
    private static final int MAX_PARAMS = 2000;
    private static final int MAX_RESULT = 4000;
    private static final int MAX_ERROR = 1000;
    private static final int MAX_AGENT = 500;

    private final SysOperationLogMapper logMapper;
    private final ObjectMapper objectMapper;

    public OperationLogAspect(SysOperationLogMapper logMapper, ObjectMapper objectMapper) {
        this.logMapper = logMapper;
        this.objectMapper = objectMapper;
    }

    @Around("@annotation(com.crm.annotation.OperationLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        OperationLog ann = method.getAnnotation(OperationLog.class);
        SysOperationLog opLog = new SysOperationLog();
        opLog.setModuleName(ann.module());
        opLog.setOperationType(ann.type());
        opLog.setOperationDesc(ann.description());
        opLog.setStatus(1);
        try {
            opLog.setUserId(LoginUserContext.getCurrentUserId());
            opLog.setUsername(LoginUserContext.getCurrentUsername());
        } catch (RuntimeException e) {
            // Login is public and has no authenticated user context yet.
            opLog.setUserId(null);
        }

        // Logging must never change the outcome of the business operation.
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            HttpServletRequest req = attrs == null ? null : attrs.getRequest();
            if (req != null) {
                opLog.setRequestMethod(req.getMethod());
                opLog.setRequestUrl(req.getRequestURI());
                opLog.setIpAddress(getClientIp(req));
                opLog.setUserAgent(truncate(req.getHeader("User-Agent"), MAX_AGENT));
            }
            opLog.setRequestParams(truncate(toSanitizedJson(requestParams(req, method, joinPoint.getArgs())), MAX_PARAMS));
        } catch (Exception e) {
            log.warn("Could not serialize operation request: {}", e.getClass().getSimpleName());
        }

        Object result;
        try {
            result = joinPoint.proceed();
        } catch (Throwable e) {
            opLog.setStatus(0);
            opLog.setCostTime(System.currentTimeMillis() - start);
            opLog.setErrorMessage(truncate(e instanceof BusinessException
                    ? e.getMessage() : e.getClass().getSimpleName() + ": operation failed", MAX_ERROR));
            trySave(opLog);
            throw e;
        }

        opLog.setCostTime(System.currentTimeMillis() - start);
        try {
            if (result != null) opLog.setResponseResult(truncate(toSanitizedJson(result), MAX_RESULT));
        } catch (Exception e) {
            log.warn("Could not serialize operation response: {}", e.getClass().getSimpleName());
        }
        trySave(opLog);
        return result;
    }

    private Map<String, Object> requestParams(HttpServletRequest req, Method method, Object[] args) {
        Map<String, Object> params = new LinkedHashMap<>();
        if (req != null && !req.getParameterMap().isEmpty()) params.put("query", req.getParameterMap());
        Parameter[] parameters = method.getParameters();
        if (args != null) {
            for (int i = 0; i < args.length; i++) {
                Object value = args[i];
                if (value instanceof ServletRequest || value instanceof ServletResponse || value instanceof BindingResult
                        || value instanceof MultipartFile || value instanceof MultipartFile[] || value instanceof Principal
                        || value instanceof InputStream || value instanceof OutputStream) continue;
                String name = i < parameters.length && parameters[i].isNamePresent() ? parameters[i].getName() : "arg" + i;
                params.put(name, value);
            }
        }
        return params;
    }

    private String toSanitizedJson(Object value) throws Exception {
        JsonNode tree = objectMapper.valueToTree(value);
        redact(tree);
        return objectMapper.writeValueAsString(tree);
    }

    private void redact(JsonNode node) {
        if (node.isObject()) {
            ObjectNode object = (ObjectNode) node;
            List<String> names = new ArrayList<>();
            object.fieldNames().forEachRemaining(names::add);
            for (String name : names) {
                String key = name.replace("_", "").replace("-", "").toLowerCase(Locale.ROOT);
                if (SENSITIVE_KEYS.contains(key)) object.put(name, "******");
                else redact(object.get(name));
            }
        } else if (node.isArray()) {
            node.forEach(this::redact);
        }
    }

    private void trySave(SysOperationLog opLog) {
        try {
            logMapper.insert(opLog);
        } catch (Exception e) {
            log.warn("Failed to save operation log: {}", e.getClass().getSimpleName());
        }
    }

    private String getClientIp(HttpServletRequest req) {
        String ip = req.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = req.getHeader("X-Real-IP");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) ip = req.getRemoteAddr();
        if (ip != null && ip.contains(",")) ip = ip.split(",")[0].trim();
        return ip;
    }

    private String truncate(String value, int max) {
        if (value == null || value.length() <= max) return value;
        return value.substring(0, max - 3) + "...";
    }
}
