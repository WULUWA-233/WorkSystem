package com.crm.vo;

import java.io.Serializable;

public class NameValueVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String name;
    private Long count;

    public NameValueVO() {}
    public NameValueVO(String name, Long count) { this.name = name; this.count = count; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
