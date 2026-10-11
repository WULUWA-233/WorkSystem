package com.crm.service;

import com.crm.common.GlobalExceptionHandler;
import com.crm.controller.AnalyticsController;
import com.crm.mapper.AnalyticsMapper;
import com.crm.service.impl.AnalyticsServiceImpl;
import com.crm.vo.OverviewVO;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnalyticsRangeRegressionTest {
    private static final String START = "2024-02-01 00:00:00";
    private static final String END = "2024-02-29 23:59:59";

    @Test
    void overviewUsesOnePeriodForCountsAndAmountsWhileRetainingLifetimeAndTodayMetrics() throws Exception {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        when(mapper.countCustomers()).thenReturn(100L);
        when(mapper.countNewCustomers(START, END)).thenReturn(2L);
        when(mapper.sumOpportunityAmount(START, END)).thenReturn(new BigDecimal("123.45"));
        when(mapper.sumOrderAmount(START, END)).thenReturn(new BigDecimal("88.00"));
        when(mapper.sumPaidOrderAmount(START, END)).thenReturn(new BigDecimal("66.00"));
        when(mapper.countTodayFollows()).thenReturn(3L);

        OverviewVO overview = new AnalyticsServiceImpl(mapper).getOverview(START, END);
        assertEquals(100L, overview.getCustomerTotal());
        assertEquals(2L, overview.getNewCustomerCount());
        assertEquals(new BigDecimal("123.45"), overview.getOpportunityAmount());
        assertEquals(new BigDecimal("88.00"), overview.getOrderAmount());
        assertEquals(new BigDecimal("66.00"), overview.getPaidOrderAmount());
        assertEquals(3L, overview.getTodayFollowCount());
        verify(mapper).countOpportunities(START, END);
        verify(mapper).countWonOpportunities(START, END);
        verify(mapper).countLostOpportunities(START, END);
        verify(mapper).countOrders(START, END);
        verify(mapper).countFollowRecords(START, END);

        // Parse the real mapper scripts without a database to verify bound date predicates.
        for (String name : Arrays.asList("sumOpportunityAmount", "sumOrderAmount", "sumPaidOrderAmount",
                "countWonOpportunities", "countLostOpportunities")) {
            Method method = AnalyticsMapper.class.getMethod(name, String.class, String.class);
            BoundSql sql = boundSql(method, dates());
            assertTrue(sql.getSql().contains("created_at >= ?"));
            assertTrue(sql.getSql().contains("created_at <= ?"));
            assertEquals(Arrays.asList("start", "end"), sql.getParameterMappings().stream()
                    .map(mapping -> mapping.getProperty()).collect(Collectors.toList()));
            assertFalse(boundSql(method, Collections.emptyMap()).getSql().contains("created_at"));
        }
    }

    @Test
    void salesEndpointAcceptsDateRangeAndLegacyOwnerFilterAndBindsEveryBusinessMetric() throws Exception {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        when(mapper.salesPerformance(7L, START, END)).thenReturn(Collections.emptyList());
        when(mapper.salesPerformance(7L, null, null)).thenReturn(Collections.emptyList());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new AnalyticsController(new AnalyticsServiceImpl(mapper)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/v1/analytics/sales/performance").param("ownerId", "7")
                        .param("startTime", START).param("endTime", END))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isArray());
        mvc.perform(get("/api/v1/analytics/sales/performance").param("ownerId", "7"))
                .andExpect(status().isOk());
        verify(mapper).salesPerformance(7L, START, END);
        verify(mapper).salesPerformance(7L, null, null);

        Method method = AnalyticsMapper.class.getMethod("salesPerformance", Long.class, String.class, String.class);
        Map<String, Object> params = dates();
        params.put("ownerId", 7L);
        BoundSql sql = boundSql(method, params);
        assertEquals(5, sql.getParameterMappings().stream().filter(p -> "start".equals(p.getProperty())).count());
        assertEquals(5, sql.getParameterMappings().stream().filter(p -> "end".equals(p.getProperty())).count());
        assertEquals("ownerId", sql.getParameterMappings().get(10).getProperty());
        assertFalse(sql.getSql().contains(START));
        assertFalse(boundSql(method, Collections.emptyMap()).getSql().contains("created_at"));
    }

    @Test
    void monthlyTrendKeepsLeapYearBoundsAfterRangeUnification() {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        when(mapper.customerTrend("%Y-%m", START, END)).thenReturn(Collections.emptyList());
        assertTrue(new AnalyticsServiceImpl(mapper).getCustomerTrend("2024-02", "2024-02", "month").isEmpty());
        verify(mapper).customerTrend("%Y-%m", START, END);
    }

    private static Map<String, Object> dates() {
        Map<String, Object> params = new HashMap<>();
        params.put("start", START);
        params.put("end", END);
        return params;
    }

    private static BoundSql boundSql(Method method, Map<String, Object> params) {
        String script = String.join(" ", method.getAnnotation(Select.class).value());
        return new XMLLanguageDriver().createSqlSource(new Configuration(), script, Map.class).getBoundSql(params);
    }
}
