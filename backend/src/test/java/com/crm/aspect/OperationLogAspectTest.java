package com.crm.aspect;

import com.crm.annotation.OperationLog;
import com.crm.entity.SysOperationLog;
import com.crm.mapper.SysOperationLogMapper;
import com.crm.utils.LoginUserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OperationLogAspectTest {
    @AfterEach void clearRequest() { RequestContextHolder.resetRequestAttributes(); }

    @Test void publicLoginCanProceedWithoutAnAuthenticatedUser() throws Throwable {
        LoginUserContext.clear();
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        Object result = Collections.singletonMap("success", true);
        OperationLogAspect aspect = new OperationLogAspect(mapper, new ObjectMapper());
        assertSame(result, aspect.around(point(Collections.emptyMap(), result)));
        ArgumentCaptor<SysOperationLog> captured = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(mapper).insert(captured.capture());
        assertNull(captured.getValue().getUserId());
        assertEquals(1, captured.getValue().getStatus());
    }

    @Test void recordsJsonBodyAndRedactsNestedSecretsAndQueryArrays() throws Throwable {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        ObjectMapper json = new ObjectMapper().registerModule(new JavaTimeModule());
        OperationLogAspect aspect = new OperationLogAspect(mapper, json);
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/test");
        request.addParameter("PASSWORD", "query-secret");
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("customerName", "测试客户");
        body.put("nested", Collections.singletonMap("new_password", "body-secret"));
        Object result = Collections.singletonMap("data", Collections.singletonMap("accessToken", "jwt-secret"));
        ProceedingJoinPoint point = point(body, result);
        assertSame(result, aspect.around(point));
        ArgumentCaptor<SysOperationLog> captured = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(mapper).insert(captured.capture());
        String params = captured.getValue().getRequestParams();
        assertTrue(params.contains("测试客户"));
        assertFalse(params.contains("query-secret"));
        assertFalse(params.contains("body-secret"));
        assertTrue(params.contains("******"));
        assertFalse(captured.getValue().getResponseResult().contains("jwt-secret"));
    }

    @Test void configuredMapperSupportsBusinessResponsesWithDates() throws Throwable {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        OperationLogAspect aspect = new OperationLogAspect(mapper, new ObjectMapper().registerModule(new JavaTimeModule()));
        Object result = Collections.singletonMap("createdAt", LocalDateTime.of(2024, 2, 29, 12, 0));
        assertSame(result, aspect.around(point(Collections.emptyMap(), result)));
        ArgumentCaptor<SysOperationLog> captured = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(mapper).insert(captured.capture());
        assertEquals(1, captured.getValue().getStatus());
        assertNotNull(captured.getValue().getResponseResult());
    }

    @Test void serializationFailureDoesNotTurnSuccessfulOperationIntoFailure() throws Throwable {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        ObjectMapper broken = mock(ObjectMapper.class);
        when(broken.valueToTree(any())).thenThrow(new IllegalArgumentException("unsupported value"));
        OperationLogAspect aspect = new OperationLogAspect(mapper, broken);
        Object result = new Object();
        assertSame(result, aspect.around(point(Collections.emptyMap(), result)));
        ArgumentCaptor<SysOperationLog> captured = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(mapper).insert(captured.capture());
        assertEquals(1, captured.getValue().getStatus());
    }

    @Test void recordsAndPropagatesOriginalBusinessFailure() throws Throwable {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        OperationLogAspect aspect = new OperationLogAspect(mapper, new ObjectMapper());
        ProceedingJoinPoint point = point(Collections.emptyMap(), null);
        IllegalStateException failure = new IllegalStateException("business failed");
        when(point.proceed()).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> aspect.around(point)));
        ArgumentCaptor<SysOperationLog> captured = ArgumentCaptor.forClass(SysOperationLog.class);
        verify(mapper).insert(captured.capture());
        assertEquals(0, captured.getValue().getStatus());
    }

    @Test void logStorageFailureDoesNotRepeatOrFailBusinessOperation() throws Throwable {
        SysOperationLogMapper mapper = mock(SysOperationLogMapper.class);
        when(mapper.insert(any())).thenThrow(new IllegalStateException("database unavailable"));
        OperationLogAspect aspect = new OperationLogAspect(mapper, new ObjectMapper());
        Object result = Collections.singletonMap("ok", true);
        ProceedingJoinPoint point = point(Collections.emptyMap(), result);
        assertSame(result, aspect.around(point));
        verify(point, times(1)).proceed();
    }

    private ProceedingJoinPoint point(Object body, Object result) throws Throwable {
        ProceedingJoinPoint point = mock(ProceedingJoinPoint.class);
        MethodSignature signature = mock(MethodSignature.class);
        Method method = LoggedController.class.getMethod("save", Map.class);
        when(signature.getMethod()).thenReturn(method);
        when(point.getSignature()).thenReturn(signature);
        when(point.getArgs()).thenReturn(new Object[]{body});
        when(point.proceed()).thenReturn(result);
        return point;
    }

    public static class LoggedController {
        @OperationLog(module = "Test", type = "Create", description = "Save")
        public Object save(Map<String, Object> body) { return body; }
    }
}

