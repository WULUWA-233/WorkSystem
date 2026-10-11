package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.Result;
import com.crm.dto.*;
import com.crm.service.CrmCustomerService;
import com.crm.vo.CustomerDetailVO;
import com.crm.vo.CustomerVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Map;

/**
 * Customer Management Controller
 * Requires ADMIN or SALES role (enforced by CustomerPermissionInterceptor).
 *
 * @author CRM Team
 */
@Api(tags = "Customer Management")
@RestController
@RequestMapping("/api/v1/customer/customers")
public class CustomerController {

    private final CrmCustomerService customerService;

    public CustomerController(CrmCustomerService customerService) {
        this.customerService = customerService;
    }

    @ApiOperation("Customer Pagination Query")
    @GetMapping
    public Result<PageResult<CustomerVO>> listCustomers(CustomerQueryDTO query) {
        return Result.success(customerService.listCustomers(query));
    }

    @ApiOperation("Customer Detail")
    @GetMapping("/{id}")
    public Result<CustomerDetailVO> getCustomerDetail(@PathVariable Long id) {
        return Result.success(customerService.getCustomerDetail(id));
    }

    @OperationLog(module = "Customer Management", type = "Create", description = "Create customer")
    @ApiOperation("Create Customer")
    @PostMapping
    public Result<Void> createCustomer(@Valid @RequestBody CustomerCreateDTO dto) {
        customerService.createCustomer(dto);
        return Result.success();
    }

    @OperationLog(module = "Customer Management", type = "Update", description = "Update customer")
    @ApiOperation("Update Customer")
    @PutMapping("/{id}")
    public Result<Void> updateCustomer(@PathVariable Long id, @Valid @RequestBody CustomerUpdateDTO dto) {
        customerService.updateCustomer(id, dto);
        return Result.success();
    }

    @OperationLog(module = "Customer Management", type = "Delete", description = "Delete customer")
    @ApiOperation("Delete Customer (Logic Delete)")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return Result.success();
    }

    @OperationLog(module = "Customer Management", type = "Status Change", description = "Update customer status")
    @ApiOperation("Update Customer Status")
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody CustomerStatusDTO dto) {
        customerService.updateStatus(id, dto);
        return Result.success();
    }

    @ApiOperation("Check Customer Name Exists")
    @GetMapping("/check-name")
    public Result<Map<String, Boolean>> checkName(@RequestParam String customerName,
                                                   @RequestParam(required = false) Long excludeId) {
        return Result.success(customerService.checkName(customerName, excludeId));
    }
}
