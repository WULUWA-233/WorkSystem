package com.crm.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.crm.common.BusinessException;
import com.crm.common.PageResult;
import com.crm.common.ResultCode;
import com.crm.dto.*;
import com.crm.entity.CrmCustomer;
import com.crm.entity.SysUser;
import com.crm.mapper.CrmCustomerMapper;
import com.crm.mapper.SysUserMapper;
import com.crm.service.CrmCustomerService;
import com.crm.utils.DateParseUtils;
import com.crm.utils.LoginUserContext;
import com.crm.vo.CustomerDetailVO;
import com.crm.vo.CustomerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Customer Service Implementation
 *
 * @author CRM Team
 */
@Service
public class CrmCustomerServiceImpl implements CrmCustomerService {

    private static final Logger log = LoggerFactory.getLogger(CrmCustomerServiceImpl.class);

    private final CrmCustomerMapper customerMapper;
    private final SysUserMapper sysUserMapper;

    public CrmCustomerServiceImpl(CrmCustomerMapper customerMapper, SysUserMapper sysUserMapper) {
        this.customerMapper = customerMapper;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public PageResult<CustomerVO> listCustomers(CustomerQueryDTO query) {
        LambdaQueryWrapper<CrmCustomer> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CrmCustomer::getDeleted, 0);

        if (notEmpty(query.getCustomerName())) wrapper.like(CrmCustomer::getCustomerName, query.getCustomerName());
        if (notEmpty(query.getIndustry())) wrapper.eq(CrmCustomer::getIndustry, query.getIndustry());
        if (notEmpty(query.getSource())) wrapper.eq(CrmCustomer::getSource, query.getSource());
        if (query.getLevel() != null) wrapper.eq(CrmCustomer::getLevel, String.valueOf(query.getLevel()));
        if (notEmpty(query.getRegion())) wrapper.like(CrmCustomer::getRegion, query.getRegion());
        if (query.getStatus() != null) wrapper.eq(CrmCustomer::getStatus, query.getStatus());
        if (query.getOwnerId() != null) wrapper.eq(CrmCustomer::getOwnerId, query.getOwnerId());
        if (notEmpty(query.getStartTime())) {
            wrapper.ge(CrmCustomer::getCreatedAt, DateParseUtils.parseDateTime(query.getStartTime(), "startTime"));
        }
        if (notEmpty(query.getEndTime())) {
            wrapper.le(CrmCustomer::getCreatedAt, DateParseUtils.parseDateTime(query.getEndTime(), "endTime"));
        }
        wrapper.orderByDesc(CrmCustomer::getId);

        Page<CrmCustomer> page = new Page<>(query.getPage(), query.getSize());
        Page<CrmCustomer> result = customerMapper.selectPage(page, wrapper);

        Map<Long, String> ownerNames = loadUserNames(result.getRecords().stream()
                .map(CrmCustomer::getOwnerId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList()));

        List<CustomerVO> records = result.getRecords().stream().map(c -> {
            CustomerVO vo = new CustomerVO();
            vo.setId(c.getId());
            vo.setCustomerName(c.getCustomerName());
            vo.setIndustry(c.getIndustry());
            vo.setSource(c.getSource());
            vo.setLevel(c.getLevel() != null ? Integer.valueOf(c.getLevel()) : null);
            vo.setRegion(c.getRegion());
            vo.setContactName(c.getContactName());
            vo.setContactPhone(c.getContactPhone());
            vo.setOwnerId(c.getOwnerId());
            vo.setOwnerName(ownerNames.get(c.getOwnerId()));
            vo.setStatus(c.getStatus());
            vo.setScore(c.getScore());
            vo.setLastFollowTime(c.getLastFollowTime());
            vo.setNextFollowTime(c.getNextFollowTime());
            vo.setCreatedAt(c.getCreatedAt());
            return vo;
        }).collect(Collectors.toList());

        return new PageResult<>(result.getTotal(), result.getCurrent(), result.getSize(), records);
    }

    @Override
    public CustomerDetailVO getCustomerDetail(Long id) {
        CrmCustomer c = getCustomerOrThrow(id);
        CustomerDetailVO vo = new CustomerDetailVO();
        vo.setId(c.getId());
        vo.setCustomerName(c.getCustomerName());
        vo.setIndustry(c.getIndustry());
        vo.setSource(c.getSource());
        vo.setLevel(c.getLevel() != null ? Integer.valueOf(c.getLevel()) : null);
        vo.setRegion(c.getRegion());
        vo.setAddress(c.getAddress());
        vo.setContactName(c.getContactName());
        vo.setContactPhone(c.getContactPhone());
        vo.setContactEmail(c.getContactEmail());
        vo.setOwnerId(c.getOwnerId());
        vo.setOwnerName(getOwnerName(c.getOwnerId()));
        vo.setStatus(c.getStatus());
        vo.setScore(c.getScore());
        vo.setLastFollowTime(c.getLastFollowTime());
        vo.setNextFollowTime(c.getNextFollowTime());
        vo.setCreatedAt(c.getCreatedAt());
        vo.setUpdatedAt(c.getUpdatedAt());
        vo.setRemark(c.getRemark());
        return vo;
    }

