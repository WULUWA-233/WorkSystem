package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.PageQuery;
import com.crm.common.Result;
import com.crm.dto.FollowRecordCreateDTO;
import com.crm.dto.FollowRecordQueryDTO;
import com.crm.service.CrmFollowRecordService;
import com.crm.vo.FollowRecordDetailVO;
import com.crm.vo.FollowRecordVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Follow Record Management")
@RestController
public class FollowRecordController {

    private final CrmFollowRecordService followService;

    public FollowRecordController(CrmFollowRecordService followService) {
        this.followService = followService;
    }

    // FOLLOW-001
    @ApiOperation("Follow Record Pagination")
    @GetMapping("/api/v1/customer/follow-records")
    public Result<PageResult<FollowRecordVO>> list(FollowRecordQueryDTO query) {
        return Result.success(followService.listFollowRecords(query));
    }

    // FOLLOW-002
    @OperationLog(module = "Follow Record", type = "Create", description = "Create follow record")
    @ApiOperation("Create Follow Record")
    @PostMapping("/api/v1/customer/follow-records")
    public Result<Void> create(@Valid @RequestBody FollowRecordCreateDTO dto) {
        followService.createFollowRecord(dto);
        return Result.success();
    }

    // FOLLOW-003
    @ApiOperation("Follow Record Detail")
    @GetMapping("/api/v1/customer/follow-records/{id}")
    public Result<FollowRecordDetailVO> detail(@PathVariable Long id) {
        return Result.success(followService.getFollowRecordDetail(id));
    }

    // FOLLOW-004
    @OperationLog(module = "Follow Record", type = "Delete", description = "Delete follow record")
    @ApiOperation("Delete Follow Record")
    @DeleteMapping("/api/v1/customer/follow-records/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        followService.deleteFollowRecord(id);
        return Result.success();
    }

    // FOLLOW-005
    @ApiOperation("Follow Records by Customer")
    @GetMapping("/api/v1/customer/customers/{customerId}/follow-records")
    public Result<PageResult<FollowRecordVO>> listByCustomer(
            @PathVariable Long customerId,
            PageQuery query) {
        return Result.success(followService.listByCustomer(customerId, query.getPage(), query.getSize()));
    }

    // FOLLOW-006
    @ApiOperation("Follow Records by Opportunity")
    @GetMapping("/api/v1/sales/opportunities/{opportunityId}/follow-records")
    public Result<PageResult<FollowRecordVO>> listByOpportunity(
            @PathVariable Long opportunityId,
            PageQuery query) {
        return Result.success(followService.listByOpportunity(opportunityId, query.getPage(), query.getSize()));
    }
}
