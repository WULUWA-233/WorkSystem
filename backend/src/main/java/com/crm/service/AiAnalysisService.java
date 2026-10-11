package com.crm.service;

import com.crm.vo.CustomerAiAnalysisVO;

/**
 * AI customer analysis service
 */
public interface AiAnalysisService {

    /**
     * Analyze a customer based on existing CRM data.
     *
     * @param customerId target customer ID
     * @return AI analysis result
     */
    CustomerAiAnalysisVO analyzeCustomer(Long customerId);
}
