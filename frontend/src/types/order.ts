/**
 * Order Management Types
 */

export interface OrderVO {
  id: number
  orderNo: string
  customerId: number
  customerName?: string
  opportunityId?: number
  opportunityName?: string
  amount?: number
  status: number
  statusName?: string
  paymentMethod?: string
  paymentTime?: string
  deliveryTime?: string
  finishTime?: string
  cancelTime?: string
  ownerId?: number
  ownerName?: string
  createdAt: string
  updatedAt?: string
}

export interface OrderDetailVO extends OrderVO {
  logisticsNo?: string
  logisticsCompany?: string
  remark?: string
}

export interface OrderQuery {
  pageNo?: number
  pageSize?: number
  orderNo?: string
  customerId?: number
  opportunityId?: number
  status?: number
  paymentMethod?: string
  ownerId?: number
  minAmount?: number
  maxAmount?: number
  startTime?: string
  endTime?: string
}

export interface OrderCreateDTO {
  customerId: number
  opportunityId?: number
  amount: number
  ownerId: number
  paymentMethod?: string
  remark?: string
}

export interface OrderUpdateDTO {
  amount?: number
  paymentMethod?: string
  ownerId?: number
  remark?: string
}

export interface OrderStatusDTO {
  status: number
  paymentMethod?: string
  logisticsNo?: string
  logisticsCompany?: string
  remark?: string
}

export interface OrderPayDTO {
  paymentMethod: string
}

export interface OrderShipDTO {
  logisticsNo: string
  logisticsCompany?: string
}

export interface OrderCancelDTO {
  remark?: string
}
