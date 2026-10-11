package com.crm;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.crm.common.BusinessException;
import com.crm.common.GlobalExceptionHandler;
import com.crm.controller.OrderController;
import com.crm.dto.OrderCancelDTO;
import com.crm.dto.OrderCreateDTO;
import com.crm.dto.OrderPayDTO;
import com.crm.dto.OrderShipDTO;
import com.crm.dto.OrderStatusDTO;
import com.crm.dto.OrderUpdateDTO;
import com.crm.entity.CrmOrder;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmOpportunity;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.CrmOrderMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmOrderService;
import com.crm.service.impl.CrmOrderServiceImpl;
import com.crm.utils.LoginUserContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** HTTP binding and order lifecycle checks require neither a server nor a live database. */
class OrderRegressionTest {
    private CrmOrderService controllerService;
    private CrmOrderMapper mapper;
    private CrmOrderServiceImpl service;
    private MockMvc mvc;

    @BeforeEach
    void setup() {
        controllerService = mock(CrmOrderService.class);
        mapper = mock(CrmOrderMapper.class);
        service = new CrmOrderServiceImpl(mapper, mock(CrmCustomerMapper.class),
                mock(CrmOpportunityMapper.class), mock(SysUserMapper.class));
        mvc = MockMvcBuilders.standaloneSetup(new OrderController(controllerService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        LoginUserContext.setCurrentUser(91L, "test-user", Collections.singletonList("ADMIN"));
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "order-test"), CrmOrder.class);
        when(mapper.update(isNull(), any())).thenReturn(1);
    }

    @AfterEach
    void cleanup() {
        LoginUserContext.clear();
    }

