package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.dto.*;
import com.crm.service.CrmOpportunityService;
import com.crm.vo.OpportunityDetailVO;
import com.crm.vo.OpportunityVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@Api(tags = "Opportunity Management")
@RestController
public class OpportunityController {

    private final CrmOpportunityService opportunityService;

    public OpportunityController(CrmOpportunityService opportunityService) {
        this.opportunityService = opportunityService;
    }

    // OPP-001
    @ApiOperation("Opportunity Pagination Query")
    @GetMapping("/api/v1/sales/opportunities")
    public Result<PageResult<OpportunityVO>> listOpportunities(OpportunityQueryDTO query) {
        return Result.success(opportunityService.listOpportunities(query));
    }

    // OPP-002
    @ApiOperation("Opportunity Detail")
    @GetMapping("/api/v1/sales/opportunities/{id}")
    public Result<OpportunityDetailVO> getDetail(@PathVariable Long id) {
        return Result.success(opportunityService.getOpportunityDetail(id));
    }

    // OPP-003
    @OperationLog(module = "Opportunity Management", type = "Create", description = "Create opportunity")
    @ApiOperation("Create Opportunity")
    @PostMapping("/api/v1/sales/opportunities")
    public Result<Void> create(@Valid @RequestBody OpportunityCreateDTO dto) {
        opportunityService.createOpportunity(dto);
        return Result.success();
    }

    // OPP-004
    @OperationLog(module = "Opportunity Management", type = "Update", description = "Update opportunity")
    @ApiOperation("Update Opportunity")
    @PutMapping("/api/v1/sales/opportunities/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OpportunityUpdateDTO dto) {
        opportunityService.updateOpportunity(id, dto);
        return Result.success();
    }

    // OPP-005
    @OperationLog(module = "Opportunity Management", type = "Delete", description = "Delete opportunity")
    @ApiOperation("Delete Opportunity")
    @DeleteMapping("/api/v1/sales/opportunities/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        opportunityService.deleteOpportunity(id);
        return Result.success();
    }

    // OPP-006
    @OperationLog(module = "Opportunity Management", type = "Stage Advance", description = "Advance opportunity stage")
    @ApiOperation("Update Opportunity Stage")
    @PatchMapping("/api/v1/sales/opportunities/{id}/stage")
    public Result<Void> updateStage(@PathVariable Long id, @Valid @RequestBody OpportunityStageDTO dto) {
        opportunityService.updateStage(id, dto);
        return Result.success();
    }

    // OPP-007
    @OperationLog(module = "Opportunity Management", type = "Win", description = "Mark opportunity as won")
    @ApiOperation("Mark Opportunity as Won")
    @PatchMapping("/api/v1/sales/opportunities/{id}/win")
    public Result<Void> markAsWin(@PathVariable Long id, @RequestBody(required = false) OpportunityLoseDTO dto) {
        opportunityService.markAsWin(id, dto);
        return Result.success();
    }

    // OPP-008
    @OperationLog(module = "Opportunity Management", type = "Lose", description = "Mark opportunity as lost")
    @ApiOperation("Mark Opportunity as Lost")
    @PatchMapping("/api/v1/sales/opportunities/{id}/lose")
    public Result<Void> markAsLose(@PathVariable Long id, @Valid @RequestBody OpportunityLoseDTO dto) {
        opportunityService.markAsLose(id, dto);
        return Result.success();
    }

    // OPP-009
    @ApiOperation("List Opportunities by Customer")
    @GetMapping("/api/v1/customer/customers/{customerId}/opportunities")
    public Result<List<OpportunityVO>> listByCustomer(@PathVariable Long customerId) {
        return Result.success(opportunityService.listByCustomer(customerId));
    }
}
