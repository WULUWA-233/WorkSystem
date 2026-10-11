import type { PageQuery, PageResult } from '@/types/api'

/** Load select options without exceeding the server's 100-record page limit. */
export async function fetchAllPages<T extends { id: number }>(
  loadPage: (params: Record<string, unknown>) => Promise<PageResult<T>>,
  filters: Record<string, unknown> = {}
): Promise<T[]> {
  const records: T[] = []
  const ids = new Set<number>()
  for (let pageNo = 1; ; pageNo++) {
    const result = await loadPage({ ...filters, pageNo, pageSize: 100 })
    if (!Array.isArray(result.records) || !Number.isFinite(result.total) || result.total < 0) {
      throw new Error('服务器返回的分页数据无效')
    }
    if (!result.records.length) return records
    const before = records.length
    for (const record of result.records) {
      if (!ids.has(record.id)) { ids.add(record.id); records.push(record) }
    }
    if (records.length >= result.total) return records
    if (records.length === before) throw new Error('服务器未正确处理分页请求')
  }
}

export type RelatedPageQuery = Required<PageQuery>
