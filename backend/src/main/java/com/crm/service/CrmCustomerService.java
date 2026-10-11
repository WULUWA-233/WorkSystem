package com.crm.service;

import com.crm.common.PageResult;
import com.crm.dto.*;
import com.crm.vo.CustomerDetailVO;
import com.crm.vo.CustomerVO;

import java.util.Map;

/**
 * Customer Service Interface
 *
 * @author CRM Team
 */
public interface CrmCustomerService {

    PageResult<CustomerVO> listCustomers(CustomerQueryDTO query);

    CustomerDetailVO getCustomerDetail(Long id);

    void createCustomer(CustomerCreateDTO dto);

    void updateCustomer(Long id, CustomerUpdateDTO dto);

    void deleteCustomer(Long id);

    void updateStatus(Long id, CustomerStatusDTO dto);

    Map<String, Boolean> checkName(String customerName, Long excludeId);
}
