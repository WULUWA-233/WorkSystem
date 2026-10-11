package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.CrmOpportunity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface CrmOpportunityMapper extends BaseMapper<CrmOpportunity> {
    @Select("SELECT * FROM crm_opportunity WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    CrmOpportunity selectByIdForUpdate(@Param("id") Long id);

    @Select("SELECT (SELECT COUNT(*) FROM crm_order WHERE opportunity_id = #{id} AND deleted = 0)"
            + " + (SELECT COUNT(*) FROM crm_follow_record WHERE opportunity_id = #{id} AND deleted = 0)")
    long countActiveRelations(@Param("id") Long id);
}
