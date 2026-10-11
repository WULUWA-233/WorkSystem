package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.crm.common.BusinessException;
import com.crm.common.ResultCode;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmFollowRecord;
import com.crm.entity.CrmOpportunity;
import com.crm.entity.CrmOrder;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmFollowRecordMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.CrmOrderMapper;
import com.crm.service.AiAnalysisService;
import com.crm.vo.CustomerAiAnalysisVO;
import com.crm.vo.CustomerAiMetricsVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiAnalysisServiceImpl implements AiAnalysisService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final CrmCustomerMapper customerMapper;
    private final CrmOpportunityMapper opportunityMapper;
    private final CrmOrderMapper orderMapper;
    private final CrmFollowRecordMapper followRecordMapper;

    public AiAnalysisServiceImpl(CrmCustomerMapper customerMapper,
                                  CrmOpportunityMapper opportunityMapper,
                                  CrmOrderMapper orderMapper,
                                  CrmFollowRecordMapper followRecordMapper) {
        this.customerMapper = customerMapper;
        this.opportunityMapper = opportunityMapper;
        this.orderMapper = orderMapper;
        this.followRecordMapper = followRecordMapper;
    }

    @Override
    public CustomerAiAnalysisVO analyzeCustomer(Long customerId) {
        // 1. Validate customer
        CrmCustomer customer = customerMapper.selectById(customerId);
        if (customer == null || customer.getDeleted() == 1) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "客户不存在");
        }

        // 2. Collect data
        List<CrmOpportunity> opportunities = loadOpportunities(customerId);
        List<CrmOrder> orders = loadOrders(customerId);
        List<CrmFollowRecord> followRecords = loadFollowRecords(customerId);

        // 3. Compute metrics
        CustomerAiMetricsVO metrics = buildMetrics(opportunities, orders, followRecords);

        // 4. Rule-based analysis
        String customerLevel = evaluateLevel(orders, opportunities, metrics);
        String riskLevel = evaluateRisk(followRecords);
        String summary = generateSummary(metrics, opportunities, orders);
        List<String> riskTips = generateRiskTips(metrics, followRecords, opportunities, orders);
        List<String> suggestions = generateSuggestions(customerLevel, opportunities, orders, followRecords);

        // 5. Build response
        CustomerAiAnalysisVO vo = new CustomerAiAnalysisVO();
        vo.setCustomerId(customerId);
        vo.setCustomerName(customer.getCustomerName());
        vo.setCustomerLevel(customerLevel);
        vo.setRiskLevel(riskLevel);
        vo.setSummary(summary);
        vo.setRiskTips(riskTips);
        vo.setSuggestions(suggestions);
        vo.setMetrics(metrics);
        return vo;
    }

    // ========== Data Loading ==========

    private List<CrmOpportunity> loadOpportunities(Long customerId) {
        LambdaQueryWrapper<CrmOpportunity> w = new LambdaQueryWrapper<>();
        w.eq(CrmOpportunity::getCustomerId, customerId)
         .eq(CrmOpportunity::getDeleted, 0);
        return opportunityMapper.selectList(w);
    }

    private List<CrmOrder> loadOrders(Long customerId) {
        LambdaQueryWrapper<CrmOrder> w = new LambdaQueryWrapper<>();
        w.eq(CrmOrder::getCustomerId, customerId)
         .eq(CrmOrder::getDeleted, 0);
        return orderMapper.selectList(w);
    }

    private List<CrmFollowRecord> loadFollowRecords(Long customerId) {
        LambdaQueryWrapper<CrmFollowRecord> w = new LambdaQueryWrapper<>();
        w.eq(CrmFollowRecord::getCustomerId, customerId)
         .eq(CrmFollowRecord::getDeleted, 0)
         .orderByDesc(CrmFollowRecord::getFollowTime);
        return followRecordMapper.selectList(w);
    }

    // ========== Metrics ==========

    private CustomerAiMetricsVO buildMetrics(List<CrmOpportunity> ops,
                                              List<CrmOrder> orders,
                                              List<CrmFollowRecord> follows) {
        CustomerAiMetricsVO m = new CustomerAiMetricsVO();
        m.setOpportunityCount(ops.size());
        m.setOrderCount(orders.size());

        double totalOrderAmount = orders.stream()
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
        m.setTotalOrderAmount(totalOrderAmount);

        // Paid amount: status >= 2 (已付款/已发货/已完成)
        double paidAmount = orders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus() >= 2 && o.getStatus() <= 4)
                .map(o -> o.getAmount() != null ? o.getAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue();
        m.setPaidAmount(paidAmount);

        m.setFollowRecordCount(follows.size());

        if (!follows.isEmpty()) {
            LocalDateTime last = follows.get(0).getFollowTime();
            m.setLastFollowTime(last != null ? last.format(FMT) : null);
        }
        return m;
    }

    // ========== Rule Engine ==========

    /**
     * Evaluate customer value level
     */
    private String evaluateLevel(List<CrmOrder> orders, List<CrmOpportunity> ops, CustomerAiMetricsVO m) {
        if (m.getTotalOrderAmount() >= 100000 || m.getPaidAmount() >= 80000) {
            return "高价值客户";
        }
        if (!orders.isEmpty() || ops.size() >= 2) {
            return "中价值客户";
        }
        return "普通客户";
    }

    /**
     * Evaluate risk level based on last follow-up time
     */
    private String evaluateRisk(List<CrmFollowRecord> follows) {
        if (follows.isEmpty()) return "高风险";

        CrmFollowRecord last = follows.get(0);
        if (last.getFollowTime() == null) return "高风险";

        long days = ChronoUnit.DAYS.between(last.getFollowTime(), LocalDateTime.now());
        if (days > 30) return "高风险";
        if (days > 15) return "中风险";
        return "低风险";
    }

    /**
     * Generate a natural-language summary
     */
    private String generateSummary(CustomerAiMetricsVO m, List<CrmOpportunity> ops, List<CrmOrder> orders) {
        StringBuilder sb = new StringBuilder();
        sb.append("该客户");

        if (m.getOpportunityCount() > 0) {
            sb.append("存在").append(m.getOpportunityCount()).append("个商机");
            long activeOps = ops.stream().filter(o -> !"5".equals(o.getStage()) && !"6".equals(o.getStage())).count();
            if (activeOps > 0) sb.append("（其中").append(activeOps).append("个仍在跟进中）");
            sb.append("，");
        }
        if (m.getOrderCount() > 0) {
            sb.append("历史订单").append(m.getOrderCount()).append("笔");
            if (m.getTotalOrderAmount() > 0) sb.append("，累计金额").append(String.format("%.0f", m.getTotalOrderAmount())).append("元");
            sb.append("，");
        }
        if (m.getFollowRecordCount() > 0) {
            sb.append("共有").append(m.getFollowRecordCount()).append("条跟进记录");
        } else {
            sb.append("暂无跟进记录");
        }

        if (m.getPaidAmount() >= 50000) {
            sb.append("，历史成交金额较高，具备持续维护价值。");
        } else if (m.getPaidAmount() > 0) {
            sb.append("，有一定成交基础，可进一步挖掘需求。");
        } else if (!orders.isEmpty()) {
            sb.append("，已有订单记录，但尚无有效已付款订单，需要关注付款和履约进度。");
        } else {
            sb.append("，暂未产生订单，需要重点推进商机转化。");
        }
        return sb.toString();
    }

    /**
     * Generate risk tips
     */
    private List<String> generateRiskTips(CustomerAiMetricsVO m,
                                           List<CrmFollowRecord> follows,
                                           List<CrmOpportunity> ops,
                                           List<CrmOrder> orders) {
        List<String> tips = new ArrayList<>();

        // Follow-up recency risk
        if (follows.isEmpty()) {
            tips.add("该客户尚无跟进记录，建议尽快安排首次回访。");
        } else {
            CrmFollowRecord last = follows.get(0);
            if (last.getFollowTime() != null) {
                long days = ChronoUnit.DAYS.between(last.getFollowTime(), LocalDateTime.now());
                if (days > 30) {
                    tips.add("最近一次跟进为" + days + "天前，已超过30天，存在客户流失风险。");
                } else if (days > 15) {
                    tips.add("最近一次跟进为" + days + "天前，建议尽快回访。");
                }
            }
        }

        // Conversion risk: has opportunities but no orders
        long activeOpps = ops.stream().filter(o -> !"5".equals(o.getStage()) && !"6".equals(o.getStage())).count();
        if (activeOpps > 0 && orders.isEmpty()) {
            tips.add("存在" + activeOpps + "个未完成商机但尚无订单，需关注商机转化率。");
        }

        // Maintenance risk: high order amount but low follow-up
        if (m.getTotalOrderAmount() >= 50000 && m.getFollowRecordCount() < 3) {
            tips.add("订单金额较高但跟进记录偏少，建议加强客户维护力度。");
        }

        if (tips.isEmpty()) {
            tips.add("当前客户维护情况较稳定，无明显风险信号。");
        }
        return tips;
    }

    /**
     * Generate actionable suggestions
     */
    private List<String> generateSuggestions(String customerLevel,
                                              List<CrmOpportunity> ops,
                                              List<CrmOrder> orders,
                                              List<CrmFollowRecord> follows) {
        List<String> suggestions = new ArrayList<>();

        // High-value customer suggestions
        if ("高价值客户".equals(customerLevel)) {
            suggestions.add("该客户为高价值客户，建议安排定期回访计划，至少每月一次跟进。");
            suggestions.add("可结合历史订单分析客户偏好，推荐增购或续费服务。");
        }

        // Opportunities to close
        long activeOpps = ops.stream().filter(o -> !"5".equals(o.getStage()) && !"6".equals(o.getStage())).count();
        if (activeOpps > 0) {
            if (activeOpps == 1) {
                CrmOpportunity opp = ops.stream().filter(o -> !"5".equals(o.getStage()) && !"6".equals(o.getStage())).findFirst().orElse(null);
                String stageDesc = opp != null ? getStageDesc(opp.getStage()) : "跟进中";
                suggestions.add("当前有1个商机处于「" + stageDesc + "」阶段，建议重点推进至成交。");
            } else {
                suggestions.add("当前有" + activeOpps + "个活跃商机，建议优先级排序后逐一推进。");
            }
        }

        // Follow-up advice
        if (follows.isEmpty()) {
            suggestions.add("建议立即安排首次电话或拜访跟进，建立客户联系。");
        } else if (follows.size() < 3) {
            suggestions.add("建议补充跟进记录，增加客户接触频次以提升客户粘性。");
        }

        // Medium/low value
        if ("中价值客户".equals(customerLevel) || "普通客户".equals(customerLevel)) {
            if (!orders.isEmpty()) {
                suggestions.add("该客户有历史成交记录，建议挖掘潜在复购需求。");
            }
            if (ops.isEmpty()) {
                suggestions.add("建议完善客户需求画像，评估是否存在潜在商机。");
            }
        }

        if (suggestions.isEmpty()) {
            suggestions.add("建议保持现有跟进节奏，关注客户动态变化。");
        }
        return suggestions;
    }

    private String getStageDesc(String stage) {
        switch (stage) {
            case "1": return "初步接触";
            case "2": return "需求确认";
            case "3": return "方案报价";
            case "4": return "商务谈判";
            case "5": return "赢单";
            case "6": return "输单";
            default: return "跟进中";
        }
    }
}
