package com.crm.common;

import java.io.Serializable;

/**
 * Pagination Query Parameters
 *
 * @author CRM Team
 */
public class PageQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Current page number (default: 1) */
    private long page = 1;

    /** Page size (default: 10, max: 100) */
    private long size = 10;

    private boolean pageSpecified;
    private boolean sizeSpecified;

    /** Sort field */
    private String sortField;

    /** Sort order: asc / desc */
    private String sortOrder;

    public PageQuery() {
    }

    public PageQuery(long page, long size) {
        setPage(page);
        setSize(size);
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.pageSpecified = true;
        this.page = page > 0 ? page : 1;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.sizeSpecified = true;
        this.size = size > 0 ? Math.min(size, 100) : 10;
    }

    /** Legacy parameter aliases; explicit page/size always take precedence. */
    public long getPageNo() { return getPage(); }

    public void setPageNo(long pageNo) {
        if (!pageSpecified) this.page = pageNo > 0 ? pageNo : 1;
    }

    public long getPageSize() { return getSize(); }

    public void setPageSize(long pageSize) {
        if (!sizeSpecified) this.size = pageSize > 0 ? Math.min(pageSize, 100) : 10;
    }

    public String getSortField() {
        return sortField;
    }

    public void setSortField(String sortField) {
        this.sortField = sortField;
    }

    public String getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = sortOrder;
    }
}
