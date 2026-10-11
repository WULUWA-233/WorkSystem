package com.crm.controller;

import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.dto.OperationLogQueryDTO;
import com.crm.service.SysOperationLogService;
import com.crm.vo.OperationLogDetailVO;
import com.crm.vo.OperationLogVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Operation Log")
@RestController
@RequestMapping("/api/v1/system/operation-logs")
public class OperationLogController {

    private final SysOperationLogService logService;

    public OperationLogController(SysOperationLogService logService) { this.logService = logService; }

    @ApiOperation("Operation Log Pagination")
    @GetMapping
    public Result<PageResult<OperationLogVO>> list(OperationLogQueryDTO q) { return Result.success(logService.listLogs(q)); }

    @ApiOperation("Operation Log Detail")
    @GetMapping("/{id}")
    public Result<OperationLogDetailVO> detail(@PathVariable Long id) { return Result.success(logService.getLogDetail(id)); }

    @ApiOperation("Delete Operation Log")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) { logService.deleteLog(id); return Result.success(); }
}