    @Test
    void completeAndCancelAcceptAbsentBodies() throws Exception {
        mvc.perform(patch("/api/v1/sales/orders/42/complete")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/sales/orders/42/cancel")).andExpect(status().isOk());
        verify(controllerService).complete(42L, null);
        verify(controllerService).cancel(42L, null);
    }

    @Test
    void completeAndCancelAlsoAcceptEmptyObjects() throws Exception {
        mvc.perform(patch("/api/v1/sales/orders/42/complete")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        mvc.perform(patch("/api/v1/sales/orders/42/cancel")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isOk());
        verify(controllerService).complete(eq(42L), any(OrderCancelDTO.class));
        verify(controllerService).cancel(eq(42L), any(OrderCancelDTO.class));
    }

    @Test
    void associatedCustomerOrdersBindStandardPagination() throws Exception {
        mvc.perform(get("/api/v1/customer/customers/7/orders").param("page", "2").param("size", "100"))
                .andExpect(status().isOk());
        verify(controllerService).listByCustomer(7L, 2L, 100L);
    }

    @Test
    void associatedOpportunityOrdersBindLegacyPagination() throws Exception {
        mvc.perform(get("/api/v1/sales/opportunities/8/orders").param("pageNo", "3").param("pageSize", "20"))
                .andExpect(status().isOk());
        verify(controllerService).listByOpportunity(8L, 3L, 20L);
    }

    @Test
    void associatedOrderPaginationIsBounded() throws Exception {
        mvc.perform(get("/api/v1/customer/customers/7/orders").param("page", "-1").param("size", "10000"))
                .andExpect(status().isOk());
        verify(controllerService).listByCustomer(7L, 1L, 100L);
    }

    @Test
    void blankLogisticsNumberFailsHttpValidation() throws Exception {
        mvc.perform(patch("/api/v1/sales/orders/42/ship").contentType(MediaType.APPLICATION_JSON)
                .content("{\"logisticsNo\":\"   \"}")).andExpect(status().isBadRequest());
        verifyNoInteractions(controllerService);
    }

    @Test
    void negativeOrderAmountFailsHttpValidation() throws Exception {
        mvc.perform(put("/api/v1/sales/orders/42").contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":-1}")).andExpect(status().isBadRequest());
        verifyNoInteractions(controllerService);
    }

    @Test
    void excessPrecisionOrderAmountFailsHttpValidation() throws Exception {
        mvc.perform(put("/api/v1/sales/orders/42").contentType(MediaType.APPLICATION_JSON)
                .content("{\"amount\":1.001}")).andExpect(status().isBadRequest());
        verifyNoInteractions(controllerService);
    }

    @Test
    void paidAndShippedOrdersCannotBeEdited() {
        OrderUpdateDTO request = new OrderUpdateDTO();
        request.setAmount(new BigDecimal("900"));
        for (int state : new int[]{2, 3, 4, 5}) {
            when(mapper.selectById(42L)).thenReturn(order(state));
            assertThrows(BusinessException.class, () -> service.updateOrder(42L, request));
        }
        verify(mapper, never()).update(any(), any());
        verify(mapper, never()).updateById(any());
    }

    @Test
    void pendingOrderUpdateCannotOverwriteConcurrentPayment() {
        when(mapper.selectById(42L)).thenReturn(order(1));
        when(mapper.update(isNull(), any())).thenReturn(0);
        OrderUpdateDTO request = new OrderUpdateDTO();
        request.setAmount(new BigDecimal("900"));
        assertThrows(BusinessException.class, () -> service.updateOrder(42L, request));
        LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
        assertGuard(update, 1);
        assertEquals(new BigDecimal("900"), assignedValue(update, "amount"));
    }

    @Test
    void paymentSetsMetadataAndRequiresOriginalPendingState() {
        CrmOrder order = order(1);
        order.setPaymentMethod("银行转账");
        when(mapper.selectById(42L)).thenReturn(order);
        service.pay(42L, null);
        LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
        assertGuard(update, 1);
        assertEquals(2, assignedValue(update, "status"));
        assertEquals("银行转账", assignedValue(update, "payment_method"));
        assertInstanceOf(LocalDateTime.class, assignedValue(update, "payment_time"));
        assertInstanceOf(LocalDateTime.class, assignedValue(update, "updated_at"));
    }

    @Test
    void missingPaymentMethodCannotMarkOrderPaid() {
        when(mapper.selectById(42L)).thenReturn(order(1));
        OrderPayDTO request = new OrderPayDTO();
        request.setPaymentMethod("  ");
        assertThrows(BusinessException.class, () -> service.pay(42L, request));
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void genericStatusUsesTheFullLifecycle() {
        String[] timestamps = {"payment_time", "delivery_time", "finish_time"};
        for (int state = 1; state <= 3; state++) {
            reset(mapper);
            when(mapper.update(isNull(), any())).thenReturn(1);
            when(mapper.selectById(42L)).thenReturn(order(state));
            OrderStatusDTO request = new OrderStatusDTO();
            request.setStatus(state + 1);
            request.setPaymentMethod("银行转账");
            request.setLogisticsNo("SF123");
            request.setLogisticsCompany("顺丰");
            service.updateStatus(42L, request);
            LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
            assertGuard(update, state);
            assertEquals(state + 1, assignedValue(update, "status"));
            assertInstanceOf(LocalDateTime.class, assignedValue(update, timestamps[state - 1]));
            if (state == 2) assertEquals("SF123", assignedValue(update, "logistics_no"));
        }
    }

    @Test
    void genericShipStatusCannotBypassLogisticsRequirement() {
        when(mapper.selectById(42L)).thenReturn(order(2));
        OrderStatusDTO request = new OrderStatusDTO();
        request.setStatus(3);
        assertThrows(BusinessException.class, () -> service.updateStatus(42L, request));
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void lifecycleCannotSkipPayment() {
        when(mapper.selectById(42L)).thenReturn(order(1));
        OrderStatusDTO request = new OrderStatusDTO();
        request.setStatus(4);
        assertThrows(BusinessException.class, () -> service.updateStatus(42L, request));
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void cancelWritesTimestampAndGuardsPreviousState() {
        when(mapper.selectById(42L)).thenReturn(order(2));
        service.cancel(42L, null);
        LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
        assertGuard(update, 2);
        assertEquals(5, assignedValue(update, "status"));
        assertInstanceOf(LocalDateTime.class, assignedValue(update, "cancel_time"));
    }

    @Test
    void completeAcceptsAbsentRemarkAndWritesFinishTime() {
        when(mapper.selectById(42L)).thenReturn(order(3));
        service.complete(42L, null);
        LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
        assertGuard(update, 3);
        assertEquals(4, assignedValue(update, "status"));
        assertInstanceOf(LocalDateTime.class, assignedValue(update, "finish_time"));
    }

    @Test
    void concurrentStateChangeIsReportedInsteadOfFalseSuccess() {
        CrmOrder order = order(1);
        order.setPaymentMethod("银行转账");
        when(mapper.selectById(42L)).thenReturn(order);
        when(mapper.update(isNull(), any())).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.pay(42L, null));
        assertGuard(lastUpdate(), 1);
    }

    @Test
    void deletionCannotRaceWithPayment() {
        when(mapper.selectById(42L)).thenReturn(order(1));
        when(mapper.update(isNull(), any())).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.deleteOrder(42L));
        LambdaUpdateWrapper<CrmOrder> update = lastUpdate();
        assertGuard(update, 1);
        assertEquals(1, assignedValue(update, "deleted"));
    }

    @Test
    void alreadyDeletedOrderCannotBeUpdated() {
        CrmOrder order = order(1);
        order.setDeleted(1);
        when(mapper.selectById(42L)).thenReturn(order);
        assertThrows(BusinessException.class, () -> service.cancel(42L, null));
        verify(mapper, never()).update(any(), any());
    }

    @Test
    void createLocksCustomerThenWonOpportunityBeforeInsertingOrder() {
        CrmCustomerMapper customers = mock(CrmCustomerMapper.class);
        CrmOpportunityMapper opportunities = mock(CrmOpportunityMapper.class);
        SysUserMapper users = mock(SysUserMapper.class);
        CrmOrderServiceImpl creationService = new CrmOrderServiceImpl(mapper, customers, opportunities, users);
        CrmCustomer customer = new CrmCustomer();
        customer.setId(7L);
        customer.setDeleted(0);
        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.setId(8L);
        opportunity.setCustomerId(7L);
        opportunity.setDeleted(0);
        opportunity.setStage("5");
        SysUser owner = new SysUser();
        owner.setId(91L);
        owner.setDeleted(0);
        owner.setStatus(1);
        when(customers.selectByIdForUpdate(7L)).thenReturn(customer);
        when(opportunities.selectByIdForUpdate(8L)).thenReturn(opportunity);
        when(users.selectById(91L)).thenReturn(owner);
        OrderCreateDTO request = new OrderCreateDTO();
        request.setCustomerId(7L);
        request.setOpportunityId(8L);
        request.setOwnerId(91L);
        request.setAmount(new BigDecimal("100"));
        creationService.createOrder(request);
        InOrder locks = inOrder(customers, opportunities, mapper);
        locks.verify(customers).selectByIdForUpdate(7L);
        locks.verify(opportunities).selectByIdForUpdate(8L);
        locks.verify(mapper).countByOrderNo(anyString());
        locks.verify(mapper).insert(any(CrmOrder.class));
    }

    private CrmOrder order(int state) {
        CrmOrder order = new CrmOrder();
        order.setId(42L);
        order.setDeleted(0);
        order.setStatus(state);
        order.setAmount(new BigDecimal("100"));
        return order;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaUpdateWrapper<CrmOrder> lastUpdate() {
        ArgumentCaptor<Wrapper> capture = ArgumentCaptor.forClass(Wrapper.class);
        verify(mapper).update(isNull(), capture.capture());
        return (LambdaUpdateWrapper<CrmOrder>) capture.getValue();
    }

    private void assertGuard(LambdaUpdateWrapper<CrmOrder> update, int expectedState) {
        assertEquals(42L, valueInSql(update, update.getSqlSegment(), "id"));
        assertEquals(0, valueInSql(update, update.getSqlSegment(), "deleted"));
        assertEquals(expectedState, valueInSql(update, update.getSqlSegment(), "status"));
    }

    private Object assignedValue(LambdaUpdateWrapper<CrmOrder> update, String column) {
        return valueInSql(update, update.getSqlSet(), column);
    }

    private Object valueInSql(LambdaUpdateWrapper<CrmOrder> update, String sql, String column) {
        Pattern binding = Pattern.compile("(?<![a-zA-Z_])" + column
                + "\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)\\}");
        Matcher match = binding.matcher(sql);
        assertTrue(match.find(), "Missing SQL binding for " + column + ": " + sql);
        return update.getParamNameValuePairs().get(match.group(1));
    }
}
