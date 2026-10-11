/**
 * Formatting utilities
 */

/**
 * Format an ISO/datetime string to "yyyy-MM-dd HH:mm:ss".
 * Falls back to the raw value if parsing fails.
 */
export function formatDateTime(value?: string | null): string {
  if (!value) return '-'
  try {
    const d = new Date(value)
    if (isNaN(d.getTime())) return value
    const pad = (n: number) => String(n).padStart(2, '0')
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  } catch {
    return value
  }
}

/**
 * Try to parse a string as JSON and pretty-print it.
 * Falls back to the raw string if it's not valid JSON.
 */
export function formatJson(raw?: string | null): string {
  if (!raw) return '-'
  try {
    const parsed = JSON.parse(raw)
    return JSON.stringify(parsed, null, 2)
  } catch {
    return raw
  }
}
