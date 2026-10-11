package com.crm.controller;

import com.crm.common.Result;
import com.crm.service.AiAnalysisService;
import com.crm.vo.CustomerAiAnalysisVO;
import org.springframework.web.bind.annotation.*;

/**
 * AI Customer Analysis Controller
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiAnalysisController {

    private final AiAnalysisService aiAnalysisService;

    public AiAnalysisController(AiAnalysisService aiAnalysisService) {
        this.aiAnalysisService = aiAnalysisService;
    }

    /**
     * Analyze a customer using AI rule engine
     */
    @GetMapping("/customers/{customerId}/analysis")
    public Result<CustomerAiAnalysisVO> analyzeCustomer(@PathVariable Long customerId) {
        CustomerAiAnalysisVO analysis = aiAnalysisService.analyzeCustomer(customerId);
        return Result.success(analysis);
    }
}