    @Override
    @Transactional
    public void createCustomer(CustomerCreateDTO dto) {
        // Check name uniqueness
        String customerName = dto.getCustomerName().trim();
        if (customerMapper.countByName(customerName) > 0) {
            throw new BusinessException(ResultCode.DUPLICATE_KEY, "Customer name already exists: " + dto.getCustomerName());
        }
        // Validate owner
        validateOwner(dto.getOwnerId());
        // Validate level/status/score
        int level = dto.getLevel() != null ? dto.getLevel() : 1;
        int status = dto.getStatus() != null ? dto.getStatus() : 1;
        int score = dto.getScore() != null ? dto.getScore() : 0;
        if (level < 1 || level > 5) throw new BusinessException(ResultCode.PARAM_ERROR, "Level must be 1-5");
        if (status < 0 || status > 2) throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0/1/2");
        if (score < 0 || score > 100) throw new BusinessException(ResultCode.PARAM_ERROR, "Score must be 0-100");

        Long currentUserId = LoginUserContext.getCurrentUserId();
        CrmCustomer c = new CrmCustomer();
        c.setCustomerName(customerName);
        c.setIndustry(dto.getIndustry());
        c.setSource(dto.getSource());
        c.setLevel(String.valueOf(level));
        c.setRegion(dto.getRegion());
        c.setAddress(dto.getAddress());
        c.setContactName(dto.getContactName());
        c.setContactPhone(dto.getContactPhone());
        c.setContactEmail(dto.getContactEmail());
        c.setOwnerId(dto.getOwnerId());
        c.setStatus(status);
        c.setScore(score);
        if (notEmpty(dto.getNextFollowTime())) {
            c.setNextFollowTime(DateParseUtils.parseDateTime(dto.getNextFollowTime(), "nextFollowTime"));
        }
        c.setCreatedBy(currentUserId);
        c.setUpdatedBy(currentUserId);
        c.setRemark(dto.getRemark());
        customerMapper.insert(c);
        log.info("Customer created: id={}, name={}", c.getId(), c.getCustomerName());
    }

    @Override
    public void updateCustomer(Long id, CustomerUpdateDTO dto) {
        CrmCustomer c = getCustomerOrThrow(id);
        LambdaUpdateWrapper<CrmCustomer> wrapper = updateForCustomer(c);

        // Check name uniqueness if name is being changed
        if (dto.getCustomerName() != null) {
            if (!notEmpty(dto.getCustomerName()))
                throw new BusinessException(ResultCode.PARAM_ERROR, "Customer name cannot be blank");
            String name = dto.getCustomerName().trim();
            if (!name.equals(c.getCustomerName()) && customerMapper.countByNameExcludeId(name, id) > 0) {
                throw new BusinessException(ResultCode.DUPLICATE_KEY, "Customer name already exists: " + dto.getCustomerName());
            }
            wrapper.set(CrmCustomer::getCustomerName, name);
        }

        // Validate owner if changed
        if (dto.getOwnerId() != null) {
            validateOwner(dto.getOwnerId());
            wrapper.set(CrmCustomer::getOwnerId, dto.getOwnerId());
        }
        // Validate ranges
        if (dto.getLevel() != null) {
            if (dto.getLevel() < 1 || dto.getLevel() > 5) throw new BusinessException(ResultCode.PARAM_ERROR, "Level must be 1-5");
            wrapper.set(CrmCustomer::getLevel, String.valueOf(dto.getLevel()));
        }
        if (dto.getStatus() != null) {
            if (dto.getStatus() < 0 || dto.getStatus() > 2) throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0/1/2");
            wrapper.eq(CrmCustomer::getStatus, c.getStatus()).set(CrmCustomer::getStatus, dto.getStatus());
        }
        if (dto.getScore() != null) {
            if (dto.getScore() < 0 || dto.getScore() > 100) throw new BusinessException(ResultCode.PARAM_ERROR, "Score must be 0-100");
            wrapper.set(CrmCustomer::getScore, dto.getScore());
        }

        if (dto.getIndustry() != null) wrapper.set(CrmCustomer::getIndustry, dto.getIndustry());
        if (dto.getSource() != null) wrapper.set(CrmCustomer::getSource, dto.getSource());
        if (dto.getRegion() != null) wrapper.set(CrmCustomer::getRegion, dto.getRegion());
        if (dto.getAddress() != null) wrapper.set(CrmCustomer::getAddress, dto.getAddress());
        if (dto.getContactName() != null) wrapper.set(CrmCustomer::getContactName, dto.getContactName());
        if (dto.getContactPhone() != null) wrapper.set(CrmCustomer::getContactPhone, dto.getContactPhone());
        if (dto.getContactEmail() != null) wrapper.set(CrmCustomer::getContactEmail, dto.getContactEmail());
        if (dto.getNextFollowTime() != null) {
            wrapper.set(CrmCustomer::getNextFollowTime, notEmpty(dto.getNextFollowTime())
                    ? DateParseUtils.parseDateTime(dto.getNextFollowTime(), "nextFollowTime") : null);
        }
        if (dto.getRemark() != null) wrapper.set(CrmCustomer::getRemark, dto.getRemark());

        updateOrThrow(wrapper);
        log.info("Customer updated: id={}", id);
    }

