package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.OperationLogQueryDTO;
import com.crm.vo.OperationLogDetailVO;
import com.crm.vo.OperationLogVO;

public interface SysOperationLogService {
    PageResult<OperationLogVO> listLogs(OperationLogQueryDTO query);
    OperationLogDetailVO getLogDetail(Long id);
    void deleteLog(Long id);
}
