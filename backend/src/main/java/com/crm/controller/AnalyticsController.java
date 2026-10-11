package com.crm.controller;

import com.crm.common.Result;
import com.crm.service.AnalyticsService;
import com.crm.vo.*;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "Analytics")
@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) { this.analyticsService = analyticsService; }

    @ApiOperation("Overview")
    @GetMapping("/overview")
    public Result<OverviewVO> overview(@RequestParam(required = false) String startTime,
                                        @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getOverview(startTime, endTime));
    }

    @ApiOperation("Customer Trend")
    @GetMapping("/customers/new")
    public Result<List<TrendItemVO>> customerTrend(@RequestParam(required = false) String startDate,
                                                    @RequestParam(required = false) String endDate,
                                                    @RequestParam(defaultValue = "day") String groupBy) {
        return Result.success(analyticsService.getCustomerTrend(startDate, endDate, groupBy));
    }

    @ApiOperation("Opportunity Stage Distribution")
    @GetMapping("/opportunities/stage")
    public Result<List<StageStatsVO>> opportunityStage(@RequestParam(required = false) String startTime,
                                                        @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getOpportunityStageStats(startTime, endTime));
    }

    @ApiOperation("Order Status Distribution")
    @GetMapping("/orders/status")
    public Result<List<OrderStatusStatsVO>> orderStatus(@RequestParam(required = false) String startTime,
                                                         @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getOrderStatusStats(startTime, endTime));
    }

    @ApiOperation("Sales Performance")
    @GetMapping("/sales/performance")
    public Result<List<SalesPerformanceVO>> salesPerformance(@RequestParam(required = false) Long ownerId,
                                                            @RequestParam(required = false) String startTime,
                                                            @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getSalesPerformance(ownerId, startTime, endTime));
    }

    @ApiOperation("Customer Industry Distribution")
    @GetMapping("/customers/industry")
    public Result<List<NameValueVO>> customerIndustry(@RequestParam(required = false) String startTime,
                                                       @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getCustomerIndustryStats(startTime, endTime));
    }

    @ApiOperation("Customer Region Distribution")
    @GetMapping("/customers/region")
    public Result<List<NameValueVO>> customerRegion(@RequestParam(required = false) String startTime,
                                                     @RequestParam(required = false) String endTime) {
        return Result.success(analyticsService.getCustomerRegionStats(startTime, endTime));
    }
}
