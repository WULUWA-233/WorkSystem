package com.crm.vo;

import java.io.Serializable;

public class TrendItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String date;
    private Long count;

    public TrendItemVO() {}
    public TrendItemVO(String date, Long count) { this.date = date; this.count = count; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
