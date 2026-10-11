package com.crm.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.crm.common.BusinessException;
import com.crm.dto.FollowRecordCreateDTO;
import com.crm.entity.*;
import com.crm.mapper.*;
import com.crm.service.impl.CrmFollowRecordServiceImpl;
import com.crm.utils.LoginUserContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class FollowRecordRegressionTest {
    private CrmFollowRecordMapper followMapper;
    private CrmCustomerMapper customerMapper;
    private CrmOpportunityMapper opportunityMapper;
    private CrmFollowRecordServiceImpl service;

    @BeforeEach void setup() {
        followMapper = mock(CrmFollowRecordMapper.class);
        customerMapper = mock(CrmCustomerMapper.class);
        opportunityMapper = mock(CrmOpportunityMapper.class);
        SysUserMapper userMapper = mock(SysUserMapper.class);
        service = new CrmFollowRecordServiceImpl(followMapper, customerMapper, opportunityMapper, userMapper);
        CrmCustomer customer = new CrmCustomer(); customer.setId(1L); customer.setDeleted(0);
        CrmOpportunity opportunity = new CrmOpportunity(); opportunity.setId(2L); opportunity.setCustomerId(1L); opportunity.setDeleted(0);
        SysUser user = new SysUser(); user.setId(3L); user.setDeleted(0); user.setStatus(1);
        when(customerMapper.selectByIdForUpdate(1L)).thenReturn(customer);
        when(opportunityMapper.selectByIdForUpdate(2L)).thenReturn(opportunity);
        when(userMapper.selectById(3L)).thenReturn(user);
        LoginUserContext.setCurrentUser(3L, "sales", Collections.singletonList("SALES"));
        for (Class<?> type : new Class<?>[]{CrmCustomer.class, CrmOpportunity.class, CrmFollowRecord.class}) {
            TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), "follow-test"), type);
        }
    }

    @AfterEach void cleanup() { LoginUserContext.clear(); }

    @Test void historicalFollowDoesNotUnconditionallyRewindParentFollowTimes() {
        FollowRecordCreateDTO dto = validDto();
        dto.setFollowTime("2024-01-01 10:00:00");
        dto.setNextFollowTime("2024-01-05 10:00:00");
        service.createFollowRecord(dto);
        ArgumentCaptor<CrmFollowRecord> record = ArgumentCaptor.forClass(CrmFollowRecord.class);
        verify(followMapper).insert(record.capture());
        assertEquals(LocalDateTime.of(2024, 1, 1, 10, 0), record.getValue().getFollowTime());
        ArgumentCaptor<LambdaUpdateWrapper<CrmCustomer>> customer = wrapperCaptor();
        verify(customerMapper).update(isNull(), customer.capture());
        assertTrue(customer.getValue().getSqlSegment().contains("last_follow_time IS NULL"));
        assertTrue(customer.getValue().getSqlSegment().contains("last_follow_time <="));
        ArgumentCaptor<LambdaUpdateWrapper<CrmOpportunity>> opportunity = wrapperCaptor();
        verify(opportunityMapper).update(isNull(), opportunity.capture());
        assertTrue(opportunity.getValue().getSqlSegment().contains("last_follow_time <="));
        // All child creation uses the same customer -> opportunity lock order.
        org.mockito.InOrder locks = inOrder(customerMapper, opportunityMapper);
        locks.verify(customerMapper).selectByIdForUpdate(1L);
        locks.verify(opportunityMapper).selectByIdForUpdate(2L);
    }

    @Test void invalidFollowTypeAndBlankContentNeverReachInsert() {
        FollowRecordCreateDTO dto = validDto();
        dto.setFollowType("鐢佃瘽");
        assertThrows(BusinessException.class, () -> service.createFollowRecord(dto));
        dto.setFollowType("电话"); dto.setFollowContent("   ");
        assertThrows(BusinessException.class, () -> service.createFollowRecord(dto));
        verifyNoInteractions(followMapper, customerMapper, opportunityMapper);
    }

    @Test void nextFollowCannotPrecedeActualFollow() {
        FollowRecordCreateDTO dto = validDto();
        dto.setFollowTime("2024-01-05 10:00:00"); dto.setNextFollowTime("2024-01-01 10:00:00");
        assertThrows(BusinessException.class, () -> service.createFollowRecord(dto));
        verify(followMapper, never()).insert(any());
    }

    @Test void removingLatestFollowRestoresRemainingRecordTime() {
        CrmFollowRecord deleted = record(10L, LocalDateTime.of(2024, 2, 1, 10, 0));
        CrmFollowRecord remaining = record(9L, LocalDateTime.of(2024, 1, 1, 10, 0));
        when(followMapper.selectById(10L)).thenReturn(deleted);
        when(followMapper.update(isNull(), any())).thenReturn(1);
        when(followMapper.selectOne(any())).thenReturn(remaining);
        service.deleteFollowRecord(10L);
        ArgumentCaptor<LambdaUpdateWrapper<CrmCustomer>> customer = wrapperCaptor();
        verify(customerMapper).update(isNull(), customer.capture());
        String sql = customer.getValue().getSqlSegment();
        assertTrue(sql.contains("last_follow_time ="));
        assertTrue(customer.getValue().getParamNameValuePairs().containsValue(deleted.getFollowTime()));
        assertTrue(customer.getValue().getParamNameValuePairs().containsValue(remaining.getFollowTime()));
    }

    @Test void concurrentDeleteDoesNotReportSuccessOrRewriteParents() {
        when(followMapper.selectById(10L)).thenReturn(record(10L, LocalDateTime.of(2024, 2, 1, 10, 0)));
        when(followMapper.update(isNull(), any())).thenReturn(0);
        assertThrows(BusinessException.class, () -> service.deleteFollowRecord(10L));
        verify(customerMapper, never()).update(any(), any());
        verify(opportunityMapper, never()).update(any(), any());
    }

    private FollowRecordCreateDTO validDto() {
        FollowRecordCreateDTO dto = new FollowRecordCreateDTO();
        dto.setCustomerId(1L); dto.setOpportunityId(2L); dto.setFollowerId(3L);
        dto.setFollowType("电话"); dto.setFollowContent("确认需求");
        return dto;
    }

    private CrmFollowRecord record(Long id, LocalDateTime time) {
        CrmFollowRecord record = new CrmFollowRecord();
        record.setId(id); record.setCustomerId(1L); record.setOpportunityId(2L);
        record.setDeleted(0); record.setFollowTime(time); record.setNextFollowTime(time.plusDays(3));
        return record;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private <T> ArgumentCaptor<LambdaUpdateWrapper<T>> wrapperCaptor() {
        return (ArgumentCaptor) ArgumentCaptor.forClass(LambdaUpdateWrapper.class);
    }
}

