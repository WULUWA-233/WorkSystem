package com.crm.service.impl;

import com.crm.common.BusinessException;
import com.crm.common.ResultCode;
import com.crm.mapper.AnalyticsMapper;
import com.crm.utils.DateParseUtils;
import com.crm.service.AnalyticsService;
import com.crm.vo.*;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsServiceImpl implements AnalyticsService {

    private static final Map<Integer, String> STAGE_NAMES = new LinkedHashMap<>();
    private static final Map<Integer, String> ORDER_STATUS_NAMES = new LinkedHashMap<>();
    static {
        STAGE_NAMES.put(1, "Initial Contact"); STAGE_NAMES.put(2, "Requirements Confirmed");
        STAGE_NAMES.put(3, "Proposal/Quotation"); STAGE_NAMES.put(4, "Negotiation");
        STAGE_NAMES.put(5, "Won"); STAGE_NAMES.put(6, "Lost");
        ORDER_STATUS_NAMES.put(1, "Pending Payment"); ORDER_STATUS_NAMES.put(2, "Paid");
        ORDER_STATUS_NAMES.put(3, "Shipped"); ORDER_STATUS_NAMES.put(4, "Completed");
        ORDER_STATUS_NAMES.put(5, "Cancelled");
    }

    private final AnalyticsMapper mapper;

    public AnalyticsServiceImpl(AnalyticsMapper mapper) { this.mapper = mapper; }

    @Override
    public OverviewVO getOverview(String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        startTime = range[0]; endTime = range[1];
        OverviewVO vo = new OverviewVO();
        vo.setCustomerTotal(mapper.countCustomers());
        vo.setNewCustomerCount(mapper.countNewCustomers(startTime, endTime));
        vo.setOpportunityTotal(mapper.countOpportunities(startTime, endTime));
        vo.setOpportunityAmount(mapper.sumOpportunityAmount(startTime, endTime));
        vo.setWonOpportunityCount(mapper.countWonOpportunities(startTime, endTime));
        vo.setLostOpportunityCount(mapper.countLostOpportunities(startTime, endTime));
        vo.setOrderTotal(mapper.countOrders(startTime, endTime));
        vo.setOrderAmount(mapper.sumOrderAmount(startTime, endTime));
        vo.setPaidOrderAmount(mapper.sumPaidOrderAmount(startTime, endTime));
        vo.setFollowRecordTotal(mapper.countFollowRecords(startTime, endTime));
        vo.setTodayFollowCount(mapper.countTodayFollows());
        return vo;
    }

    @Override
    public List<TrendItemVO> getCustomerTrend(String startDate, String endDate, String groupBy) {
        if (!"day".equals(groupBy) && !"month".equals(groupBy))
            throw new BusinessException(ResultCode.PARAM_ERROR, "groupBy must be day or month");
        String fmt = "month".equals(groupBy) ? "%Y-%m" : "%Y-%m-%d";
        LocalDate first = trendDate(startDate, groupBy, false);
        LocalDate last = trendDate(endDate, groupBy, true);
        if (first != null && last != null && first.isAfter(last))
            throw new BusinessException(ResultCode.PARAM_ERROR, "startDate must not be after endDate");
        String start = first == null ? null : first + " 00:00:00";
        String end = last == null ? null : last + " 23:59:59";
        return mapper.customerTrend(fmt, start, end).stream()
                .map(m -> new TrendItemVO((String) m.get("dt"), ((Number) m.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<StageStatsVO> getOpportunityStageStats(String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        startTime = range[0]; endTime = range[1];
        Map<Integer, StageStatsVO> map = new LinkedHashMap<>();
        for (int i = 1; i <= 6; i++) map.put(i, new StageStatsVO(i, STAGE_NAMES.get(i), 0L, BigDecimal.ZERO));
        mapper.opportunityStageStats(startTime, endTime).forEach(m -> {
            int s = stageNumber(m.get("stage"));
            StageStatsVO vo = map.get(s);
            if (vo != null) {
                vo.setCount(((Number) m.get("cnt")).longValue());
                vo.setAmount((BigDecimal) m.get("amt"));
            }
        });
        return new ArrayList<>(map.values());
    }

    @Override
    public List<OrderStatusStatsVO> getOrderStatusStats(String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        startTime = range[0]; endTime = range[1];
        Map<Integer, OrderStatusStatsVO> map = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) map.put(i, new OrderStatusStatsVO(i, ORDER_STATUS_NAMES.get(i), 0L, BigDecimal.ZERO));
        mapper.orderStatusStats(startTime, endTime).forEach(m -> {
            int s = stageNumber(m.get("status"));
            OrderStatusStatsVO vo = map.get(s);
            if (vo != null) {
                vo.setCount(((Number) m.get("cnt")).longValue());
                vo.setAmount((BigDecimal) m.get("amt"));
            }
        });
        return new ArrayList<>(map.values());
    }

    @Override
    public List<SalesPerformanceVO> getSalesPerformance(Long ownerId, String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        return mapper.salesPerformance(ownerId, range[0], range[1]).stream().map(m -> {
            SalesPerformanceVO vo = new SalesPerformanceVO();
            vo.setOwnerId(((Number) m.get("ownerId")).longValue());
            vo.setOwnerName((String) m.get("ownerName"));
            vo.setCustomerCount(((Number) m.get("customerCount")).longValue());
            vo.setOpportunityCount(((Number) m.get("opportunityCount")).longValue());
            vo.setWonOpportunityCount(((Number) m.get("wonOpportunityCount")).longValue());
            vo.setOrderCount(((Number) m.get("orderCount")).longValue());
            vo.setOrderAmount((BigDecimal) m.get("orderAmount"));
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<NameValueVO> getCustomerIndustryStats(String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        startTime = range[0]; endTime = range[1];
        return mapper.customerIndustryStats(startTime, endTime).stream()
                .map(m -> new NameValueVO((String) m.get("nm"), ((Number) m.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    @Override
    public List<NameValueVO> getCustomerRegionStats(String startTime, String endTime) {
        String[] range = timeRange(startTime, endTime);
        startTime = range[0]; endTime = range[1];
        return mapper.customerRegionStats(startTime, endTime).stream()
                .map(m -> new NameValueVO((String) m.get("nm"), ((Number) m.get("cnt")).longValue()))
                .collect(Collectors.toList());
    }

    private LocalDate trendDate(String value, String groupBy, boolean end) {
        if (value == null || value.trim().isEmpty()) return null;
        if ("month".equals(groupBy) && value.matches("\\d{4}-\\d{2}")) {
            try {
                YearMonth month = YearMonth.parse(value);
                return end ? month.atEndOfMonth() : month.atDay(1);
            } catch (DateTimeParseException e) {
                throw new BusinessException(ResultCode.PARAM_ERROR, "Invalid month: expected yyyy-MM");
            }
        }
        return DateParseUtils.parseDate(value, end ? "endDate" : "startDate");
    }

    private String[] timeRange(String start, String end) {
        String first = start == null || start.trim().isEmpty() ? null : start;
        String last = end == null || end.trim().isEmpty() ? null : end;
        LocalDateTime from = first == null ? null : DateParseUtils.parseDateTime(first, "startTime");
        LocalDateTime to = last == null ? null : DateParseUtils.parseDateTime(last, "endTime");
        if (from != null && to != null && from.isAfter(to))
            throw new BusinessException(ResultCode.PARAM_ERROR, "startTime must not be after endTime");
        return new String[]{first, last};
    }

    private int stageNumber(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        try { return Integer.parseInt(String.valueOf(value)); }
        catch (NumberFormatException e) { return 0; }
    }
}
