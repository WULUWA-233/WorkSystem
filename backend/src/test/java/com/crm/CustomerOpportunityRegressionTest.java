package com.crm;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.crm.common.BusinessException;
import com.crm.common.GlobalExceptionHandler;
import com.crm.controller.OpportunityController;
import com.crm.dto.CustomerUpdateDTO;
import com.crm.dto.OpportunityLoseDTO;
import com.crm.dto.OpportunityStageDTO;
import com.crm.dto.OpportunityUpdateDTO;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmOpportunity;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmOpportunityService;
import com.crm.service.impl.CrmCustomerServiceImpl;
import com.crm.service.impl.CrmOpportunityServiceImpl;
import com.crm.utils.LoginUserContext;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CustomerOpportunityRegressionTest {
    private CrmCustomerMapper customers;
    private CrmOpportunityMapper opportunities;
    private CrmCustomerServiceImpl customerService;
    private CrmOpportunityServiceImpl opportunityService;

    @BeforeEach
    void setup() {
        customers = mock(CrmCustomerMapper.class);
        opportunities = mock(CrmOpportunityMapper.class);
        SysUserMapper users = mock(SysUserMapper.class);
        customerService = new CrmCustomerServiceImpl(customers, users);
        opportunityService = new CrmOpportunityServiceImpl(opportunities, customers, users);
        LoginUserContext.setCurrentUser(91L, "test-user", Collections.singletonList("ADMIN"));
        MapperBuilderAssistant assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "customer-opportunity-test");
        TableInfoHelper.initTableInfo(assistant, CrmCustomer.class);
        TableInfoHelper.initTableInfo(assistant, CrmOpportunity.class);
        when(customers.update(isNull(), any())).thenReturn(1);
        when(opportunities.update(isNull(), any())).thenReturn(1);
    }

    @AfterEach
    void cleanup() { LoginUserContext.clear(); }

    @Test
    void winningOpportunityAcceptsNoRequestBody() throws Exception {
        CrmOpportunityService controllerService = mock(CrmOpportunityService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OpportunityController(controllerService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(patch("/api/v1/sales/opportunities/42/win")).andExpect(status().isOk());
        verify(controllerService).markAsWin(42L, null);
    }

    @Test
    void losingOpportunityRequiresReason() throws Exception {
        CrmOpportunityService controllerService = mock(CrmOpportunityService.class);
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new OpportunityController(controllerService))
                .setControllerAdvice(new GlobalExceptionHandler()).build();
        mvc.perform(patch("/api/v1/sales/opportunities/42/lose")
                .contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
        verifyNoInteractions(controllerService);
    }

    @Test
    void customerPatchKeepsFollowUpMetadataAndCanClearSchedule() {
        CrmCustomer customer = customer();
        customer.setLastFollowTime(LocalDateTime.of(2026, 10, 1, 12, 0));
        when(customers.selectById(7L)).thenReturn(customer);
        CustomerUpdateDTO request = new CustomerUpdateDTO();
        request.setRemark("Updated note");
        request.setNextFollowTime("");
        customerService.updateCustomer(7L, request);
        LambdaUpdateWrapper<?> update = customerUpdate();
        assertFalse(update.getSqlSet().contains("last_follow_time"));
        assertFalse(update.getSqlSet().contains("status"));
        assertFalse(update.getSqlSet().contains("customer_name"));
        assertNull(value(update, update.getSqlSet(), "next_follow_time"));
        assertEquals(0, value(update, update.getSqlSegment(), "deleted"));
        verify(customers, never()).updateById(any());
    }

    @Test
    void customerWithChildrenCannotBeDeleted() {
        when(customers.selectByIdForUpdate(7L)).thenReturn(customer());
        when(customers.countActiveRelations(7L)).thenReturn(1L);
        assertThrows(BusinessException.class, () -> customerService.deleteCustomer(7L));
        verify(customers, never()).update(any(), any());
    }

    @Test
    void customerDeletionLocksParentAndRechecksAllRelationsAtomically() {
        when(customers.selectByIdForUpdate(7L)).thenReturn(customer());
        customerService.deleteCustomer(7L);
        InOrder locks = inOrder(customers);
        locks.verify(customers).selectByIdForUpdate(7L);
        locks.verify(customers).countActiveRelations(7L);
        locks.verify(customers).update(isNull(), any());
        String predicate = customerUpdate().getSqlSegment();
        assertTrue(predicate.contains("crm_opportunity"));
        assertTrue(predicate.contains("crm_order"));
        assertTrue(predicate.contains("crm_follow_record"));
        assertEquals(0, value(customerUpdate(), predicate, "deleted"));
    }

    @Test
    void deletedCustomerCannotReceiveAnUpdate() {
        when(customers.selectById(7L)).thenReturn(customer());
        when(customers.update(isNull(), any())).thenReturn(0);
        CustomerUpdateDTO request = new CustomerUpdateDTO();
        request.setRemark("note");
        assertThrows(BusinessException.class, () -> customerService.updateCustomer(7L, request));
    }

    @Test
    void opportunityEditDoesNotOverwriteConcurrentFollowUpOrStage() {
        CrmOpportunity opportunity = opportunity("3");
        opportunity.setLastFollowTime(LocalDateTime.of(2026, 10, 1, 12, 0));
        opportunity.setExpectedDate(LocalDate.of(2026, 10, 31));
        when(opportunities.selectById(42L)).thenReturn(opportunity);
        OpportunityUpdateDTO request = new OpportunityUpdateDTO();
        request.setExpectedDate("");
        request.setNextFollowTime("");
        request.setRemark("Updated note");
        opportunityService.updateOpportunity(42L, request);
        LambdaUpdateWrapper<?> update = opportunityUpdate();
        assertFalse(update.getSqlSet().contains("stage"));
        assertFalse(update.getSqlSet().contains("last_follow_time"));
        assertFalse(update.getSqlSet().contains("win_time"));
        assertEquals("3", value(update, update.getSqlSegment(), "stage"));
        assertEquals(0, value(update, update.getSqlSegment(), "deleted"));
        assertNull(value(update, update.getSqlSet(), "expected_date"));
        assertNull(value(update, update.getSqlSet(), "next_follow_time"));
        verify(opportunities, never()).updateById(any());
    }

    @Test
    void editCannotReopenConcurrentlyWonOpportunity() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("3"));
        when(opportunities.update(isNull(), any())).thenReturn(0);
        OpportunityUpdateDTO request = new OpportunityUpdateDTO();
        request.setRemark("note");
        assertThrows(BusinessException.class, () -> opportunityService.updateOpportunity(42L, request));
        assertEquals("3", value(opportunityUpdate(), opportunityUpdate().getSqlSegment(), "stage"));
    }

    @Test
    void winningOpportunityWritesTimesAndGuardsPreviousStage() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("4"));
        opportunityService.markAsWin(42L, null);
        LambdaUpdateWrapper<?> update = opportunityUpdate();
        assertEquals("4", value(update, update.getSqlSegment(), "stage"));
        assertEquals("5", value(update, update.getSqlSet(), "stage"));
        assertEquals(100, value(update, update.getSqlSet(), "probability"));
        assertInstanceOf(LocalDateTime.class, value(update, update.getSqlSet(), "win_time"));
        assertInstanceOf(LocalDateTime.class, value(update, update.getSqlSet(), "close_time"));
    }

    @Test
    void stageAdvanceCannotOverwriteConcurrentClosure() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("3"));
        when(opportunities.update(isNull(), any())).thenReturn(0);
        OpportunityStageDTO request = new OpportunityStageDTO();
        request.setStage(4);
        assertThrows(BusinessException.class, () -> opportunityService.updateStage(42L, request));
        assertEquals("3", value(opportunityUpdate(), opportunityUpdate().getSqlSegment(), "stage"));
    }

    @Test
    void lostOpportunityCannotBecomeWon() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("6"));
        assertThrows(BusinessException.class, () -> opportunityService.markAsWin(42L, null));
        verify(opportunities, never()).update(any(), any());
    }

    @Test
    void losingOpportunityChecksReasonEvenOutsideHttpValidation() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("3"));
        OpportunityLoseDTO request = new OpportunityLoseDTO();
        request.setLoseReason("   ");
        assertThrows(BusinessException.class, () -> opportunityService.markAsLose(42L, request));
        verify(opportunities, never()).update(any(), any());
    }

    @Test
    void opportunityWithChildrenCannotBeDeleted() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("5"));
        when(customers.selectByIdForUpdate(7L)).thenReturn(customer());
        when(opportunities.selectByIdForUpdate(42L)).thenReturn(opportunity("5"));
        when(opportunities.countActiveRelations(42L)).thenReturn(1L);
        assertThrows(BusinessException.class, () -> opportunityService.deleteOpportunity(42L));
        verify(opportunities, never()).update(any(), any());
    }

    @Test
    void opportunityDeletionUsesCustomerThenOpportunityLockOrder() {
        when(opportunities.selectById(42L)).thenReturn(opportunity("3"));
        when(customers.selectByIdForUpdate(7L)).thenReturn(customer());
        when(opportunities.selectByIdForUpdate(42L)).thenReturn(opportunity("3"));
        opportunityService.deleteOpportunity(42L);
        InOrder locks = inOrder(customers, opportunities);
        locks.verify(opportunities).selectById(42L);
        locks.verify(customers).selectByIdForUpdate(7L);
        locks.verify(opportunities).selectByIdForUpdate(42L);
        locks.verify(opportunities).countActiveRelations(42L);
        locks.verify(opportunities).update(isNull(), any());
        LambdaUpdateWrapper<?> update = opportunityUpdate();
        assertEquals("3", value(update, update.getSqlSegment(), "stage"));
        assertTrue(update.getSqlSegment().contains("crm_order"));
        assertTrue(update.getSqlSegment().contains("crm_follow_record"));
    }

    private CrmCustomer customer() {
        CrmCustomer customer = new CrmCustomer();
        customer.setId(7L);
        customer.setDeleted(0);
        customer.setStatus(1);
        customer.setCustomerName("Customer");
        return customer;
    }

    private CrmOpportunity opportunity(String stage) {
        CrmOpportunity opportunity = new CrmOpportunity();
        opportunity.setId(42L);
        opportunity.setCustomerId(7L);
        opportunity.setStage(stage);
        opportunity.setDeleted(0);
        return opportunity;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaUpdateWrapper<?> customerUpdate() {
        ArgumentCaptor<Wrapper> capture = ArgumentCaptor.forClass(Wrapper.class);
        verify(customers).update(isNull(), capture.capture());
        return (LambdaUpdateWrapper<?>) capture.getValue();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private LambdaUpdateWrapper<?> opportunityUpdate() {
        ArgumentCaptor<Wrapper> capture = ArgumentCaptor.forClass(Wrapper.class);
        verify(opportunities).update(isNull(), capture.capture());
        return (LambdaUpdateWrapper<?>) capture.getValue();
    }

    private Object value(LambdaUpdateWrapper<?> update, String sql, String column) {
        Matcher match = Pattern.compile("(?<![a-zA-Z_])" + column
                + "\\s*=\\s*#\\{ew\\.paramNameValuePairs\\.(MPGENVAL\\d+)\\}").matcher(sql);
        assertTrue(match.find(), "Missing SQL binding for " + column + ": " + sql);
        return update.getParamNameValuePairs().get(match.group(1));
    }
}
