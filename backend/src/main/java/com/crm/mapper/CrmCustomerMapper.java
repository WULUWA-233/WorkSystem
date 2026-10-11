package com.crm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.crm.entity.CrmCustomer;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * Customer Mapper
 *
 * @author CRM Team
 */
@Mapper
public interface CrmCustomerMapper extends BaseMapper<CrmCustomer> {

    /** Child creation and parent deletion share this lock for relationship integrity. */
    @Select("SELECT * FROM crm_customer WHERE id = #{id} AND deleted = 0 FOR UPDATE")
    CrmCustomer selectByIdForUpdate(@Param("id") Long id);

    /**
     * Check if a customer name already exists among non-deleted customers
     */
    @Select("SELECT COUNT(*) FROM crm_customer WHERE customer_name = #{name} AND deleted = 0")
    int countByName(@Param("name") String name);

    /**
     * Check if customer name exists excluding a given ID
     */
    @Select("SELECT COUNT(*) FROM crm_customer WHERE customer_name = #{name} AND deleted = 0 AND id != #{excludeId}")
    int countByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    @Select("SELECT (SELECT COUNT(*) FROM crm_opportunity WHERE customer_id = #{id} AND deleted = 0)"
            + " + (SELECT COUNT(*) FROM crm_order WHERE customer_id = #{id} AND deleted = 0)"
            + " + (SELECT COUNT(*) FROM crm_follow_record WHERE customer_id = #{id} AND deleted = 0)")
    long countActiveRelations(@Param("id") Long id);
}
