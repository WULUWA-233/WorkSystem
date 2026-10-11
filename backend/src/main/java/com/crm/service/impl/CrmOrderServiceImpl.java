package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.*;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmOpportunity;
import com.crm.entity.CrmOrder;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.CrmOrderMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmOrderService;
import com.crm.utils.DateParseUtils;
import com.crm.utils.LoginUserContext;
import com.crm.vo.OrderDetailVO;
import com.crm.vo.OrderVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Service
public class CrmOrderServiceImpl implements CrmOrderService {

    private static final Logger log = LoggerFactory.getLogger(CrmOrderServiceImpl.class);
    private static final Map<Integer, String> STATUS_NAMES = new LinkedHashMap<>();
    static {
        STATUS_NAMES.put(1, "Pending Payment");
        STATUS_NAMES.put(2, "Paid");
        STATUS_NAMES.put(3, "Shipped");
        STATUS_NAMES.put(4, "Completed");
        STATUS_NAMES.put(5, "Cancelled");
    }

    private final CrmOrderMapper orderMapper;
    private final CrmCustomerMapper customerMapper;
    private final CrmOpportunityMapper opportunityMapper;
    private final SysUserMapper sysUserMapper;

    public CrmOrderServiceImpl(CrmOrderMapper orderMapper, CrmCustomerMapper customerMapper,
                                CrmOpportunityMapper opportunityMapper, SysUserMapper sysUserMapper) {
        this.orderMapper = orderMapper;
        this.customerMapper = customerMapper;
        this.opportunityMapper = opportunityMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<OrderVO> listOrders(OrderQueryDTO query) {
        LambdaQueryWrapper<CrmOrder> w = new LambdaQueryWrapper<>();
        w.eq(CrmOrder::getDeleted, 0);
        if (notEmpty(query.getOrderNo())) w.like(CrmOrder::getOrderNo, query.getOrderNo());
        if (query.getCustomerId() != null) w.eq(CrmOrder::getCustomerId, query.getCustomerId());
        if (query.getOpportunityId() != null) w.eq(CrmOrder::getOpportunityId, query.getOpportunityId());
        if (query.getStatus() != null) w.eq(CrmOrder::getStatus, query.getStatus());
        if (notEmpty(query.getPaymentMethod())) w.eq(CrmOrder::getPaymentMethod, query.getPaymentMethod());
        if (query.getOwnerId() != null) w.eq(CrmOrder::getOwnerId, query.getOwnerId());
        if (query.getMinAmount() != null) w.ge(CrmOrder::getAmount, query.getMinAmount());
        if (query.getMaxAmount() != null) w.le(CrmOrder::getAmount, query.getMaxAmount());
        if (notEmpty(query.getStartTime())) {
            w.ge(CrmOrder::getCreatedAt, DateParseUtils.parseDateTime(query.getStartTime(), "startTime"));
        }
        if (notEmpty(query.getEndTime())) {
            w.le(CrmOrder::getCreatedAt, DateParseUtils.parseDateTime(query.getEndTime(), "endTime"));
        }
        w.orderByDesc(CrmOrder::getId);

        Page<CrmOrder> page = new Page<>(query.getPage(), query.getSize());
        Page<CrmOrder> result = orderMapper.selectPage(page, w);
        List<CrmOrder> orders = result.getRecords();
        Map<Long, String> customerNames = loadCustomerNames(orders.stream()
                .map(CrmOrder::getCustomerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        Map<Long, String> opportunityNames = loadOpportunityNames(orders.stream()
                .map(CrmOrder::getOpportunityId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));
        Map<Long, String> ownerNames = loadUserNames(orders.stream()
                .map(CrmOrder::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<OrderVO> records = orders.stream()
                .map(o -> toVO(o, customerNames, opportunityNames, ownerNames))
                .collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public OrderDetailVO getOrderDetail(Long id) {
        return toDetailVO(getOrThrow(id));
    }

    @Override
    @Transactional
    public void createOrder(OrderCreateDTO dto) {
        // Validate customer
        CrmCustomer customer = customerMapper.selectByIdForUpdate(dto.getCustomerId());
        if (customer == null || Integer.valueOf(1).equals(customer.getDeleted()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Customer not found");

        // Validate opportunity
        if (dto.getOpportunityId() != null) {
            CrmOpportunity opp = opportunityMapper.selectByIdForUpdate(dto.getOpportunityId());
            if (opp == null || Integer.valueOf(1).equals(opp.getDeleted()))
                throw new BusinessException(ResultCode.PARAM_ERROR, "Opportunity not found");
            if (!Objects.equals(opp.getCustomerId(), dto.getCustomerId()))
                throw new BusinessException(ResultCode.PARAM_ERROR, "Opportunity does not belong to this customer");
            // Only won opportunities (stage=5)
            if (!"5".equals(opp.getStage()))
                throw new BusinessException(ResultCode.OPERATION_FAILED, "Order can only be created for won opportunities (stage=5)");
        }
        // Validate amount
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Amount cannot be negative");
        // Validate owner
        validateOwner(dto.getOwnerId());

        Long uid = LoginUserContext.getCurrentUserId();
        CrmOrder o = new CrmOrder();
        o.setOrderNo(generateOrderNo());
        o.setCustomerId(dto.getCustomerId());
        o.setOpportunityId(dto.getOpportunityId());
        o.setAmount(dto.getAmount());
        o.setStatus(1);
        o.setPaymentMethod(dto.getPaymentMethod());
        o.setOwnerId(dto.getOwnerId());
        o.setCreatedBy(uid);
        o.setUpdatedBy(uid);
        o.setRemark(dto.getRemark());
        orderMapper.insert(o);
        log.info("Order created: id={}, orderNo={}", o.getId(), o.getOrderNo());
    }

    @Override
    public void updateOrder(Long id, OrderUpdateDTO dto) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(1).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only pending orders can be modified");

        if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Amount cannot be negative");
        if (dto.getOwnerId() != null) validateOwner(dto.getOwnerId());

        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        if (dto.getAmount() != null) w.set(CrmOrder::getAmount, dto.getAmount());
        if (dto.getPaymentMethod() != null) w.set(CrmOrder::getPaymentMethod, dto.getPaymentMethod().trim());
        if (dto.getOwnerId() != null) w.set(CrmOrder::getOwnerId, dto.getOwnerId());
        if (dto.getRemark() != null) w.set(CrmOrder::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Order updated: id={}", id);
    }

    @Override
    public void deleteOrder(Long id) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(1).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only pending orders can be deleted");

        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        w.set(CrmOrder::getDeleted, 1);
        updateOrThrow(w);
        log.info("Order deleted: id={}", id);
    }

    @Override
    public void updateStatus(Long id, OrderStatusDTO dto) {
        if (dto == null || dto.getStatus() == null || dto.getStatus() < 1 || dto.getStatus() > 5)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 1-5");
        // Reuse the same lifecycle checks and timestamps as the dedicated endpoints.
        switch (dto.getStatus()) {
            case 2:
                OrderPayDTO pay = new OrderPayDTO();
                pay.setPaymentMethod(dto.getPaymentMethod());
                pay.setRemark(dto.getRemark());
                pay(id, pay);
                break;
            case 3:
                OrderShipDTO ship = new OrderShipDTO();
                ship.setLogisticsNo(dto.getLogisticsNo());
                ship.setLogisticsCompany(dto.getLogisticsCompany());
                ship.setRemark(dto.getRemark());
                ship(id, ship);
                break;
            case 4:
                OrderCancelDTO complete = new OrderCancelDTO();
                complete.setRemark(dto.getRemark());
                complete(id, complete);
                break;
            case 5:
                OrderCancelDTO cancel = new OrderCancelDTO();
                cancel.setRemark(dto.getRemark());
                cancel(id, cancel);
                break;
            default:
                throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot revert an order to pending payment");
        }
    }

    @Override
    public void pay(Long id, OrderPayDTO dto) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(1).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only pending orders can be paid");
        String paymentMethod = dto != null && notEmpty(dto.getPaymentMethod())
                ? dto.getPaymentMethod().trim() : o.getPaymentMethod();
        if (!notEmpty(paymentMethod))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Payment method cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        w.set(CrmOrder::getStatus, 2)
         .set(CrmOrder::getPaymentTime, now)
         .set(CrmOrder::getPaymentMethod, paymentMethod.trim());
        if (dto != null && notEmpty(dto.getRemark())) w.set(CrmOrder::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Order paid: id={}", id);
    }

    @Override
    public void ship(Long id, OrderShipDTO dto) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(2).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only paid orders can be shipped");
        if (dto == null || !notEmpty(dto.getLogisticsNo()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Logistics tracking number cannot be blank");

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        w.set(CrmOrder::getStatus, 3)
         .set(CrmOrder::getDeliveryTime, now)
         .set(CrmOrder::getLogisticsNo, dto.getLogisticsNo().trim())
         .set(CrmOrder::getLogisticsCompany, dto.getLogisticsCompany() == null ? null : dto.getLogisticsCompany().trim());
        if (notEmpty(dto.getRemark())) w.set(CrmOrder::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Order shipped: id={}", id);
    }

    @Override
    public void complete(Long id, OrderCancelDTO dto) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(3).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only shipped orders can be completed");

        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        w.set(CrmOrder::getStatus, 4)
         .set(CrmOrder::getFinishTime, LocalDateTime.now());
        if (dto != null && notEmpty(dto.getRemark())) w.set(CrmOrder::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Order completed: id={}", id);
    }

    @Override
    public void cancel(Long id, OrderCancelDTO dto) {
        CrmOrder o = getOrThrow(id);
        if (!Integer.valueOf(1).equals(o.getStatus()) && !Integer.valueOf(2).equals(o.getStatus()))
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Only pending or paid orders can be cancelled");

        LambdaUpdateWrapper<CrmOrder> w = updateForState(o);
        w.set(CrmOrder::getStatus, 5)
         .set(CrmOrder::getCancelTime, LocalDateTime.now());
        if (dto != null && notEmpty(dto.getRemark())) w.set(CrmOrder::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Order cancelled: id={}", id);
    }

    @Override
    public PageResult<OrderVO> listByCustomer(Long customerId, long page, long size) {
        CrmCustomer c = customerMapper.selectById(customerId);
        if (c == null || c.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Customer not found");
        LambdaQueryWrapper<CrmOrder> w = new LambdaQueryWrapper<>();
        w.eq(CrmOrder::getCustomerId, customerId).eq(CrmOrder::getDeleted, 0).orderByDesc(CrmOrder::getId);
        Page<CrmOrder> p = new Page<>(page, size);
        Page<CrmOrder> result = orderMapper.selectPage(p, w);
        List<OrderVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public PageResult<OrderVO> listByOpportunity(Long opportunityId, long page, long size) {
        CrmOpportunity opp = opportunityMapper.selectById(opportunityId);
        if (opp == null || opp.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Opportunity not found");
        LambdaQueryWrapper<CrmOrder> w = new LambdaQueryWrapper<>();
        w.eq(CrmOrder::getOpportunityId, opportunityId).eq(CrmOrder::getDeleted, 0).orderByDesc(CrmOrder::getId);
        Page<CrmOrder> p = new Page<>(page, size);
        Page<CrmOrder> result = orderMapper.selectPage(p, w);
        List<OrderVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ========== helpers ==========

    private String generateOrderNo() {
        String prefix = "ORD" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        String orderNo = prefix + suffix;
        // Ensure uniqueness
        int retries = 0;
        while (orderMapper.countByOrderNo(orderNo) > 0 && retries < 10) {
            suffix = String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
            orderNo = prefix + suffix;
            retries++;
        }
        return orderNo;
    }

    private LambdaUpdateWrapper<CrmOrder> updateForState(CrmOrder order) {
        LambdaUpdateWrapper<CrmOrder> w = new LambdaUpdateWrapper<>();
        w.eq(CrmOrder::getId, order.getId())
         .eq(CrmOrder::getDeleted, 0)
         .eq(CrmOrder::getStatus, order.getStatus())
         .set(CrmOrder::getUpdatedBy, LoginUserContext.getCurrentUserId())
         .set(CrmOrder::getUpdatedAt, LocalDateTime.now());
        return w;
    }

    private void updateOrThrow(LambdaUpdateWrapper<CrmOrder> wrapper) {
        if (orderMapper.update(null, wrapper) != 1)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Order changed or was deleted; refresh and try again");
    }

    private CrmOrder getOrThrow(Long id) {
        CrmOrder o = orderMapper.selectById(id);
        if (o == null || Integer.valueOf(1).equals(o.getDeleted()))
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Order not found");
        return o;
    }

    private void validateOwner(Long ownerId) {
        SysUser u = sysUserMapper.selectById(ownerId);
        if (u == null || u.getDeleted() == 1) throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user not found");
        if (u.getStatus() == null || u.getStatus() == 0) throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user is disabled");
    }

    private String statusName(Integer s) { return s == null ? null : STATUS_NAMES.getOrDefault(s, "Unknown"); }
    private String customerName(Long id) {
        if (id == null) return null;
        CrmCustomer c = customerMapper.selectById(id);
        return c != null ? c.getCustomerName() : null;
    }
    private String oppName(Long id) {
        if (id == null) return null;
        CrmOpportunity o = opportunityMapper.selectById(id);
        return o != null ? o.getOpportunityName() : null;
    }
    private String userName(Long id) {
        if (id == null) return null;
        SysUser u = sysUserMapper.selectById(id);
        return u != null ? u.getRealName() : null;
    }

    private Map<Long, String> loadCustomerNames(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return customerMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CrmCustomer::getId, CrmCustomer::getCustomerName, (a, b) -> a));
    }

    private Map<Long, String> loadOpportunityNames(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return opportunityMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(CrmOpportunity::getId, CrmOpportunity::getOpportunityName, (a, b) -> a));
    }

    private Map<Long, String> loadUserNames(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return Collections.emptyMap();
        return sysUserMapper.selectBatchIds(ids).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
    }

    private String lookupName(Map<Long, String> names, Long id, java.util.function.Function<Long, String> fallback) {
        if (id == null) return null;
        String name = names.get(id);
        return name != null ? name : fallback.apply(id);
    }

    private OrderVO toVO(CrmOrder o) {
        return toVO(o, Collections.emptyMap(), Collections.emptyMap(), Collections.emptyMap());
    }

    private OrderVO toVO(CrmOrder o, Map<Long, String> customerNames,
                         Map<Long, String> opportunityNames,
                         Map<Long, String> ownerNames) {
        OrderVO vo = new OrderVO();
        vo.setId(o.getId()); vo.setOrderNo(o.getOrderNo()); vo.setCustomerId(o.getCustomerId());
        vo.setCustomerName(lookupName(customerNames, o.getCustomerId(), this::customerName)); vo.setOpportunityId(o.getOpportunityId());
        vo.setOpportunityName(lookupName(opportunityNames, o.getOpportunityId(), this::oppName)); vo.setAmount(o.getAmount());
        vo.setStatus(o.getStatus()); vo.setStatusName(statusName(o.getStatus()));
        vo.setPaymentMethod(o.getPaymentMethod()); vo.setPaymentTime(o.getPaymentTime());
        vo.setDeliveryTime(o.getDeliveryTime()); vo.setFinishTime(o.getFinishTime());
        vo.setCancelTime(o.getCancelTime()); vo.setLogisticsNo(o.getLogisticsNo());
        vo.setLogisticsCompany(o.getLogisticsCompany()); vo.setOwnerId(o.getOwnerId());
        vo.setOwnerName(lookupName(ownerNames, o.getOwnerId(), this::userName)); vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private OrderDetailVO toDetailVO(CrmOrder o) {
        OrderDetailVO vo = new OrderDetailVO();
        vo.setId(o.getId()); vo.setOrderNo(o.getOrderNo()); vo.setCustomerId(o.getCustomerId());
        vo.setCustomerName(customerName(o.getCustomerId())); vo.setOpportunityId(o.getOpportunityId());
        vo.setOpportunityName(oppName(o.getOpportunityId())); vo.setAmount(o.getAmount());
        vo.setStatus(o.getStatus()); vo.setStatusName(statusName(o.getStatus()));
        vo.setPaymentMethod(o.getPaymentMethod()); vo.setPaymentTime(o.getPaymentTime());
        vo.setDeliveryTime(o.getDeliveryTime()); vo.setFinishTime(o.getFinishTime());
        vo.setCancelTime(o.getCancelTime()); vo.setLogisticsNo(o.getLogisticsNo());
        vo.setLogisticsCompany(o.getLogisticsCompany()); vo.setOwnerId(o.getOwnerId());
        vo.setOwnerName(userName(o.getOwnerId())); vo.setCreatedAt(o.getCreatedAt());
        vo.setUpdatedAt(o.getUpdatedAt()); vo.setRemark(o.getRemark());
        return vo;
    }

    private boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
}
