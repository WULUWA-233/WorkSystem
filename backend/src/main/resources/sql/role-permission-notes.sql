-- =====================================================
-- Role permission notes and performance policy
-- Connection:
--   mysql -uroot -p123456 --default-character-set=utf8mb4 < backend/src/main/resources/sql/role-permission-notes.sql
-- =====================================================

SET NAMES utf8mb4;
USE crm_db;

UPDATE sys_role
SET role_name = '系统管理员',
    description = '负责系统配置、用户角色维护、全量数据查看与操作日志审计；不参与销售业绩统计。',
    remark = '权限：用户管理、角色管理、操作日志、客户、商机、跟进记录、订单和统计看板。数据范围：全量数据。业绩口径：不参与业绩。',
    status = 1,
    deleted = 0
WHERE role_code = 'ADMIN';

UPDATE sys_role
SET role_name = '销售顾问',
    description = '负责客户经营、商机推进、跟进记录和订单转化；参与销售业绩统计。',
    remark = '权限：客户新增编辑、商机创建推进、跟进记录、基于赢单商机创建订单、个人看板。数据范围：本人负责的数据。业绩口径：参与业绩。',
    status = 1,
    deleted = 0
WHERE role_code = 'SALES';

UPDATE sys_role
SET role_name = '普通用户',
    description = '基础访问身份，不建议直接用于业务授权；不参与销售业绩统计。',
    remark = '权限：基础登录和个人信息查看。数据范围：本人基础信息。业绩口径：不参与业绩。',
    deleted = 0
WHERE role_code = 'USER';

SELECT role_code, role_name, description, remark
FROM sys_role
WHERE role_code IN ('ADMIN', 'SALES', 'USER')
ORDER BY sort, id;
