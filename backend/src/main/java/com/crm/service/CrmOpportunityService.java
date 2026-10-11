package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.*;
import com.crm.vo.OpportunityDetailVO;
import com.crm.vo.OpportunityVO;

import java.util.List;

public interface CrmOpportunityService {

    PageResult<OpportunityVO> listOpportunities(OpportunityQueryDTO query);

    OpportunityDetailVO getOpportunityDetail(Long id);

    void createOpportunity(OpportunityCreateDTO dto);

    void updateOpportunity(Long id, OpportunityUpdateDTO dto);

    void deleteOpportunity(Long id);

    void updateStage(Long id, OpportunityStageDTO dto);

    void markAsWin(Long id, OpportunityLoseDTO dto);

    void markAsLose(Long id, OpportunityLoseDTO dto);

    List<OpportunityVO> listByCustomer(Long customerId);
}
