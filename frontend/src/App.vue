<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <a-layout class="app-layout">
    <a-layout-header class="app-header">
      <div class="logo">Config History</div>
      <a-menu
        theme="dark"
        mode="horizontal"
        :style="{ lineHeight: '64px' }"
      >
        <a-menu-item key="config" @click="$router.push('/config')">
          Config
        </a-menu-item>
        <a-menu-item key="shell-demo" @click="$router.push('/shell-demo')">
          Shell Demo
        </a-menu-item>
      </a-menu>
    </a-layout-header>
    <a-layout-content class="app-content">
      <RouterView />
    </a-layout-content>
  </a-layout>
</template>

<style scoped>
.app-layout {
  min-height: 100vh;
}

.app-header {
  display: flex;
  align-items: center;
}

.logo {
  color: #fff;
  font-size: 18px;
  font-weight: bold;
  margin-right: 24px;
}

.app-content {
  padding: 24px;
  background: #f0f2f5;
}
</style>