package com.crm.controller;

import com.crm.annotation.OperationLog;
import com.crm.common.PageResult;
import com.crm.common.PageQuery;
import com.crm.common.Result;
import com.crm.dto.*;
import com.crm.service.CrmOrderService;
import com.crm.vo.OrderDetailVO;
import com.crm.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Order Management")
@RestController
public class OrderController {

    private final CrmOrderService orderService;

    public OrderController(CrmOrderService orderService) { this.orderService = orderService; }

    @ApiOperation("Order Pagination")
    @GetMapping("/api/v1/sales/orders")
    public Result<PageResult<OrderVO>> list(OrderQueryDTO q) { return Result.success(orderService.listOrders(q)); }

    @ApiOperation("Order Detail")
    @GetMapping("/api/v1/sales/orders/{id}")
    public Result<OrderDetailVO> detail(@PathVariable Long id) { return Result.success(orderService.getOrderDetail(id)); }

    @OperationLog(module = "Order Management", type = "Create", description = "Create order")
    @ApiOperation("Create Order")
    @PostMapping("/api/v1/sales/orders")
    public Result<Void> create(@Valid @RequestBody OrderCreateDTO dto) { orderService.createOrder(dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Update", description = "Update order")
    @ApiOperation("Update Order")
    @PutMapping("/api/v1/sales/orders/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody OrderUpdateDTO dto) { orderService.updateOrder(id, dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Delete", description = "Delete order")
    @ApiOperation("Delete Order")
    @DeleteMapping("/api/v1/sales/orders/{id}")
    public Result<Void> delete(@PathVariable Long id) { orderService.deleteOrder(id); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Status Change", description = "Update order status")
    @ApiOperation("Update Order Status")
    @PatchMapping("/api/v1/sales/orders/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody OrderStatusDTO dto) { orderService.updateStatus(id, dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Pay", description = "Pay order")
    @ApiOperation("Pay Order")
    @PatchMapping("/api/v1/sales/orders/{id}/pay")
    public Result<Void> pay(@PathVariable Long id, @Valid @RequestBody(required = false) OrderPayDTO dto) { orderService.pay(id, dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Ship", description = "Ship order")
    @ApiOperation("Ship Order")
    @PatchMapping("/api/v1/sales/orders/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @Valid @RequestBody OrderShipDTO dto) { orderService.ship(id, dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Complete", description = "Complete order")
    @ApiOperation("Complete Order")
    @PatchMapping("/api/v1/sales/orders/{id}/complete")
    public Result<Void> complete(@PathVariable Long id, @Valid @RequestBody(required = false) OrderCancelDTO dto) { orderService.complete(id, dto); return Result.success(); }

    @OperationLog(module = "Order Management", type = "Cancel", description = "Cancel order")
    @ApiOperation("Cancel Order")
    @PatchMapping("/api/v1/sales/orders/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id, @Valid @RequestBody(required = false) OrderCancelDTO dto) { orderService.cancel(id, dto); return Result.success(); }

    @ApiOperation("Orders by Customer")
    @GetMapping("/api/v1/customer/customers/{customerId}/orders")
    public Result<PageResult<OrderVO>> byCustomer(@PathVariable Long customerId, PageQuery query) {
        return Result.success(orderService.listByCustomer(customerId, query.getPage(), query.getSize()));
    }

    @ApiOperation("Orders by Opportunity")
    @GetMapping("/api/v1/sales/opportunities/{opportunityId}/orders")
    public Result<PageResult<OrderVO>> byOpportunity(@PathVariable Long opportunityId, PageQuery query) {
        return Result.success(orderService.listByOpportunity(opportunityId, query.getPage(), query.getSize()));
    }
}
