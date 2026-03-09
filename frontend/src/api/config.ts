import { get, post, put, del } from './request'
import type { PageResult } from './request'
import type { ConfigItem, ConfigRequest, HistoryRecord, DiffResult } from '@/types'

const CONFIG_BASE = '/configs'

export const configApi = {
  list: (page = 1, pageSize = 10): Promise<PageResult<ConfigItem>> => 
    get(CONFIG_BASE, { params: { page, pageSize } }),

  get: (id: number): Promise<ConfigItem> => 
    get(`${CONFIG_BASE}/${id}`),

  create: (data: ConfigRequest): Promise<ConfigItem> => 
    post(CONFIG_BASE, data),

  update: (id: number, data: ConfigRequest): Promise<ConfigItem> => 
    put(`${CONFIG_BASE}/${id}`, data),

  delete: (id: number): Promise<void> => 
    del(`${CONFIG_BASE}/${id}`),

  getHistory: (id: number, page = 1, pageSize = 10): Promise<PageResult<HistoryRecord>> => 
    get(`${CONFIG_BASE}/${id}/history`, { params: { page, pageSize } }),

  getDiff: (id: number, from: number, to: number): Promise<DiffResult> => 
    get(`${CONFIG_BASE}/${id}/diff`, { params: { from, to } }),

  rollback: (id: number, versionId: number, operator?: string, reason?: string): Promise<void> => 
    post(`${CONFIG_BASE}/${id}/rollback/${versionId}`, null, { 
      params: { operator, reason } 
    })
}