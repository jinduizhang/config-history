export interface Result<T> {
  code: number
  message: string
  data: T
}

export interface PageResult<T> {
  records: T[]
  total: number
  page: number
  pageSize: number
}

export interface ConfigItem {
  id: number
  configKey: string
  configName: string
  configValue: string
  description: string
  createdAt: string
  updatedAt: string
}

export interface ConfigRequest {
  configKey: string
  configName: string
  configValue: string
  description?: string
}

export interface HistoryRecord {
  id: number
  entityType: string
  entityId: number
  versionNo: number
  snapshot: string
  changeType: string
  changeFields: string[]
  operator: string
  operatorIp: string
  changeReason: string
  createdAt: string
}

export interface DiffResult {
  sourceVersion: number
  targetVersion: number
  sourceValue: string
  targetValue: string
  differences: Record<string, DiffItem>
}

export interface DiffItem {
  type: 'ADD' | 'DELETE' | 'MODIFY'
  oldValue: unknown
  newValue: unknown
  displayName: string
}

export interface HistoryQueryParams {
  page?: number
  pageSize?: number
  startTime?: string
  endTime?: string
  sortBy?: string
  sortOrder?: 'asc' | 'desc'
}

export interface TopQueryParams {
  limit?: number
  sortOrder?: 'asc' | 'desc'
}