    @Override
    @Transactional
    public void deleteCustomer(Long id) {
        CrmCustomer c = customerMapper.selectByIdForUpdate(id);
        if (c == null || Integer.valueOf(1).equals(c.getDeleted()))
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Customer not found");
        if (customerMapper.countActiveRelations(id) > 0)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Cannot delete a customer with opportunities, orders or follow-up records");

        LambdaUpdateWrapper<CrmCustomer> wrapper = updateForCustomer(c);
        wrapper.apply("NOT EXISTS (SELECT 1 FROM crm_opportunity WHERE customer_id = {0} AND deleted = 0)", id)
               .apply("NOT EXISTS (SELECT 1 FROM crm_order WHERE customer_id = {0} AND deleted = 0)", id)
               .apply("NOT EXISTS (SELECT 1 FROM crm_follow_record WHERE customer_id = {0} AND deleted = 0)", id)
               .set(CrmCustomer::getDeleted, 1);
        updateOrThrow(wrapper);
        log.info("Customer deleted: id={}, name={}", id, c.getCustomerName());
    }

    @Override
    public void updateStatus(Long id, CustomerStatusDTO dto) {
        if (dto.getStatus() == null || dto.getStatus() < 0 || dto.getStatus() > 2) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Status must be 0, 1, or 2");
        }
        CrmCustomer c = getCustomerOrThrow(id);

        LambdaUpdateWrapper<CrmCustomer> wrapper = updateForCustomer(c);
        wrapper.eq(CrmCustomer::getStatus, c.getStatus()).set(CrmCustomer::getStatus, dto.getStatus());
        updateOrThrow(wrapper);
        log.info("Customer status changed: id={}, status={}", id, dto.getStatus());
    }

    @Override
    public Map<String, Boolean> checkName(String customerName, Long excludeId) {
        if (customerName == null || customerName.trim().isEmpty()) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Customer name cannot be empty");
        }
        int count;
        if (excludeId != null) {
            count = customerMapper.countByNameExcludeId(customerName.trim(), excludeId);
        } else {
            count = customerMapper.countByName(customerName.trim());
        }
        Map<String, Boolean> result = new HashMap<>();
        result.put("exists", count > 0);
        return result;
    }

    // ========== Private helpers ==========

    private LambdaUpdateWrapper<CrmCustomer> updateForCustomer(CrmCustomer customer) {
        LambdaUpdateWrapper<CrmCustomer> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(CrmCustomer::getId, customer.getId())
               .eq(CrmCustomer::getDeleted, 0)
               .set(CrmCustomer::getUpdatedBy, LoginUserContext.getCurrentUserId())
               .set(CrmCustomer::getUpdatedAt, LocalDateTime.now());
        return wrapper;
    }

    private void updateOrThrow(LambdaUpdateWrapper<CrmCustomer> wrapper) {
        if (customerMapper.update(null, wrapper) != 1)
            throw new BusinessException(ResultCode.OPERATION_FAILED, "Customer changed, was deleted or has related records; refresh and try again");
    }

    private CrmCustomer getCustomerOrThrow(Long id) {
        CrmCustomer c = customerMapper.selectById(id);
        if (c == null || Integer.valueOf(1).equals(c.getDeleted())) {
            throw new BusinessException(ResultCode.DATA_NOT_FOUND, "Customer not found");
        }
        return c;
    }

    private void validateOwner(Long ownerId) {
        SysUser owner = sysUserMapper.selectById(ownerId);
        if (owner == null || owner.getDeleted() == 1) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user not found");
        }
        if (owner.getStatus() == null || owner.getStatus() == 0) {
            throw new BusinessException(ResultCode.PARAM_ERROR, "Owner user is disabled");
        }
    }

    private String getOwnerName(Long ownerId) {
        if (ownerId == null) return null;
        SysUser owner = sysUserMapper.selectById(ownerId);
        return owner != null ? owner.getRealName() : null;
    }

    private Map<Long, String> loadUserNames(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return sysUserMapper.selectBatchIds(userIds).stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName, (a, b) -> a));
    }

    private boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
