package com.crm.controller;

import com.crm.common.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check Controller
 *
 * @author CRM Team
 */
@Api(tags = "Health Check")
@RestController
@RequestMapping("/api/v1/common")
public class HealthController {

    @ApiOperation("Health Check")
    @GetMapping("/health")
    public Result<String> health() {
        return Result.success("ok");
    }
}
