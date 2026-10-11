package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.OperationLogQueryDTO;
import com.crm.entity.SysOperationLog;
import com.crm.mapper.SysOperationLogMapper;
import com.crm.service.SysOperationLogService;
import com.crm.utils.DateParseUtils;
import com.crm.vo.OperationLogDetailVO;
import com.crm.vo.OperationLogVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SysOperationLogServiceImpl implements SysOperationLogService {

    private final SysOperationLogMapper logMapper;

    public SysOperationLogServiceImpl(SysOperationLogMapper logMapper) { this.logMapper = logMapper; }

    @Override
    public PageResult<OperationLogVO> listLogs(OperationLogQueryDTO query) {
        LambdaQueryWrapper<SysOperationLog> w = new LambdaQueryWrapper<>();
        if (notEmpty(query.getUsername())) w.like(SysOperationLog::getUsername, query.getUsername());
        if (notEmpty(query.getModuleName())) w.like(SysOperationLog::getModuleName, query.getModuleName());
        if (notEmpty(query.getOperationType())) w.like(SysOperationLog::getOperationType, query.getOperationType());
        if (notEmpty(query.getRequestMethod())) w.eq(SysOperationLog::getRequestMethod, query.getRequestMethod().toUpperCase());
        if (query.getStatus() != null) w.eq(SysOperationLog::getStatus, query.getStatus());
        if (notEmpty(query.getStartTime())) {
            w.ge(SysOperationLog::getCreatedAt, DateParseUtils.parseDateTime(query.getStartTime(), "startTime"));
        }
        if (notEmpty(query.getEndTime())) {
            w.le(SysOperationLog::getCreatedAt, DateParseUtils.parseDateTime(query.getEndTime(), "endTime"));
        }
        w.orderByDesc(SysOperationLog::getCreatedAt);

        Page<SysOperationLog> page = new Page<>(query.getPage(), query.getSize());
        Page<SysOperationLog> result = logMapper.selectPage(page, w);
        List<OperationLogVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public OperationLogDetailVO getLogDetail(Long id) {
        SysOperationLog l = getOrThrow(id);
        OperationLogDetailVO vo = new OperationLogDetailVO();
        vo.setId(l.getId()); vo.setUserId(l.getUserId()); vo.setUsername(l.getUsername());
        vo.setModuleName(l.getModuleName()); vo.setOperationType(l.getOperationType());
        vo.setOperationDesc(l.getOperationDesc()); vo.setRequestMethod(l.getRequestMethod());
        vo.setRequestUrl(l.getRequestUrl()); vo.setRequestParams(l.getRequestParams());
        vo.setResponseResult(l.getResponseResult()); vo.setIpAddress(l.getIpAddress());
        vo.setUserAgent(l.getUserAgent()); vo.setCostTime(l.getCostTime());
        vo.setStatus(l.getStatus()); vo.setErrorMessage(l.getErrorMessage());
        vo.setCreatedAt(l.getCreatedAt());
        return vo;
    }

    @Override
    public void deleteLog(Long id) {
        SysOperationLog l = getOrThrow(id);
        logMapper.deleteById(id); // physical delete (no @TableLogic)
    }

    private SysOperationLog getOrThrow(Long id) {
        SysOperationLog l = logMapper.selectById(id);
        if (l == null) throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Operation log not found");
        return l;
    }

    private OperationLogVO toVO(SysOperationLog l) {
        OperationLogVO vo = new OperationLogVO();
        vo.setId(l.getId()); vo.setUserId(l.getUserId()); vo.setUsername(l.getUsername());
        vo.setModuleName(l.getModuleName()); vo.setOperationType(l.getOperationType());
        vo.setOperationDesc(l.getOperationDesc()); vo.setRequestMethod(l.getRequestMethod());
        vo.setRequestUrl(l.getRequestUrl()); vo.setIpAddress(l.getIpAddress());
        vo.setCostTime(l.getCostTime()); vo.setStatus(l.getStatus());
        vo.setErrorMessage(l.getErrorMessage()); vo.setCreatedAt(l.getCreatedAt());
        return vo;
    }

    private boolean notEmpty(String s) { return s != null && !s.isEmpty(); }
}
