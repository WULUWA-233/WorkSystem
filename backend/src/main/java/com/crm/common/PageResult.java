package com.crm.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Paginated Response Wrapper
 *
 * @param <T> data item type
 * @author CRM Team
 */
public class PageResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Total record count */
    private long total;

    /** Current page number */
    private long page;

    /** Page size */
    private long size;

    /** Data list */
    private List<T> records;

    public PageResult() {
        this.total = 0;
        this.page = 1;
        this.size = 10;
        this.records = Collections.emptyList();
    }

    public PageResult(long total, long page, long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records != null ? records : Collections.emptyList();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long page) {
        this.page = page;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public List<T> getRecords() {
        return records;
    }

    public void setRecords(List<T> records) {
        this.records = records;
    }
}
