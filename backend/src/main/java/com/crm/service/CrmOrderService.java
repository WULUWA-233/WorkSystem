package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.*;
import com.crm.vo.OrderDetailVO;
import com.crm.vo.OrderVO;

public interface CrmOrderService {
    PageResult<OrderVO> listOrders(OrderQueryDTO query);
    OrderDetailVO getOrderDetail(Long id);
    void createOrder(OrderCreateDTO dto);
    void updateOrder(Long id, OrderUpdateDTO dto);
    void deleteOrder(Long id);
    void updateStatus(Long id, OrderStatusDTO dto);
    void pay(Long id, OrderPayDTO dto);
    void ship(Long id, OrderShipDTO dto);
    void complete(Long id, OrderCancelDTO dto);
    void cancel(Long id, OrderCancelDTO dto);
    PageResult<OrderVO> listByCustomer(Long customerId, long page, long size);
    PageResult<OrderVO> listByOpportunity(Long opportunityId, long page, long size);
}
