package com.crm.dto;

import com.crm.common.PageQuery;

public class OperationLogQueryDTO extends PageQuery {
    private String username;
    private String moduleName;
    private String operationType;
    private String requestMethod;
    private Integer status;
    private String startTime;
    private String endTime;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getModuleName() { return moduleName; }
    public void setModuleName(String moduleName) { this.moduleName = moduleName; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getRequestMethod() { return requestMethod; }
    public void setRequestMethod(String requestMethod) { this.requestMethod = requestMethod; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
}
