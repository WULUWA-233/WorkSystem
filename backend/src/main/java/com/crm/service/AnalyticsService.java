package com.crm.service;

import com.crm.vo.*;

import java.util.List;

public interface AnalyticsService {
    OverviewVO getOverview(String startTime, String endTime);
    List<TrendItemVO> getCustomerTrend(String startDate, String endDate, String groupBy);
    List<StageStatsVO> getOpportunityStageStats(String startTime, String endTime);
    List<OrderStatusStatsVO> getOrderStatusStats(String startTime, String endTime);
    List<SalesPerformanceVO> getSalesPerformance(Long ownerId, String startTime, String endTime);
    default List<SalesPerformanceVO> getSalesPerformance(Long ownerId) {
        return getSalesPerformance(ownerId, null, null);
    }
    List<NameValueVO> getCustomerIndustryStats(String startTime, String endTime);
    List<NameValueVO> getCustomerRegionStats(String startTime, String endTime);
}
