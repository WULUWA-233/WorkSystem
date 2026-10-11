package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.FollowRecordCreateDTO;
import com.crm.dto.FollowRecordQueryDTO;
import com.crm.vo.FollowRecordDetailVO;
import com.crm.vo.FollowRecordVO;

public interface CrmFollowRecordService {
    PageResult<FollowRecordVO> listFollowRecords(FollowRecordQueryDTO query);
    void createFollowRecord(FollowRecordCreateDTO dto);
    FollowRecordDetailVO getFollowRecordDetail(Long id);
    void deleteFollowRecord(Long id);
    PageResult<FollowRecordVO> listByCustomer(Long customerId, long page, long size);
    PageResult<FollowRecordVO> listByOpportunity(Long opportunityId, long page, long size);
}
