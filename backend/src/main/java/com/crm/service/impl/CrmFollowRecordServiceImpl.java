package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.FollowRecordCreateDTO;
import com.crm.dto.FollowRecordQueryDTO;
import com.crm.entity.CrmCustomer;
import com.crm.entity.CrmFollowRecord;
import com.crm.entity.CrmOpportunity;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.CrmFollowRecordMapper;
import com.crm.mapper.CrmOpportunityMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmFollowRecordService;
import com.crm.utils.DateParseUtils;
import com.crm.utils.LoginUserContext;
import com.crm.vo.FollowRecordDetailVO;
import com.crm.vo.FollowRecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CrmFollowRecordServiceImpl implements CrmFollowRecordService {

    private static final Logger log = LoggerFactory.getLogger(CrmFollowRecordServiceImpl.class);
    private static final Set<String> FOLLOW_TYPES = new HashSet<>(Arrays.asList("电话", "微信", "邮件", "拜访", "会议", "其他"));

    private final CrmFollowRecordMapper followMapper;
    private final CrmCustomerMapper customerMapper;
    private final CrmOpportunityMapper opportunityMapper;
    private final SysUserMapper sysUserMapper;

    public CrmFollowRecordServiceImpl(CrmFollowRecordMapper followMapper,
                                       CrmCustomerMapper customerMapper,
                                       CrmOpportunityMapper opportunityMapper,
                                       SysUserMapper sysUserMapper) {
        this.followMapper = followMapper;
        this.customerMapper = customerMapper;
        this.opportunityMapper = opportunityMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<FollowRecordVO> listFollowRecords(FollowRecordQueryDTO query) {
        LambdaQueryWrapper<CrmFollowRecord> w = new LambdaQueryWrapper<>();
        w.eq(CrmFollowRecord::getDeleted, 0);

        if (query.getCustomerId() != null) w.eq(CrmFollowRecord::getCustomerId, query.getCustomerId());
        if (query.getOpportunityId() != null) w.eq(CrmFollowRecord::getOpportunityId, query.getOpportunityId());
        if (query.getFollowerId() != null) w.eq(CrmFollowRecord::getFollowerId, query.getFollowerId());
        if (notEmpty(query.getFollowType())) w.eq(CrmFollowRecord::getFollowType, query.getFollowType());
        if (query.getCustomerIntention() != null) w.eq(CrmFollowRecord::getCustomerIntention, query.getCustomerIntention());
        if (notEmpty(query.getStartTime())) {
            w.ge(CrmFollowRecord::getFollowTime, DateParseUtils.parseDateTime(query.getStartTime(), "startTime"));
        }
        if (notEmpty(query.getEndTime())) {
            w.le(CrmFollowRecord::getFollowTime, DateParseUtils.parseDateTime(query.getEndTime(), "endTime"));
        }
        w.orderByDesc(CrmFollowRecord::getFollowTime);

        Page<CrmFollowRecord> page = new Page<>(query.getPage(), query.getSize());
        Page<CrmFollowRecord> result = followMapper.selectPage(page, w);

        List<FollowRecordVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    @Transactional
    public void createFollowRecord(FollowRecordCreateDTO dto) {
        if (!FOLLOW_TYPES.contains(dto.getFollowType()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择有效的跟进方式");
        if (dto.getFollowContent() == null || dto.getFollowContent().trim().isEmpty())
            throw new BusinessException(ResultCode.PARAM_ERROR, "请填写跟进内容");
        // Validate customer
        CrmCustomer customer = customerMapper.selectByIdForUpdate(dto.getCustomerId());
        if (customer == null || customer.getDeleted() == 1)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Customer not found");

        if (dto.getOpportunityId() == null) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "请选择商机");
        }
        CrmOpportunity opp = opportunityMapper.selectByIdForUpdate(dto.getOpportunityId());
        if (opp == null || opp.getDeleted() == 1)
            throw new BusinessException(ResultCode.PARAM_ERROR, "商机不存在");
        if (!opp.getCustomerId().equals(dto.getCustomerId()))
            throw new BusinessException(ResultCode.PARAM_ERROR, "商机不属于该客户");

        // Validate follower
        SysUser follower = sysUserMapper.selectById(dto.getFollowerId());
        if (follower == null || follower.getDeleted() == 1)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Follower user not found");
        if (follower.getStatus() == null || follower.getStatus() == 0)
            throw new BusinessException(ResultCode.PARAM_ERROR, "Follower user is disabled");

        // Validate customerIntention range
        if (dto.getCustomerIntention() != null && (dto.getCustomerIntention() < 1 || dto.getCustomerIntention() > 4))
            throw new BusinessException(ResultCode.PARAM_ERROR, "Customer intention must be 1-4");

        // Parse times
        LocalDateTime followTime = LocalDateTime.now();
        if (notEmpty(dto.getFollowTime())) {
            followTime = DateParseUtils.parseDateTime(dto.getFollowTime(), "followTime");
        }
        LocalDateTime nextFollowTime = null;
        if (notEmpty(dto.getNextFollowTime())) {
            nextFollowTime = DateParseUtils.parseDateTime(dto.getNextFollowTime(), "nextFollowTime");
        }
        if (nextFollowTime != null && nextFollowTime.isBefore(followTime))
            throw new BusinessException(ResultCode.PARAM_ERROR, "下次跟进时间不能早于本次跟进时间");

        Long uid = LoginUserContext.getCurrentUserId();

        // Insert follow record
        CrmFollowRecord fr = new CrmFollowRecord();
        fr.setCustomerId(dto.getCustomerId());
        fr.setOpportunityId(dto.getOpportunityId());
        fr.setFollowType(dto.getFollowType());
        fr.setFollowContent(dto.getFollowContent());
        fr.setFollowResult(dto.getFollowResult());
        fr.setCustomerIntention(dto.getCustomerIntention());
        fr.setNextPlan(dto.getNextPlan());
        fr.setNextFollowTime(nextFollowTime);
        fr.setFollowTime(followTime);
        fr.setFollowerId(dto.getFollowerId());
        fr.setCreatedBy(uid);
        fr.setUpdatedBy(uid);
        fr.setRemark(dto.getRemark());
        followMapper.insert(fr);

        // Update customer's last_follow_time / next_follow_time
        LambdaUpdateWrapper<CrmCustomer> cw = new LambdaUpdateWrapper<>();
        cw.eq(CrmCustomer::getId, dto.getCustomerId())
          .eq(CrmCustomer::getDeleted, 0)
          .and(w -> w.isNull(CrmCustomer::getLastFollowTime).or().le(CrmCustomer::getLastFollowTime, fr.getFollowTime()))
          .set(CrmCustomer::getLastFollowTime, followTime)
          .set(CrmCustomer::getNextFollowTime, nextFollowTime)
          .set(CrmCustomer::getUpdatedBy, uid)
          .set(CrmCustomer::getUpdatedAt, LocalDateTime.now());
        customerMapper.update(null, cw);

        LambdaUpdateWrapper<CrmOpportunity> ow = new LambdaUpdateWrapper<>();
        ow.eq(CrmOpportunity::getId, dto.getOpportunityId())
          .eq(CrmOpportunity::getDeleted, 0)
          .and(w -> w.isNull(CrmOpportunity::getLastFollowTime).or().le(CrmOpportunity::getLastFollowTime, fr.getFollowTime()))
          .set(CrmOpportunity::getLastFollowTime, followTime)
          .set(CrmOpportunity::getNextFollowTime, nextFollowTime)
          .set(CrmOpportunity::getUpdatedBy, uid)
          .set(CrmOpportunity::getUpdatedAt, LocalDateTime.now());
        opportunityMapper.update(null, ow);

        log.info("Follow record created: id={}, customerId={}, oppId={}", fr.getId(), dto.getCustomerId(), dto.getOpportunityId());
    }

    @Override
    public FollowRecordDetailVO getFollowRecordDetail(Long id) {
        CrmFollowRecord fr = getOrThrow(id);
        return toDetailVO(fr);
    }

    @Override
    @Transactional
    public void deleteFollowRecord(Long id) {
        CrmFollowRecord fr = getOrThrow(id);
        // Use the same lock order as creation; current reads below see committed changes.
        customerMapper.selectByIdForUpdate(fr.getCustomerId());
        if (fr.getOpportunityId() != null) opportunityMapper.selectByIdForUpdate(fr.getOpportunityId());
        LambdaUpdateWrapper<CrmFollowRecord> w = new LambdaUpdateWrapper<>();
        w.eq(CrmFollowRecord::getId, id)
         .eq(CrmFollowRecord::getDeleted, 0)
         .set(CrmFollowRecord::getDeleted, 1)
         .set(CrmFollowRecord::getUpdatedAt, LocalDateTime.now())
         .set(CrmFollowRecord::getUpdatedBy, LoginUserContext.getCurrentUserId());
        if (followMapper.update(null, w) != 1)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "跟进记录已发生变化，请刷新后重试");
        refreshFollowTimes(fr);
        log.info("Follow record deleted: id={}", id);
    }

    @Override
    public PageResult<FollowRecordVO> listByCustomer(Long customerId, long page, long size) {
        CrmCustomer c = customerMapper.selectById(customerId);
        if (c == null || c.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Customer not found");

        LambdaQueryWrapper<CrmFollowRecord> w = new LambdaQueryWrapper<>();
        w.eq(CrmFollowRecord::getCustomerId, customerId)
         .eq(CrmFollowRecord::getDeleted, 0)
         .orderByDesc(CrmFollowRecord::getFollowTime);
        Page<CrmFollowRecord> p = new Page<>(page, size);
        Page<CrmFollowRecord> result = followMapper.selectPage(p, w);
        List<FollowRecordVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public PageResult<FollowRecordVO> listByOpportunity(Long opportunityId, long page, long size) {
        CrmOpportunity opp = opportunityMapper.selectById(opportunityId);
        if (opp == null || opp.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Opportunity not found");

        LambdaQueryWrapper<CrmFollowRecord> w = new LambdaQueryWrapper<>();
        w.eq(CrmFollowRecord::getOpportunityId, opportunityId)
         .eq(CrmFollowRecord::getDeleted, 0)
         .orderByDesc(CrmFollowRecord::getFollowTime);
        Page<CrmFollowRecord> p = new Page<>(page, size);
        Page<CrmFollowRecord> result = followMapper.selectPage(p, w);
        List<FollowRecordVO> records = result.getRecords().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    // ========== helpers ==========

    private void refreshFollowTimes(CrmFollowRecord deletedRecord) {
        CrmFollowRecord latestCustomer = latestFollow(deletedRecord.getCustomerId(), null);
        LambdaUpdateWrapper<CrmCustomer> cw = new LambdaUpdateWrapper<>();
        cw.eq(CrmCustomer::getId, deletedRecord.getCustomerId()).eq(CrmCustomer::getDeleted, 0);
        if (deletedRecord.getFollowTime() == null) cw.isNull(CrmCustomer::getLastFollowTime);
        else cw.eq(CrmCustomer::getLastFollowTime, deletedRecord.getFollowTime());
        cw.set(CrmCustomer::getLastFollowTime, latestCustomer == null ? null : latestCustomer.getFollowTime())
          .set(CrmCustomer::getNextFollowTime, latestCustomer == null ? null : latestCustomer.getNextFollowTime())
          .set(CrmCustomer::getUpdatedBy, LoginUserContext.getCurrentUserId())
          .set(CrmCustomer::getUpdatedAt, LocalDateTime.now());
        customerMapper.update(null, cw);
        if (deletedRecord.getOpportunityId() != null) {
            CrmFollowRecord latestOpportunity = latestFollow(null, deletedRecord.getOpportunityId());
            LambdaUpdateWrapper<CrmOpportunity> ow = new LambdaUpdateWrapper<>();
            ow.eq(CrmOpportunity::getId, deletedRecord.getOpportunityId()).eq(CrmOpportunity::getDeleted, 0);
            if (deletedRecord.getFollowTime() == null) ow.isNull(CrmOpportunity::getLastFollowTime);
            else ow.eq(CrmOpportunity::getLastFollowTime, deletedRecord.getFollowTime());
            ow.set(CrmOpportunity::getLastFollowTime, latestOpportunity == null ? null : latestOpportunity.getFollowTime())
              .set(CrmOpportunity::getNextFollowTime, latestOpportunity == null ? null : latestOpportunity.getNextFollowTime())
              .set(CrmOpportunity::getUpdatedBy, LoginUserContext.getCurrentUserId())
              .set(CrmOpportunity::getUpdatedAt, LocalDateTime.now());
            opportunityMapper.update(null, ow);
        }
    }

    private CrmFollowRecord latestFollow(Long customerId, Long opportunityId) {
        LambdaQueryWrapper<CrmFollowRecord> w = new LambdaQueryWrapper<>();
        w.eq(CrmFollowRecord::getDeleted, 0)
         .eq(customerId != null, CrmFollowRecord::getCustomerId, customerId)
         .eq(opportunityId != null, CrmFollowRecord::getOpportunityId, opportunityId)
         .orderByDesc(CrmFollowRecord::getFollowTime, CrmFollowRecord::getId).last("LIMIT 1 FOR UPDATE");
        return followMapper.selectOne(w);
    }

    private CrmFollowRecord getOrThrow(Long id) {
        CrmFollowRecord fr = followMapper.selectById(id);
        if (fr == null || fr.getDeleted() == 1)
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Follow record not found");
        return fr;
    }

    private String getCustomerName(Long id) {
        if (id == null) return null;
        CrmCustomer c = customerMapper.selectById(id);
        return c != null ? c.getCustomerName() : null;
    }

    private String getOpportunityName(Long id) {
        if (id == null) return null;
        CrmOpportunity o = opportunityMapper.selectById(id);
        return o != null ? o.getOpportunityName() : null;
    }

    private String getFollowerName(Long id) {
        if (id == null) return null;
        SysUser u = sysUserMapper.selectById(id);
        return u != null ? u.getRealName() : null;
    }

    private FollowRecordVO toVO(CrmFollowRecord fr) {
        FollowRecordVO vo = new FollowRecordVO();
        vo.setId(fr.getId());
        vo.setCustomerId(fr.getCustomerId());
        vo.setCustomerName(getCustomerName(fr.getCustomerId()));
        vo.setOpportunityId(fr.getOpportunityId());
        vo.setOpportunityName(getOpportunityName(fr.getOpportunityId()));
        vo.setFollowType(fr.getFollowType());
        vo.setFollowContent(fr.getFollowContent());
        vo.setFollowResult(fr.getFollowResult());
        vo.setCustomerIntention(fr.getCustomerIntention());
        vo.setNextPlan(fr.getNextPlan());
        vo.setNextFollowTime(fr.getNextFollowTime());
        vo.setFollowTime(fr.getFollowTime());
        vo.setFollowerId(fr.getFollowerId());
        vo.setFollowerName(getFollowerName(fr.getFollowerId()));
        vo.setCreatedAt(fr.getCreatedAt());
        return vo;
    }

    private FollowRecordDetailVO toDetailVO(CrmFollowRecord fr) {
        FollowRecordDetailVO vo = new FollowRecordDetailVO();
        vo.setId(fr.getId());
        vo.setCustomerId(fr.getCustomerId());
        vo.setCustomerName(getCustomerName(fr.getCustomerId()));
        vo.setOpportunityId(fr.getOpportunityId());
        vo.setOpportunityName(getOpportunityName(fr.getOpportunityId()));
        vo.setFollowType(fr.getFollowType());
        vo.setFollowContent(fr.getFollowContent());
        vo.setFollowResult(fr.getFollowResult());
        vo.setCustomerIntention(fr.getCustomerIntention());
        vo.setNextPlan(fr.getNextPlan());
        vo.setNextFollowTime(fr.getNextFollowTime());
        vo.setFollowTime(fr.getFollowTime());
        vo.setFollowerId(fr.getFollowerId());
        vo.setFollowerName(getFollowerName(fr.getFollowerId()));
        vo.setCreatedAt(fr.getCreatedAt());
        vo.setUpdatedAt(fr.getUpdatedAt());
        vo.setRemark(fr.getRemark());
        return vo;
    }

    private boolean notEmpty(String s) { return s != null && !s.isEmpty(); }
}
