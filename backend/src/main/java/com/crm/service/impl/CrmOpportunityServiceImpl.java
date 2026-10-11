package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.*;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmOpportunity;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmOpportunityService;
import com.crm.utils.DateParseUtils;
import com.crm.utils.LoginUserContext;
import com.crm.vo.OpportunityDetailVO;
import com.crm.vo.OpportunityVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CrmOpportunityServiceImpl implements CrmOpportunityService {

    private static final Logger log = LoggerFactory.getLogger(CrmOpportunityServiceImpl.class);

    private static final Map<Integer, String> STAGE_NAMES = new LinkedHashMap<>();
    static {
        STAGE_NAMES.put(1, "Initial Contact");
        STAGE_NAMES.put(2, "Requirements Confirmed");
        STAGE_NAMES.put(3, "Proposal/Quotation");
        STAGE_NAMES.put(4, "Negotiation");
        STAGE_NAMES.put(5, "Won");
        STAGE_NAMES.put(6, "Lost");
    }

    private final CrmOpportunityMapper opportunityMapper;
    private final CrmCustomerMapper customerMapper;
    private final SysUserMapper sysUserMapper;

    public CrmOpportunityServiceImpl(CrmOpportunityMapper opportunityMapper,
                                     CrmCustomerMapper customerMapper,
                                     SysUserMapper sysUserMapper) {
        this.opportunityMapper = opportunityMapper;
        this.customerMapper = customerMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<OpportunityVO> listOpportunities(OpportunityQueryDTO query) {
        LambdaQueryWrapper<CrmOpportunity> w = new LambdaQueryWrapper<>();
        w.eq(CrmOpportunity::getDeleted, 0);

        if (query.getCustomerId() != null) w.eq(CrmOpportunity::getCustomerId, query.getCustomerId());
        if (notEmpty(query.getOpportunityName())) w.like(CrmOpportunity::getOpportunityName, query.getOpportunityName());
        if (query.getStage() != null) w.eq(CrmOpportunity::getStage, String.valueOf(query.getStage()));
        if (query.getOwnerId() != null) w.eq(CrmOpportunity::getOwnerId, query.getOwnerId());
        if (query.getMinAmount() != null) w.ge(CrmOpportunity::getAmount, query.getMinAmount());
        if (query.getMaxAmount() != null) w.le(CrmOpportunity::getAmount, query.getMaxAmount());
        if (notEmpty(query.getExpectedStartDate())) {
            w.ge(CrmOpportunity::getExpectedDate, DateParseUtils.parseDate(query.getExpectedStartDate(), "expectedStartDate"));
        }
        if (notEmpty(query.getExpectedEndDate())) {
            w.le(CrmOpportunity::getExpectedDate, DateParseUtils.parseDate(query.getExpectedEndDate(), "expectedEndDate"));
        }
        w.orderByDesc(CrmOpportunity::getId);

        Page<CrmOpportunity> page = new Page<>(query.getPage(), query.getSize());
        Page<CrmOpportunity> result = opportunityMapper.selectPage(page, w);

        List<OpportunityVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public OpportunityDetailVO getOpportunityDetail(Long id) {
        CrmOpportunity o = getOrThrow(id);
        return toDetailVO(o);
    }

    @Override
    @Transactional
    public void createOpportunity(OpportunityCreateDTO dto) {
        // Validate customer
        CrmCustomer customer = customerMapper.selectByIdForUpdate(dto.getCustomerId());
        if (customer == null || Integer.valueOf(1).equals(customer.getDeleted()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Customer not found");

        // Validate stage (only 1-4 allowed on create)
        int stage = dto.getStage() != null ? dto.getStage() : 1;
        if (stage < 1 || stage > 4)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Stage must be 1-4 on create (use /win or /lose for 5/6)");
        if (stage == 5 || stage == 6)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot create opportunity with stage 5 or 6");

        // Validate amount
        if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Amount cannot be negative");

        // Validate probability
        int prob = dto.getProbability() != null ? dto.getProbability() : 0;
        if (prob < 0 || prob > 100) throw new BusinessException(ResultCode.PARAM_ERROR, "Probability must be 0-100");

        // Validate owner
        validateOwner(dto.getOwnerId());

        Long uid = LoginUserContext.getCurrentUserId();
        CrmOpportunity o = new CrmOpportunity();
        o.setCustomerId(dto.getCustomerId());
        o.setOpportunityName(dto.getOpportunityName().trim());
        o.setAmount(dto.getAmount() != null ? dto.getAmount() : BigDecimal.ZERO);
        o.setStage(String.valueOf(stage));
        o.setProbability(prob);
        if (notEmpty(dto.getExpectedDate())) {
            o.setExpectedDate(DateParseUtils.parseDate(dto.getExpectedDate(), "expectedDate"));
        }
        o.setOwnerId(dto.getOwnerId());
        o.setSource(dto.getSource());
        if (notEmpty(dto.getNextFollowTime())) {
            o.setNextFollowTime(DateParseUtils.parseDateTime(dto.getNextFollowTime(), "nextFollowTime"));
        }
        o.setCreatedBy(uid);
        o.setUpdatedBy(uid);
        o.setRemark(dto.getRemark());
        opportunityMapper.insert(o);
        log.info("Opportunity created: id={}, name={}", o.getId(), o.getOpportunityName());
    }

    @Override
    public void updateOpportunity(Long id, OpportunityUpdateDTO dto) {
        CrmOpportunity o = getOrThrow(id);

        // Closed opportunities cannot be modified via this endpoint
        int currentStage = parseStage(o.getStage());
        if (currentStage < 1 || currentStage > 4)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot modify a closed opportunity (won/lost)");

        if (dto.getAmount() != null && dto.getAmount().compareTo(BigDecimal.ZERO) < 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Amount cannot be negative");
        if (dto.getProbability() != null && (dto.getProbability() < 0 || dto.getProbability() > 100))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Probability must be 0-100");
        if (dto.getOwnerId() != null) validateOwner(dto.getOwnerId());

        LambdaUpdateWrapper<CrmOpportunity> w = updateForStage(o);
        if (dto.getOpportunityName() != null) {
            if (!notEmpty(dto.getOpportunityName()))
                throw new BusinessException(ResultCode.PARAM_ERROR, "Opportunity name cannot be blank");
            w.set(CrmOpportunity::getOpportunityName, dto.getOpportunityName().trim());
        }
        if (dto.getAmount() != null) w.set(CrmOpportunity::getAmount, dto.getAmount());
        if (dto.getProbability() != null) w.set(CrmOpportunity::getProbability, dto.getProbability());
        if (dto.getExpectedDate() != null) {
            w.set(CrmOpportunity::getExpectedDate, notEmpty(dto.getExpectedDate())
                    ? DateParseUtils.parseDate(dto.getExpectedDate(), "expectedDate") : null);
        }
        if (dto.getOwnerId() != null) w.set(CrmOpportunity::getOwnerId, dto.getOwnerId());
        if (dto.getSource() != null) w.set(CrmOpportunity::getSource, dto.getSource());
        if (dto.getNextFollowTime() != null) {
            w.set(CrmOpportunity::getNextFollowTime, notEmpty(dto.getNextFollowTime())
                    ? DateParseUtils.parseDateTime(dto.getNextFollowTime(), "nextFollowTime") : null);
        }
        if (dto.getRemark() != null) w.set(CrmOpportunity::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Opportunity updated: id={}", id);
    }

    @Override
    @Transactional
    public void deleteOpportunity(Long id) {
        CrmOpportunity snapshot = getOrThrow(id);
        // Keep the same lock order as creation of orders and follow-up records.
        if (snapshot.getCustomerId() != null) customerMapper.selectByIdForUpdate(snapshot.getCustomerId());
        CrmOpportunity o = opportunityMapper.selectByIdForUpdate(id);
        if (o == null || Integer.valueOf(1).equals(o.getDeleted()))
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Opportunity not found");
        if (opportunityMapper.countActiveRelations(id) > 0)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot delete an opportunity with orders or follow-up records");
        LambdaUpdateWrapper<CrmOpportunity> w = updateForStage(o);
        w.apply("NOT EXISTS (SELECT 1 FROM crm_order WHERE opportunity_id = {0} AND deleted = 0)", id)
         .apply("NOT EXISTS (SELECT 1 FROM crm_follow_record WHERE opportunity_id = {0} AND deleted = 0)", id)
         .set(CrmOpportunity::getDeleted, 1);
        updateOrThrow(w);
        log.info("Opportunity deleted: id={}, name={}", id, o.getOpportunityName());
    }

    @Override
    public void updateStage(Long id, OpportunityStageDTO dto) {
        CrmOpportunity o = getOrThrow(id);
        int curr = parseStage(o.getStage());
        if (curr < 1 || curr > 4)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot advance a closed opportunity (won/lost)");

        if (dto == null || dto.getStage() == null)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Stage cannot be null");
        int newStage = dto.getStage();
        if (newStage < 1 || newStage > 4)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Stage must be 1-4; use /win or /lose for 5/6");
        if (newStage == 5 || newStage == 6)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Cannot set stage to 5/6 via this endpoint; use /win or /lose");

        if (dto.getProbability() != null && (dto.getProbability() < 0 || dto.getProbability() > 100))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Probability must be 0-100");

        LambdaUpdateWrapper<CrmOpportunity> w = updateForStage(o);
        w.set(CrmOpportunity::getStage, String.valueOf(newStage));
        if (dto.getProbability() != null) w.set(CrmOpportunity::getProbability, dto.getProbability());
        if (notEmpty(dto.getRemark())) w.set(CrmOpportunity::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Opportunity stage advanced: id={}, stage {}->{}", id, curr, newStage);
    }

    @Override
    public void markAsWin(Long id, OpportunityLoseDTO dto) {
        CrmOpportunity o = getOrThrow(id);
        int curr = parseStage(o.getStage());
        if (curr == 5) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity already won");
        if (curr == 6) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity already lost, cannot win");
        if (curr < 1 || curr > 4) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity stage is invalid");
        if (dto != null && dto.getRemark() != null && dto.getRemark().length() > 500)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Remark must not exceed 500 characters");

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<CrmOpportunity> w = updateForStage(o);
        w.set(CrmOpportunity::getStage, "5")
         .set(CrmOpportunity::getProbability, 100)
         .set(CrmOpportunity::getWinTime, now)
         .set(CrmOpportunity::getCloseTime, now);
        if (dto != null && notEmpty(dto.getRemark())) w.set(CrmOpportunity::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Opportunity won: id={}", id);
    }

    @Override
    public void markAsLose(Long id, OpportunityLoseDTO dto) {
        CrmOpportunity o = getOrThrow(id);
        int curr = parseStage(o.getStage());
        if (curr == 6) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity already lost");
        if (curr == 5) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity already won, cannot lose");
        if (curr < 1 || curr > 4) throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity stage is invalid");
        if (dto == null || !notEmpty(dto.getLoseReason()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Loss reason cannot be empty");

        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<CrmOpportunity> w = updateForStage(o);
        w.set(CrmOpportunity::getStage, "6")
         .set(CrmOpportunity::getProbability, 0)
         .set(CrmOpportunity::getLoseReason, dto.getLoseReason().trim())
         .set(CrmOpportunity::getCloseTime, now);
        if (notEmpty(dto.getRemark())) w.set(CrmOpportunity::getRemark, dto.getRemark());
        updateOrThrow(w);
        log.info("Opportunity lost: id={}", id);
    }

    @Override
    public List<OpportunityVO> listByCustomer(Long customerId) {
        CrmCustomer c = customerMapper.selectById(customerId);
        if (c == null || c.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Customer not found");

        LambdaQueryWrapper<CrmOpportunity> w = new LambdaQueryWrapper<>();
        w.eq(CrmOpportunity::getCustomerId, customerId)
         .eq(CrmOpportunity::getDeleted, 0)
         .orderByDesc(CrmOpportunity::getId);
        return opportunityMapper.selectList(w).stream().map(this::toVO).collect(Collectors.toList());
    }

    // ========== Private helpers ==========

    private LambdaUpdateWrapper<CrmOpportunity> updateForStage(CrmOpportunity opportunity) {
        LambdaUpdateWrapper<CrmOpportunity> w = new LambdaUpdateWrapper<>();
        w.eq(CrmOpportunity::getId, opportunity.getId())
         .eq(CrmOpportunity::getDeleted, 0)
         .eq(CrmOpportunity::getStage, opportunity.getStage())
         .set(CrmOpportunity::getUpdatedBy, LoginUserContext.getCurrentUserId())
         .set(CrmOpportunity::getUpdatedAt, LocalDateTime.now());
        return w;
    }

    private void updateOrThrow(LambdaUpdateWrapper<CrmOpportunity> wrapper) {
        if (opportunityMapper.update(null, wrapper) != 1)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Opportunity changed, was deleted or has related records; refresh and try again");
    }

    private CrmOpportunity getOrThrow(Long id) {
        CrmOpportunity o = opportunityMapper.selectById(id);
        if (o == null || Integer.valueOf(1).equals(o.getDeleted()))
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Opportunity not found");
        return o;
    }

    private void validateOwner(Long ownerId) {
        SysUser u = sysUserMapper.selectById(ownerId);
        if (u == null || u.getDeleted() == 1)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user not found");
        if (u.getStatus() == null || u.getStatus() == 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user is disabled");
    }

    private int parseStage(String s) {
        try { return Integer.parseInt(s); } catch (Exception e) { return 0; }
    }

    private String getStageName(String s) {
        int stage = parseStage(s);
        return STAGE_NAMES.getOrDefault(stage, "Unknown");
    }

    private String getCustomerName(Long customerId) {
        if (customerId == null) return null;
        CrmCustomer c = customerMapper.selectById(customerId);
        return c != null ? c.getCustomerName() : null;
    }

    private String getOwnerName(Long ownerId) {
        if (ownerId == null) return null;
        SysUser u = sysUserMapper.selectById(ownerId);
        return u != null ? u.getRealName() : null;
    }

    private OpportunityVO toVO(CrmOpportunity o) {
        OpportunityVO vo = new OpportunityVO();
        vo.setId(o.getId());
        vo.setCustomerId(o.getCustomerId());
        vo.setCustomerName(getCustomerName(o.getCustomerId()));
        vo.setOpportunityName(o.getOpportunityName());
        vo.setAmount(o.getAmount());
        vo.setStage(parseStage(o.getStage()));
        vo.setStageName(getStageName(o.getStage()));
        vo.setProbability(o.getProbability());
        vo.setExpectedDate(o.getExpectedDate());
        vo.setOwnerId(o.getOwnerId());
        vo.setOwnerName(getOwnerName(o.getOwnerId()));
        vo.setSource(o.getSource());
        vo.setLastFollowTime(o.getLastFollowTime());
        vo.setNextFollowTime(o.getNextFollowTime());
        vo.setWinTime(o.getWinTime());
        vo.setCloseTime(o.getCloseTime());
        vo.setCreatedAt(o.getCreatedAt());
        return vo;
    }

    private OpportunityDetailVO toDetailVO(CrmOpportunity o) {
        OpportunityDetailVO vo = new OpportunityDetailVO();
        vo.setId(o.getId());
        vo.setCustomerId(o.getCustomerId());
        vo.setCustomerName(getCustomerName(o.getCustomerId()));
        vo.setOpportunityName(o.getOpportunityName());
        vo.setAmount(o.getAmount());
        vo.setStage(parseStage(o.getStage()));
        vo.setStageName(getStageName(o.getStage()));
        vo.setProbability(o.getProbability());
        vo.setExpectedDate(o.getExpectedDate());
        vo.setOwnerId(o.getOwnerId());
        vo.setOwnerName(getOwnerName(o.getOwnerId()));
        vo.setSource(o.getSource());
        vo.setLoseReason(o.getLoseReason());
        vo.setWinTime(o.getWinTime());
        vo.setCloseTime(o.getCloseTime());
        vo.setLastFollowTime(o.getLastFollowTime());
        vo.setNextFollowTime(o.getNextFollowTime());
        vo.setCreatedAt(o.getCreatedAt());
        vo.setUpdatedAt(o.getUpdatedAt());
        vo.setRemark(o.getRemark());
        return vo;
    }

    private boolean notEmpty(String s) { return s != null && !s.trim().isEmpty(); }
}
