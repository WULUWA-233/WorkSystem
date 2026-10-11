package com.crm.vo;

import java.io.Serializable;
import java.util.List;

/**
 * AI Customer Analysis result
 */
public class CustomerAiAnalysisVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long customerId;
    private String customerName;
    private String customerLevel;
    private String riskLevel;
    private String summary;
    private List<String> riskTips;
    private List<String> suggestions;
    private CustomerAiMetricsVO metrics;

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public String getCustomerLevel() { return customerLevel; }
    public void setCustomerLevel(String customerLevel) { this.customerLevel = customerLevel; }
    public String getRiskLevel() { return riskLevel; }
    public void setRiskLevel(String riskLevel) { this.riskLevel = riskLevel; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public List<String> getRiskTips() { return riskTips; }
    public void setRiskTips(List<String> riskTips) { this.riskTips = riskTips; }
    public List<String> getSuggestions() { return suggestions; }
    public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }
    public CustomerAiMetricsVO getMetrics() { return metrics; }
    public void setMetrics(CustomerAiMetricsVO metrics) { this.metrics = metrics; }
}
