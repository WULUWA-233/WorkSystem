package com.crm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface AnalyticsMapper {

    // ========== Overview ==========

    @Select("SELECT COUNT(*) FROM crm_customer WHERE deleted = 0")
    Long countCustomers();

    @Select("<script>SELECT COALESCE(SUM(amount),0) FROM crm_opportunity WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if></script>")
    BigDecimal sumOpportunityAmount(@Param("start") String start, @Param("end") String end);

    @Select("<script>SELECT COALESCE(SUM(amount),0) FROM crm_order WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if></script>")
    BigDecimal sumOrderAmount(@Param("start") String start, @Param("end") String end);

    @Select("<script>SELECT COALESCE(SUM(amount),0) FROM crm_order WHERE deleted = 0 AND status IN (2,3,4)"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if></script>")
    BigDecimal sumPaidOrderAmount(@Param("start") String start, @Param("end") String end);

    @Select("<script>"
            + "SELECT COUNT(*) FROM crm_customer WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + "</script>")
    Long countNewCustomers(@Param("start") String start, @Param("end") String end);

    @Select("<script>"
            + "SELECT COUNT(*) FROM crm_opportunity WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + "</script>")
    Long countOpportunities(@Param("start") String start, @Param("end") String end);

    @Select("<script>SELECT COUNT(*) FROM crm_opportunity WHERE deleted = 0 AND stage = '5'"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if></script>")
    Long countWonOpportunities(@Param("start") String start, @Param("end") String end);

    @Select("<script>SELECT COUNT(*) FROM crm_opportunity WHERE deleted = 0 AND stage = '6'"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if></script>")
    Long countLostOpportunities(@Param("start") String start, @Param("end") String end);

    @Select("<script>"
            + "SELECT COUNT(*) FROM crm_order WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + "</script>")
    Long countOrders(@Param("start") String start, @Param("end") String end);

    @Select("<script>"
            + "SELECT COUNT(*) FROM crm_follow_record WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + "</script>")
    Long countFollowRecords(@Param("start") String start, @Param("end") String end);

    @Select("SELECT COUNT(*) FROM crm_follow_record WHERE deleted = 0 AND DATE(follow_time) = CURDATE()")
    Long countTodayFollows();

    // ========== Customer Trend ==========

    @Select("<script>"
            + "SELECT DATE_FORMAT(created_at, #{fmt}) AS dt, COUNT(*) AS cnt "
            + "FROM crm_customer WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + " GROUP BY dt ORDER BY dt"
            + "</script>")
    List<Map<String, Object>> customerTrend(@Param("fmt") String fmt, @Param("start") String start, @Param("end") String end);

    // ========== Opportunity Stage ==========

    @Select("<script>"
            + "SELECT stage, COUNT(*) AS cnt, COALESCE(SUM(amount),0) AS amt "
            + "FROM crm_opportunity WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + " GROUP BY stage ORDER BY CAST(stage AS UNSIGNED)"
            + "</script>")
    List<Map<String, Object>> opportunityStageStats(@Param("start") String start, @Param("end") String end);

    // ========== Order Status ==========

    @Select("<script>"
            + "SELECT status, COUNT(*) AS cnt, COALESCE(SUM(amount),0) AS amt "
            + "FROM crm_order WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + " GROUP BY status ORDER BY status"
            + "</script>")
    List<Map<String, Object>> orderStatusStats(@Param("start") String start, @Param("end") String end);

    // ========== Sales Performance ==========

    @Select("<script>"
            + "SELECT u.id AS ownerId, u.real_name AS ownerName,"
            + " (SELECT COUNT(*) FROM crm_customer WHERE owner_id = u.id AND deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>) AS customerCount,"
            + " (SELECT COUNT(*) FROM crm_opportunity WHERE owner_id = u.id AND deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>) AS opportunityCount,"
            + " (SELECT COUNT(*) FROM crm_opportunity WHERE owner_id = u.id AND deleted = 0 AND stage = '5'"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>) AS wonOpportunityCount,"
            + " (SELECT COUNT(*) FROM crm_order WHERE owner_id = u.id AND deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>) AS orderCount,"
            + " COALESCE((SELECT SUM(amount) FROM crm_order WHERE owner_id = u.id AND deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>),0) AS orderAmount"
            + " FROM sys_user u WHERE u.deleted = 0 AND u.status = 1"
            + " AND EXISTS ("
            + "   SELECT 1 FROM sys_user_role ur"
            + "   INNER JOIN sys_role r ON r.id = ur.role_id"
            + "   WHERE ur.user_id = u.id AND ur.deleted = 0 AND r.deleted = 0 AND r.status = 1 AND r.role_code = 'SALES'"
            + " )"
            + " AND NOT EXISTS ("
            + "   SELECT 1 FROM sys_user_role ur"
            + "   INNER JOIN sys_role r ON r.id = ur.role_id"
            + "   WHERE ur.user_id = u.id AND ur.deleted = 0 AND r.deleted = 0 AND r.status = 1"
            + "     AND r.role_code IN ('ADMIN', 'MANAGER', 'AUDITOR')"
            + " )"
            + "<if test='ownerId != null'> AND u.id = #{ownerId}</if>"
            + " ORDER BY orderAmount DESC"
            + "</script>")
    List<Map<String, Object>> salesPerformance(@Param("ownerId") Long ownerId,
                                             @Param("start") String start, @Param("end") String end);

    // ========== Customer Industry ==========

    @Select("<script>"
            + "SELECT COALESCE(NULLIF(industry,''), 'Not filled') AS nm, COUNT(*) AS cnt "
            + "FROM crm_customer WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + " GROUP BY nm ORDER BY cnt DESC"
            + "</script>")
    List<Map<String, Object>> customerIndustryStats(@Param("start") String start, @Param("end") String end);

    // ========== Customer Region ==========

    @Select("<script>"
            + "SELECT COALESCE(NULLIF(region,''), 'Not filled') AS nm, COUNT(*) AS cnt "
            + "FROM crm_customer WHERE deleted = 0"
            + "<if test='start != null'> AND created_at &gt;= #{start}</if>"
            + "<if test='end != null'> AND created_at &lt;= #{end}</if>"
            + " GROUP BY nm ORDER BY cnt DESC"
            + "</script>")
    List<Map<String, Object>> customerRegionStats(@Param("start") String start, @Param("end") String end);
}
