package com.crm.common;

import com.crm.controller.FollowRecordController;
import com.crm.mapper.AnalyticsMapper;
import com.crm.service.CrmFollowRecordService;
import com.crm.service.impl.AnalyticsServiceImpl;
import com.crm.utils.DateParseUtils;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class QueryAndAnalyticsTest {
    @Test void constructorsAndAliasesEnforcePaginationBoundsAndStandardParameterPriority() {
        PageQuery bounded = new PageQuery(-1, 1000);
        assertEquals(1, bounded.getPage());
        assertEquals(100, bounded.getSize());
        PageQuery query = new PageQuery();
        query.setPageNo(3); query.setPageSize(50);
        assertEquals(3, query.getPage()); assertEquals(50, query.getSize());
        query.setPage(2); query.setSize(20);
        query.setPageNo(4); query.setPageSize(70);
        assertEquals(2, query.getPage()); assertEquals(20, query.getSize());
    }

    @Test void nestedFollowEndpointsBindNewAndLegacyPagination() throws Exception {
        CrmFollowRecordService service = mock(CrmFollowRecordService.class);
        when(service.listByCustomer(anyLong(), anyLong(), anyLong())).thenReturn(new PageResult<>());
        when(service.listByOpportunity(anyLong(), anyLong(), anyLong())).thenReturn(new PageResult<>());
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new FollowRecordController(service)).setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/v1/customer/customers/1/follow-records").param("page", "2").param("size", "30")).andExpect(status().isOk());
        verify(service).listByCustomer(1L, 2L, 30L);
        mvc.perform(get("/api/v1/sales/opportunities/2/follow-records").param("pageNo", "3").param("pageSize", "1000")).andExpect(status().isOk());
        verify(service).listByOpportunity(2L, 3L, 100L);
    }

    @Test void invalidPathIdIsClientErrorInsteadOfInternalError() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new FollowRecordController(mock(CrmFollowRecordService.class)))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(get("/api/v1/customer/follow-records/invalid"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value(ResultCode.PARAM_ERROR.getCode()));
    }

    @Test void dateParsingRejectsImpossibleDatesInsteadOfSilentlyClamping() {
        assertEquals(LocalDate.of(2024, 2, 29), DateParseUtils.parseDate("2024-02-29", "date"));
        assertThrows(BusinessException.class, () -> DateParseUtils.parseDate("2023-02-29", "date"));
        assertThrows(BusinessException.class, () -> DateParseUtils.parseDateTime("2024-02-30 12:00:00", "time"));
        assertThrows(BusinessException.class, () -> DateParseUtils.parseDate(null, "date"));
    }

    @Test void monthTrendUsesActualLeapMonthEnd() {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        when(mapper.customerTrend(anyString(), anyString(), anyString())).thenReturn(Collections.emptyList());
        new AnalyticsServiceImpl(mapper).getCustomerTrend("2024-02", "2024-02", "month");
        verify(mapper).customerTrend("%Y-%m", "2024-02-01 00:00:00", "2024-02-29 23:59:59");
    }

    @Test void monthGroupingPreservesSelectedDayRange() {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        when(mapper.customerTrend(anyString(), anyString(), anyString())).thenReturn(Collections.emptyList());
        new AnalyticsServiceImpl(mapper).getCustomerTrend("2024-02-10", "2024-02-20", "month");
        verify(mapper).customerTrend("%Y-%m", "2024-02-10 00:00:00", "2024-02-20 23:59:59");
    }

    @Test void invalidAnalyticsRangesFailBeforeDatabaseQuery() {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        AnalyticsServiceImpl service = new AnalyticsServiceImpl(mapper);
        assertThrows(BusinessException.class, () -> service.getCustomerTrend("2024-02", "2024-01", "month"));
        assertThrows(BusinessException.class, () -> service.getCustomerTrend(null, null, "week"));
        assertThrows(BusinessException.class, () -> service.getOverview("2024-02-02 00:00:00", "2024-02-01 00:00:00"));
        verifyNoInteractions(mapper);
    }

    @Test void aggregationHandlesJdbcNumericVariantsAndLegacyNullStages() {
        AnalyticsMapper mapper = mock(AnalyticsMapper.class);
        Map<String, Object> row = new HashMap<>();
        row.put("status", (byte) 2); row.put("cnt", 3L); row.put("amt", BigDecimal.TEN);
        when(mapper.orderStatusStats(null, null)).thenReturn(Collections.singletonList(row));
        assertEquals(3L, new AnalyticsServiceImpl(mapper).getOrderStatusStats(null, null).get(1).getCount());
        row = new HashMap<>();
        row.put("stage", null); row.put("cnt", 1L); row.put("amt", BigDecimal.ZERO);
        when(mapper.opportunityStageStats(null, null)).thenReturn(Collections.singletonList(row));
        assertEquals(6, new AnalyticsServiceImpl(mapper).getOpportunityStageStats(null, null).size());
    }
}

