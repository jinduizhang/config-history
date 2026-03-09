import { get, post } from './request'
import type { PageResult } from './request'
import type { HistoryRecord, DiffResult, HistoryQueryParams, TopQueryParams } from '@/types'

const HISTORY_BASE = '/history'

export const historyApi = {
  list: (
    entityType: string, 
    entityId: number, 
    params?: HistoryQueryParams
  ): Promise<PageResult<HistoryRecord>> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}`, { params }),

  getByTime: (
    entityType: string, 
    entityId: number, 
    params?: HistoryQueryParams
  ): Promise<PageResult<HistoryRecord>> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}/by-time`, { params }),

  getTop: (
    entityType: string, 
    entityId: number, 
    params?: TopQueryParams
  ): Promise<HistoryRecord[]> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}/top`, { params }),

  getVersion: (
    entityType: string, 
    entityId: number, 
    versionId: number
  ): Promise<HistoryRecord> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}/${versionId}`),

  getAtTime: (
    entityType: string, 
    entityId: number, 
    targetTime: string
  ): Promise<HistoryRecord> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}/at-time`, { 
      params: { targetTime } 
    }),

  getDiff: (
    entityType: string, 
    entityId: number, 
    from: number, 
    to: number
  ): Promise<DiffResult> => 
    get(`${HISTORY_BASE}/${entityType}/${entityId}/diff`, { 
      params: { from, to } 
    }),

  rollback: (
    entityType: string, 
    entityId: number, 
    versionId: number,
    operator?: string,
    reason?: string
  ): Promise<void> => 
    post(`${HISTORY_BASE}/${entityType}/${entityId}/rollback/${versionId}`, null, {
      params: { operator, reason }
    }),

  rollbackToTime: (
    entityType: string, 
    entityId: number, 
    targetTime: string,
    operator?: string,
    reason?: string
  ): Promise<void> => 
    post(`${HISTORY_BASE}/${entityType}/${entityId}/rollback-to-time`, null, {
      params: { targetTime, operator, reason }
    })
}