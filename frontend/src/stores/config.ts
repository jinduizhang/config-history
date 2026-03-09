import { defineStore } from 'pinia'
import { ref } from 'vue'
import { configApi } from '@/api/config'
import type { ConfigItem, ConfigRequest, PageResult } from '@/types'

export const useConfigStore = defineStore('config', () => {
  const configs = ref<ConfigItem[]>([])
  const loading = ref(false)
  const total = ref(0)
  const currentPage = ref(1)
  const pageSize = ref(10)

  async function fetchConfigs(page = 1, size = 10) {
    loading.value = true
    try {
      const result: PageResult<ConfigItem> = await configApi.list(page, size)
      configs.value = result.records
      total.value = result.total
      currentPage.value = page
      pageSize.value = size
    } finally {
      loading.value = false
    }
  }

  async function createConfig(data: ConfigRequest) {
    await configApi.create(data)
    await fetchConfigs(currentPage.value, pageSize.value)
  }

  async function updateConfig(id: number, data: ConfigRequest) {
    await configApi.update(id, data)
    await fetchConfigs(currentPage.value, pageSize.value)
  }

  async function deleteConfig(id: number) {
    await configApi.delete(id)
    await fetchConfigs(currentPage.value, pageSize.value)
  }

  return {
    configs,
    loading,
    total,
    currentPage,
    pageSize,
    fetchConfigs,
    createConfig,
    updateConfig,
    deleteConfig
  }
})