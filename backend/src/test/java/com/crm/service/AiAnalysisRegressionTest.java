package com.crm.service;

import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmOrder;
import com.crm.mapper.*;
import com.crm.service.impl.AiAnalysisServiceImpl;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AiAnalysisRegressionTest {
    @Test void monetaryMetricsSumDecimalsBeforeConvertingToJsonNumbers() {
        AiAnalysisServiceImpl service = serviceWithOrders(order("0.10", 2), order("0.20", 3));
        com.crm.vo.CustomerAiMetricsVO metrics = service.analyzeCustomer(1L).getMetrics();
        assertEquals(0.30, metrics.getTotalOrderAmount());
        assertEquals(0.30, metrics.getPaidAmount());
    }

    @Test void unpaidAndCancelledOrdersAreNotDescribedAsCompletedSales() {
        com.crm.vo.CustomerAiAnalysisVO analysis = serviceWithOrders(order("100", 1), order("200", 5)).analyzeCustomer(1L);
        assertEquals(0.0, analysis.getMetrics().getPaidAmount());
        assertFalse(analysis.getSummary().contains("有一定成交基础"));
        assertFalse(analysis.getSummary().contains("历史成交金额较高"));
        assertTrue(analysis.getSummary().contains("尚无有效已付款订单"));
    }

    private AiAnalysisServiceImpl serviceWithOrders(CrmOrder... orders) {
        CrmCustomerMapper customers = mock(CrmCustomerMapper.class);
        CrmOpportunityMapper opportunities = mock(CrmOpportunityMapper.class);
        CrmOrderMapper orderMapper = mock(CrmOrderMapper.class);
        CrmFollowRecordMapper follows = mock(CrmFollowRecordMapper.class);
        CrmCustomer customer = new CrmCustomer(); customer.setId(1L); customer.setDeleted(0); customer.setCustomerName("测试客户");
        when(customers.selectById(1L)).thenReturn(customer);
        when(opportunities.selectList(any())).thenReturn(Collections.emptyList());
        when(orderMapper.selectList(any())).thenReturn(Arrays.asList(orders));
        when(follows.selectList(any())).thenReturn(Collections.emptyList());
        return new AiAnalysisServiceImpl(customers, opportunities, orderMapper, follows);
    }

    private CrmOrder order(String amount, int status) {
        CrmOrder order = new CrmOrder(); order.setAmount(new BigDecimal(amount)); order.setStatus(status);
        return order;
    }
}

