<template>
  <div class="common-layout">
    <el-container>
      <!-- 左侧导航栏 -->
      <el-aside width="200px" class="sidebar-aside">
        <nav class="sidebar-nav">
          <!-- 首页 -->
          <router-link to="/index" class="nav-item home-item" active-class="router-link-active">
            <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z" />
              <polyline points="9 22 9 12 15 12 15 22" />
            </svg>
            <span>首页</span>
          </router-link>
          <!-- 系统管理：仅系统管理员和项目管理员可见 -->
          <div v-if="canAccessMenu('系统管理')" class="nav-section">
            <div class="nav-section-header" @click="toggleSection('system')">
              <span class="nav-icon">{{ sections.system.expanded ? '◎' : '○' }}</span>
              <span>系统管理</span>
              <span class="nav-arrow">{{ sections.system.expanded ? '▼' : '▶' }}</span>
            </div>
            <div v-if="sections.system.expanded" class="nav-section-items">
              <router-link v-if="canAccessMenuItem('user-management')" to="/user-management" class="nav-item" active-class="router-link-active">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                <span>用户管理</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('role-management')" to="/role-management" class="nav-item" active-class="router-link-active">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" />
                  <circle cx="9" cy="7" r="4" />
                  <path d="M23 21v-2a4 4 0 0 0-3-3.87" />
                  <path d="M16 3.13a4 4 0 0 1 0 7.75" />
                </svg>
                <span>角色管理</span>
              </router-link>
              <!-- 菜单管理功能已注释 -->
              <!-- <router-link v-if="canAccessMenuItem('menu-management')" to="/menu-management" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <line x1="8" y1="6" x2="21" y2="6" />
                  <line x1="8" y1="12" x2="21" y2="12" />
                  <line x1="8" y1="18" x2="21" y2="18" />
                  <line x1="3" y1="6" x2="3.01" y2="6" />
                  <line x1="3" y1="12" x2="3.01" y2="12" />
                  <line x1="3" y1="18" x2="3.01" y2="18" />
                </svg>
                <span>菜单管理</span>
              </router-link> -->
              <!-- 权限管理功能已注释 -->
              <!-- <router-link v-if="canAccessMenuItem('permission-management')" to="/permission-management" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
                <span>权限管理</span>
              </router-link> -->
            </div>
          </div>

          <!-- 项目管理：所有角色可见 -->
          <div v-if="canAccessMenu('项目管理')" class="nav-section">
            <div class="nav-section-header" @click="toggleSection('project')">
              <span class="nav-icon">{{ sections.project.expanded ? '◎' : '○' }}</span>
              <span>项目管理</span>
              <span class="nav-arrow">{{ sections.project.expanded ? '▼' : '▶' }}</span>
            </div>
            <div v-if="sections.project.expanded" class="nav-section-items">
              <router-link v-if="canAccessMenuItem('project-query')" to="/project-query" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21l-4.35-4.35" />
                </svg>
                <span>项目查询</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('project-apply')" to="/project-apply" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                  <polyline points="14 2 14 8 20 8" />
                  <line x1="12" y1="18" x2="12" y2="12" />
                  <line x1="9" y1="15" x2="15" y2="15" />
                </svg>
                <span>项目申报</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('progress-management')" to="/progress-management" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
                </svg>
                <span>进度管理</span>
              </router-link>
              <!-- 项目撤销功能已注释 -->
              <!-- <router-link v-if="canAccessMenuItem('project-revoke')" to="/project-revoke" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="3 6 5 6 21 6" />
                  <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                  <line x1="10" y1="11" x2="10" y2="17" />
                  <line x1="14" y1="11" x2="14" y2="17" />
                </svg>
                <span>项目撤销</span>
              </router-link> -->
              <!-- 项目一览功能已注释 -->
              <router-link v-if="canAccessMenuItem('project-overview')" to="/project-overview" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="3" width="7" height="7" />
                  <rect x="14" y="3" width="7" height="7" />
                  <rect x="14" y="14" width="7" height="7" />
                  <rect x="3" y="14" width="7" height="7" />
                </svg>
                <span>项目一览</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('project-awards')" to="/project-awards" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
                  <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
                  <path d="M4 22h16" />
                  <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
                  <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
                  <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
                </svg>
                <span>项目获奖记录</span>
              </router-link>
            </div>
          </div>

          <!-- 公告管理：所有角色可见（发布权限在前后端控制） -->
          <div v-if="canAccessMenu('公告管理')" class="nav-section">
            <div class="nav-section-header" @click="toggleSection('notice')">
              <span class="nav-icon">{{ sections.notice.expanded ? '◎' : '○' }}</span>
              <span>公告管理</span>
              <span class="nav-arrow">{{ sections.notice.expanded ? '▼' : '▶' }}</span>
            </div>
            <div v-if="sections.notice.expanded" class="nav-section-items">
              <router-link v-if="canAccessMenuItem('notice-list')" to="/notice-list" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="4" width="18" height="16" rx="2" />
                  <line x1="7" y1="8" x2="17" y2="8" />
                  <line x1="7" y1="12" x2="17" y2="12" />
                  <line x1="7" y1="16" x2="13" y2="16" />
                </svg>
                <span>查看公告</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('notice-publish')" to="/notice-publish" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14" />
                  <path d="M5 12h14" />
                  <rect x="3" y="3" width="18" height="18" rx="2" />
                </svg>
                <span>发布公告</span>
              </router-link>
            </div>
          </div>

          <!-- 项目空间功能已注释 -->
          <!-- <div v-if="canAccessMenu('项目空间')" class="nav-section">
            <div class="nav-section-header" @click="toggleSection('space')">
              <span class="nav-icon">{{ sections.space.expanded ? '◎' : '○' }}</span>
              <span>项目空间</span>
              <span class="nav-arrow">{{ sections.space.expanded ? '▼' : '▶' }}</span>
            </div>
            <div v-if="sections.space.expanded" class="nav-section-items">
              <router-link v-if="canAccessMenuItem('space-apply')" to="/space-apply" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 5v14" />
                  <path d="M5 12h14" />
                  <rect x="3" y="3" width="18" height="18" rx="2" />
                </svg>
                <span>项目空间申请</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('space-query')" to="/space-query" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <circle cx="11" cy="11" r="8" />
                  <path d="M21 21l-4.35-4.35" />
                </svg>
                <span>项目空间查询</span>
              </router-link>
              <router-link v-if="canAccessMenuItem('space-inspection')" to="/space-inspection" class="nav-item">
                <svg class="nav-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <polyline points="20 6 9 17 4 12" />
                </svg>
                <span>空间卫生检查</span>
              </router-link>
            </div>
          </div> -->
        </nav>
      </el-aside>

      <!-- 右侧容器（包含顶部和主内容） -->
      <el-container>
        <!-- 顶部导航栏 -->
        <el-header class="header-container">
          <!-- 通知铃铛（左侧） -->
          <div class="notification-bell" @click="showNotificationPanel = true">
            <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="99">
              <el-icon :size="20" style="cursor:pointer;color:#333"><Bell /></el-icon>
            </el-badge>
          </div>
          <div class="header-right">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-profile">
                <img
                  :src="headerAvatarUrl"
                  alt="User"
                  class="avatar"
                  @error="handleHeaderAvatarError"
                />
                <span class="user-name">{{ displayName }}</span>
                <span class="dropdown-arrow">▼</span>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                  <el-dropdown-item command="password">修改密码</el-dropdown-item>
                  <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </el-header>

        <!-- 通知弹窗（居中） -->
        <el-dialog
          v-model="showNotificationPanel"
          title="消息通知"
          width="600px"
          :close-on-click-modal="true"
          @open="loadNotifications"
        >
          <template #header>
            <div class="notification-drawer-header">
              <span class="notification-drawer-title">消息通知</span>
              <el-button
                v-if="unreadCount > 0"
                text
                type="primary"
                size="small"
                @click="handleMarkAllRead"
              >全部已读</el-button>
            </div>
          </template>
          <div v-loading="notificationLoading" class="notification-drawer-body" style="max-height: 500px; overflow-y: auto;">
            <div v-if="notifications.length === 0 && !notificationLoading" class="notification-empty">
              暂无通知
            </div>
            <div
              v-for="item in notifications"
              :key="item.id"
              class="notification-item"
              :class="{ 'notification-item--unread': !item.isRead }"
              @click="handleNotificationClick(item)"
            >
              <span class="notification-tag" :class="'tag--' + getNotificationTagType(item.type)">
                {{ getNotificationTagLabel(item.type) }}
              </span>
              <div class="notification-item-body">
                <div class="notification-item-content">{{ item.content }}</div>
              </div>
              <div class="notification-item-time">{{ item.createTime }}</div>
            </div>
          </div>
          <template #footer v-if="notificationTotal > 10">
            <div class="notification-pagination">
              <el-pagination
                small
                layout="prev, pager, next"
                :total="notificationTotal"
                :page-size="10"
                :current-page="notificationPage"
                @current-change="handleNotificationPageChange"
              />
            </div>
          </template>
        </el-dialog>

        <!-- 主内容区域 -->
        <el-main class="main-content">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
    <!-- 个人中心弹窗 -->
    <el-dialog 
      v-model="showProfileDialog" 
      title="" 
      width="680px" 
      @close="resetProfileForm"
      class="profile-dialog"
      :close-on-click-modal="false"
    >
      <template #header>
        <div class="profile-dialog-header">
          <div class="header-icon">
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle cx="12" cy="7" r="4" />
            </svg>
          </div>
          <h3 class="header-title">个人中心</h3>
        </div>
      </template>
      
      <div class="profile-dialog-body">
        <!-- 用户头像和基本信息区域 -->
        <div class="profile-header-section">
          <div class="avatar-container">
            <div class="avatar-wrapper">
              <img
                :src="avatarUrl"
                alt="头像"
                class="profile-avatar"
                @error="handleAvatarError"
              />
              <div class="avatar-upload-overlay" @click="triggerAvatarUpload">
                <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                  <polyline points="17 8 12 3 7 8" />
                  <line x1="12" y1="3" x2="12" y2="15" />
                </svg>
                <span>上传头像</span>
              </div>
              <div class="avatar-badge">
                <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M12 2L2 7l10 5 10-5-10-5z" />
                  <path d="M2 17l10 5 10-5" />
                  <path d="M2 12l10 5 10-5" />
                </svg>
              </div>
              <input
                ref="avatarInputRef"
                type="file"
                accept="image/jpeg,image/jpg,image/png,image/gif"
                style="display: none"
                @change="handleAvatarChange"
              />
            </div>
          </div>
          <div class="profile-basic-info">
            <h4 class="user-name-display">{{ profileForm.name || '用户' }}</h4>
            <p class="user-role-display">
              <span class="role-badge">{{ profileForm.role || '未设置' }}</span>
              <span class="status-badge" :class="getStatusClass(profileForm.status)">
                {{ profileForm.status || '未知' }}
              </span>
            </p>
          </div>
        </div>

        <!-- 表单区域 -->
        <div class="profile-form-section">
          <el-form :model="profileForm" label-width="110px" :rules="profileRules" ref="profileFormRef" class="profile-form">
            <div class="form-row">
              <el-form-item label="学号/职工号" prop="studentId" class="form-item-custom">
                <el-input 
                  v-model="profileForm.studentId" 
                  placeholder="请输入学号/职工号"
                  class="custom-input"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="姓名" prop="name" class="form-item-custom">
                <el-input 
                  v-model="profileForm.name" 
                  placeholder="请输入姓名"
                  class="custom-input"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="手机号" prop="phone" class="form-item-custom">
                <el-input 
                  v-model="profileForm.phone" 
                  placeholder="请输入手机号"
                  class="custom-input"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="学院" prop="college" class="form-item-custom">
                <el-select 
                  v-model="profileForm.college" 
                  placeholder="请选择学院" 
                  class="custom-select"
                >
                  <el-option label="信息工程学院" value="信息工程学院" />
                  <el-option label="体育学院" value="体育学院" />
                  <el-option label="外国语学院" value="外国语学院" />
                  <el-option label="商学院" value="商学院" />
                  <el-option label="语言与传媒学院" value="语言与传媒学院" />
                  <el-option label="艺术学院" value="艺术学院" />
                  <el-option label="交通与环境学院" value="交通与环境学院" />
                </el-select>
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="角色" class="form-item-custom">
                <el-input 
                  v-model="profileForm.role" 
                  disabled
                  class="custom-input disabled-input"
                />
              </el-form-item>
            </div>
            
            <div class="form-row">
              <el-form-item label="状态" class="form-item-custom">
                <el-input 
                  v-model="profileForm.status" 
                  disabled
                  class="custom-input disabled-input"
                />
              </el-form-item>
            </div>
          </el-form>
        </div>
      </div>
      
      <template #footer>
        <div class="profile-dialog-footer">
          <el-button 
            class="cancel-btn" 
            @click="() => { showProfileDialog = false; resetProfileForm() }"
          >
            取消
          </el-button>
          <el-button 
            type="primary" 
            :loading="profileLoading" 
            @click="handleProfileConfirm"
            class="save-btn"
          >
            <span v-if="!profileLoading">保存修改</span>
            <span v-else>保存中...</span>
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 修改密码弹窗 -->
    <el-dialog v-model="showPasswordDialog" title="修改密码" width="420px" @close="resetPasswordForm">
      <div class="dialog-body">
        <el-form label-width="90px">
          <el-form-item label="旧密码">
            <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入旧密码" />
          </el-form-item>
          <el-form-item label="新密码">
            <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码">
            <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="() => { showPasswordDialog = false; resetPasswordForm() }">取消</el-button>
          <el-button type="primary" :loading="passwordLoading" @click="handlePasswordConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, watch, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import { useLoginStore } from '@/stores/login'
import { changePassword, type ChangePasswordRequest, getUserById, updateUser, uploadAvatar, type User } from '@/api/user'
import { hasMenuPermission, hasMenuItemPermission } from '@/utils/permission'
import { getNotificationList, getUnreadCount, markAsRead, markAllRead, type Notification } from '@/api/notification'
import { onUnmounted } from 'vue'
import { Bell } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const loginStore = useLoginStore()

// 获取当前用户角色
const userRole = computed(() => loginStore.userInfo?.role || '')

// 检查菜单权限
const canAccessMenu = (menuName: string) => {
  return hasMenuPermission(userRole.value, menuName)
}

// 检查菜单项权限
const canAccessMenuItem = (routeName: string) => {
  return hasMenuItemPermission(userRole.value, routeName)
}

// 初始化所有分组为折叠状态（默认不自动打开）
const sections = reactive({
  system: { expanded: false },
  project: { expanded: false },
  notice: { expanded: false },
  space: { expanded: false }
})

const displayName = computed(() => {
  return loginStore.userInfo?.name || loginStore.userInfo?.username || '用户'
})

// 顶部头像URL
const headerAvatarUrl = computed(() => {
  const avatar = loginStore.userInfo?.avatar
  if (avatar) {
    if (avatar.startsWith('/uploads/')) {
      return `/api${avatar}`
    }
    return avatar
  }
  return 'https://via.placeholder.com/40/cccccc/ffffff?text=User'
})

// 处理顶部头像加载错误
const handleHeaderAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/40/cccccc/ffffff?text=User'
}

// 切换分组展开/折叠状态
const toggleSection = (section: keyof typeof sections) => {
  sections[section].expanded = !sections[section].expanded
}

// 根据当前路由自动展开对应的分组
const expandSectionByRoute = () => {
  const path = route.path
  
  // 系统管理相关路由
  if (path.includes('user-management') || 
      path.includes('role-management')) {
      // path.includes('menu-management') || // 菜单管理已注释
      // path.includes('permission-management')) { // 权限管理已注释
    sections.system.expanded = true
  }
  
  // 项目管理相关路由
  if (path.includes('project-query') || 
      path.includes('project-apply') || 
      path.includes('progress-management') || 
      // path.includes('project-revoke') || // 项目撤销已注释
      path.includes('project-overview') ||
      path.includes('project-awards')) {
    sections.project.expanded = true
  }

  // 公告管理相关路由
  if (path.includes('notice-list') || path.includes('notice-publish')) {
    sections.notice.expanded = true
  }

  // 项目空间相关路由已注释
  // if (path.includes('space-apply') || 
  //     path.includes('space-query') || 
  //     path.includes('space-inspection')) {
  //   sections.space.expanded = true
  // }
}

// 监听路由变化，自动展开对应的分组
watch(() => route.path, () => {
  expandSectionByRoute()
}, { immediate: true })

// 组件挂载时也执行一次
onMounted(() => {
  expandSectionByRoute()
  loadUnreadCount()
  notificationPollTimer = window.setInterval(loadUnreadCount, 30000)
})

onUnmounted(() => {
  if (notificationPollTimer) {
    clearInterval(notificationPollTimer)
  }
})

// ==================== 通知模块 ====================
const showNotificationPanel = ref(false)
const notifications = ref<Notification[]>([])
const notificationLoading = ref(false)
const unreadCount = ref(0)
const notificationTotal = ref(0)
const notificationPage = ref(1)
let notificationPollTimer: number | null = null

const loadNotifications = async () => {
  notificationLoading.value = true
  try {
    const res = await getNotificationList({ page: notificationPage.value, pageSize: 10 })
    const data = res as any
    notifications.value = data?.list || []
    notificationTotal.value = data?.total || 0
  } catch {
    notifications.value = []
  } finally {
    notificationLoading.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const res = await getUnreadCount()
    unreadCount.value = (res as any) ?? 0
  } catch {
    // 静默
  }
}

const handleNotificationClick = async (item: Notification) => {
  if (!item.isRead) {
    await markAsRead(item.id)
    item.isRead = 1
    unreadCount.value = Math.max(0, unreadCount.value - 1)
  }
  showNotificationPanel.value = false
  if (item.type === 'project_approve' || item.type === 'project_reject') {
    router.push(`/project-detail/${item.relatedId}`)
  } else if (item.type === 'award_approve' || item.type === 'award_reject') {
    router.push('/project-awards')
  } else if (item.type === 'notice_publish') {
    router.push('/notice-list')
  }
}

const handleMarkAllRead = async () => {
  await markAllRead()
  notifications.value.forEach(n => { n.isRead = 1 })
  unreadCount.value = 0
}

const handleNotificationPageChange = (page: number) => {
  notificationPage.value = page
  loadNotifications()
}

// 通知类型标签映射
const notificationTagMap: Record<string, { label: string; type: string }> = {
  project_approve: { label: '项目审批', type: 'success' },
  project_reject: { label: '项目驳回', type: 'danger' },
  award_approve: { label: '获奖记录', type: 'primary' },
  award_reject: { label: '获奖驳回', type: 'warning' },
  notice_publish: { label: '系统消息', type: 'info' }
}

const getNotificationTagLabel = (type: string) => {
  return notificationTagMap[type]?.label || '系统消息'
}

const getNotificationTagType = (type: string) => {
  return notificationTagMap[type]?.type || 'info'
}

const handleBack = () => {
  router.go(-1)
}

// 处理头像下拉菜单
const handleUserCommand = async (command: string) => {
  if (command === 'profile') {
    await loadUserProfile()
    showProfileDialog.value = true
    return
  }

  if (command === 'password') {
    showPasswordDialog.value = true
    return
  }

  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        type: 'warning',
        confirmButtonText: '确定',
        cancelButtonText: '取消'
      })
      loginStore.logout()
      router.push('/login')
    } catch (e) {
      // 用户取消，不做处理
    }
  }
}

// 个人中心弹窗
const showProfileDialog = ref(false)
const profileLoading = ref(false)
const avatarUploading = ref(false)
const profileFormRef = ref<FormInstance>()
const avatarInputRef = ref<HTMLInputElement>()
const profileForm = ref({
  id: '',
  studentId: '',
  name: '',
  phone: '',
  college: '',
  role: '',
  status: '',
  avatar: ''
})

// 头像URL（用于显示）
const avatarUrl = computed(() => {
  if (profileForm.value.avatar) {
    // 如果是相对路径，添加API前缀
    if (profileForm.value.avatar.startsWith('/uploads/')) {
      return `/api${profileForm.value.avatar}`
    }
    return profileForm.value.avatar
  }
  // 默认头像
  return 'https://via.placeholder.com/100/1976d2/ffffff?text=User'
})

// 触发头像上传
const triggerAvatarUpload = () => {
  avatarInputRef.value?.click()
}

// 处理头像选择
const handleAvatarChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  // 验证文件类型
  const validTypes = ['image/jpeg', 'image/jpg', 'image/png', 'image/gif']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('只支持上传图片文件（.jpg、.jpeg、.png、.gif格式）')
    return
  }

  // 验证文件大小（限制5MB）
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.error('图片大小不能超过5MB')
    return
  }

  if (!loginStore.userInfo?.id) {
    ElMessage.error('用户信息缺失，请重新登录')
    return
  }

  avatarUploading.value = true
  try {
    const avatarPath = await uploadAvatar(file, Number(loginStore.userInfo.id)) as any as string
    profileForm.value.avatar = avatarPath
    
    // 更新登录store中的用户信息
    if (loginStore.userInfo) {
      loginStore.userInfo.avatar = avatarPath
      localStorage.setItem('userInfo', JSON.stringify(loginStore.userInfo))
    }
    
    ElMessage.success('头像上传成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '头像上传失败')
  } finally {
    avatarUploading.value = false
    // 清空input，以便可以重复选择同一文件
    if (target) {
      target.value = ''
    }
  }
}

// 处理头像加载错误
const handleAvatarError = (event: Event) => {
  const img = event.target as HTMLImageElement
  img.src = 'https://via.placeholder.com/100/1976d2/ffffff?text=User'
}

// 个人中心表单验证规则
const profileRules = reactive<FormRules>({
  studentId: [
    { required: true, message: '请输入学号/职工号', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  college: [
    { required: true, message: '请选择学院', trigger: 'change' }
  ]
})

// 加载用户信息
const loadUserProfile = async () => {
  if (!loginStore.userInfo?.id) {
    ElMessage.error('用户信息缺失，请重新登录')
    return
  }
  try {
    profileLoading.value = true
    const response = await getUserById(String(loginStore.userInfo.id))
    // 响应拦截器已经处理了数据，直接使用
    const user = response as any as User
    profileForm.value = {
      id: String(user.id || ''),
      studentId: user.studentId || '',
      name: user.name || '',
      phone: user.phone || '',
      college: user.college || '',
      role: user.role || '',
      status: user.status || '',
      avatar: user.avatar || ''
    }
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '加载用户信息失败')
  } finally {
    profileLoading.value = false
  }
}

// 重置个人中心表单
const resetProfileForm = () => {
  profileFormRef.value?.resetFields()
  profileForm.value = {
    id: '',
    studentId: '',
    name: '',
    phone: '',
    college: '',
    role: '',
    status: '',
    avatar: ''
  }
}

// 获取状态样式类
const getStatusClass = (status: string) => {
  if (status === '已审批') return 'status-approved'
  if (status === '待审批') return 'status-pending'
  if (status === '已停用') return 'status-disabled'
  return 'status-default'
}

// 保存个人中心信息
const handleProfileConfirm = async () => {
  if (!profileFormRef.value) return
  
  await profileFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    if (!profileForm.value.id) {
      ElMessage.error('用户ID缺失，请重新登录')
      return
    }
    
    profileLoading.value = true
    try {
      const updateData: Partial<User> = {
        studentId: profileForm.value.studentId,
        name: profileForm.value.name,
        phone: profileForm.value.phone,
        college: profileForm.value.college
      }
      
      await updateUser(profileForm.value.id, updateData)
      ElMessage.success('个人信息更新成功')
      
      // 更新登录store中的用户信息
      if (loginStore.userInfo) {
        loginStore.userInfo.name = profileForm.value.name
        loginStore.userInfo.studentId = profileForm.value.studentId
        loginStore.userInfo.phone = profileForm.value.phone
        loginStore.userInfo.college = profileForm.value.college
        if (profileForm.value.avatar) {
          loginStore.userInfo.avatar = profileForm.value.avatar
        }
        // 更新localStorage
        localStorage.setItem('userInfo', JSON.stringify(loginStore.userInfo))
      }
      
      showProfileDialog.value = false
    } catch (error: any) {
      ElMessage.error(error?.response?.data?.msg || '更新个人信息失败')
    } finally {
      profileLoading.value = false
    }
  })
}

// 修改密码弹窗
const showPasswordDialog = ref(false)
const passwordLoading = ref(false)
const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const resetPasswordForm = () => {
  passwordForm.value = {
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
}

const handlePasswordConfirm = async () => {
  if (passwordForm.value.newPassword !== passwordForm.value.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }
  if (!loginStore.userInfo?.id) {
    ElMessage.error('用户信息缺失，请重新登录')
    router.push('/login')
    return
  }
  passwordLoading.value = true
  try {
    const payload: ChangePasswordRequest = {
      userId: Number(loginStore.userInfo.id),
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    }
    await changePassword(payload)
    ElMessage.success('密码修改成功，请重新登录')
    resetPasswordForm()
    showPasswordDialog.value = false
    // 退出登录
    loginStore.logout()
    router.push('/login')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '修改密码失败')
  } finally {
    passwordLoading.value = false
  }
}
</script>

<style scoped>
.common-layout {
  min-height: 100vh;
  background: #f5f5f5;
}

/* Element Plus 容器样式覆盖 */
:deep(.el-container) {
  min-height: 100vh;
}

/* 左侧导航栏 */
.sidebar-aside {
  background: linear-gradient(180deg, #ffffff 0%, #fbfcfe 100%);
  color: #333;
  border-right: 1px solid rgba(15, 23, 42, 0.08);
  overflow-y: auto;
}

/* 顶部导航栏 */
.header-container {
  height: 60px;
  background: white;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 100;
}

.header-left {
  display: flex;
  gap: 20px;
}

.header-link {
  color: #333;
  text-decoration: none;
  font-size: 14px;
  transition: color 0.3s;
}

.header-link:hover {
  color: #1976d2;
}

.header-right {
  display: flex;
  align-items: center;
  margin-left: auto;
}

.user-profile {
  display: flex;
  align-items: center;
  cursor: pointer;
  gap: 5px;
}

.user-name {
  font-size: 14px;
  color: #333;
}

.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

.dropdown-arrow {
  font-size: 12px;
  color: #666;
}

.dialog-body {
  padding: 20px 0;
}

/* 个人中心弹窗美化样式 */
.profile-dialog {
  border-radius: 16px;
  overflow: hidden;
}

:deep(.profile-dialog .el-dialog) {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15);
}

:deep(.profile-dialog .el-dialog__header) {
  padding: 0;
  border-bottom: none;
}

:deep(.profile-dialog .el-dialog__body) {
  padding: 0;
}

:deep(.profile-dialog .el-dialog__footer) {
  padding: 0;
  border-top: none;
}

/* 弹窗头部 */
.profile-dialog-header {
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  padding: 24px 30px;
  display: flex;
  align-items: center;
  gap: 12px;
  color: white;
}

.header-icon {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(10px);
}

.header-icon svg {
  color: white;
}

.header-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: white;
}

/* 弹窗主体 */
.profile-dialog-body {
  padding: 30px;
  background: #f8f9fa;
}

/* 用户头像和基本信息区域 */
.profile-header-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 24px;
  display: flex;
  align-items: center;
  gap: 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.avatar-container {
  flex-shrink: 0;
}

.avatar-wrapper {
  position: relative;
  width: 100px;
  height: 100px;
  cursor: pointer;
}

.profile-avatar {
  width: 100px;
  height: 100px;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #e3f2fd;
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.2);
  transition: all 0.3s ease;
}

.avatar-upload-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s ease;
  color: white;
  font-size: 12px;
  gap: 4px;
}

.avatar-wrapper:hover .avatar-upload-overlay {
  opacity: 1;
}

.avatar-upload-overlay svg {
  width: 24px;
  height: 24px;
}

.avatar-upload-overlay span {
  font-size: 11px;
  font-weight: 500;
}

.avatar-badge {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid white;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.avatar-badge svg {
  color: white;
  width: 16px;
  height: 16px;
}

.profile-basic-info {
  flex: 1;
}

.user-name-display {
  margin: 0 0 12px 0;
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.user-role-display {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.role-badge {
  display: inline-block;
  padding: 6px 14px;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  color: #1976d2;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-badge {
  display: inline-block;
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.status-approved {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  color: #2e7d32;
}

.status-pending {
  background: linear-gradient(135deg, #fff3e0 0%, #ffe0b2 100%);
  color: #f57c00;
}

.status-disabled {
  background: linear-gradient(135deg, #fce4ec 0%, #f8bbd0 100%);
  color: #c2185b;
}

.status-default {
  background: #f5f5f5;
  color: #757575;
}

/* 表单区域 */
.profile-form-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.profile-form {
  margin: 0;
}

.form-row {
  margin-bottom: 20px;
}

.form-row:last-child {
  margin-bottom: 0;
}

.form-item-custom {
  margin-bottom: 0;
}

:deep(.form-item-custom .el-form-item__label) {
  font-weight: 500;
  color: #333;
  font-size: 14px;
  padding-bottom: 8px;
}

:deep(.form-item-custom .el-form-item__content) {
  line-height: normal;
}

.custom-input {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.custom-input .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  transition: all 0.3s ease;
}

:deep(.custom-input .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #1976d2 inset;
}

:deep(.custom-input.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2) inset;
}

:deep(.custom-input .el-input__inner) {
  font-size: 14px;
  color: #333;
}

.disabled-input {
  cursor: not-allowed;
}

:deep(.disabled-input .el-input__wrapper) {
  background-color: #f5f7fa;
  box-shadow: 0 0 0 1px #e4e7ed inset;
}

:deep(.disabled-input .el-input__inner) {
  color: #909399;
  cursor: not-allowed;
}

.custom-select {
  width: 100%;
}

:deep(.custom-select .el-input__wrapper) {
  border-radius: 8px;
  box-shadow: 0 0 0 1px #e0e0e0 inset;
  transition: all 0.3s ease;
}

:deep(.custom-select .el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #1976d2 inset;
}

:deep(.custom-select.is-focus .el-input__wrapper) {
  box-shadow: 0 0 0 2px rgba(25, 118, 210, 0.2) inset;
}

/* 弹窗底部 */
.profile-dialog-footer {
  background: white;
  padding: 20px 30px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  border-top: 1px solid #f0f0f0;
}

.cancel-btn {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.cancel-btn:hover {
  background-color: #f5f5f5;
  border-color: #d0d0d0;
}

.save-btn {
  padding: 10px 24px;
  border-radius: 8px;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  border: none;
  font-weight: 500;
  transition: all 0.3s ease;
  box-shadow: 0 2px 8px rgba(25, 118, 210, 0.3);
}

.save-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.4);
}

.save-btn:active:not(:disabled) {
  transform: translateY(0);
}

:deep(.save-btn.is-loading) {
  opacity: 0.8;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .profile-dialog-body {
    padding: 20px;
  }

  .profile-header-section {
    flex-direction: column;
    text-align: center;
    padding: 20px;
  }

  .profile-form-section {
    padding: 20px;
  }

  .profile-dialog-footer {
    padding: 16px 20px;
    flex-direction: column-reverse;
  }

  .cancel-btn,
  .save-btn {
    width: 100%;
  }
}

/* 主内容区域 */
.main-content {
  background: white;
  padding: 0;
  overflow-y: auto;
}

.sidebar-nav {
  padding: 12px 8px;
}

.nav-section {
  margin-bottom: 8px;
}

.nav-section-header {
  padding: 10px 12px;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 10px;
  border-radius: 10px;
  transition: background 0.2s ease, color 0.2s ease, box-shadow 0.2s ease;
  font-size: 14px;
  color: #333;
  font-weight: 500;
  margin: 0 4px;
}

.nav-section-header:hover {
  background: rgba(25, 118, 210, 0.08);
  box-shadow: 0 1px 0 rgba(15, 23, 42, 0.04);
}

.nav-icon {
  font-size: 14px;
  width: 16px;
  height: 16px;
  text-align: center;
  flex-shrink: 0;
  color: rgba(51, 65, 85, 0.9);
}

.nav-arrow {
  margin-left: auto;
  font-size: 12px;
  color: rgba(71, 85, 105, 0.9);
}

.nav-section-items {
  background: transparent;
  padding: 6px 0 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  margin: 2px 8px;
  border-radius: 10px;
  color: rgba(71, 85, 105, 0.95);
  text-decoration: none;
  font-size: 14px;
  transition: background 0.2s ease, color 0.2s ease, transform 0.08s ease;
}

.home-item {
  padding: 10px 12px;
  margin: 2px 8px 10px;
  border-radius: 10px;
}

.nav-item:hover {
  background: rgba(25, 118, 210, 0.08);
  color: #1976d2;
}

.nav-item.router-link-active {
  background: linear-gradient(90deg, rgba(25, 118, 210, 0.18) 0%, rgba(25, 118, 210, 0.08) 100%);
  color: #1976d2;
  font-weight: 600;
  position: relative;
}

.nav-item.router-link-active::before {
  content: '';
  position: absolute;
  left: 0;
  top: 8px;
  bottom: 8px;
  width: 3px;
  border-radius: 999px;
  background: #1976d2;
}

/* 图标在菜单项里更柔和一点 */
.nav-item .nav-icon {
  color: rgba(100, 116, 139, 0.95);
}

.nav-item.router-link-active .nav-icon,
.nav-item:hover .nav-icon {
  color: currentColor;
}

/* 滚动条样式 */
.sidebar-aside::-webkit-scrollbar,
.main-content::-webkit-scrollbar {
  width: 6px;
}

.sidebar-aside::-webkit-scrollbar-track,
.main-content::-webkit-scrollbar-track {
  background: transparent;
}

.sidebar-aside::-webkit-scrollbar-thumb {
  background: rgba(15, 23, 42, 0.18);
  border-radius: 3px;
}

.sidebar-aside::-webkit-scrollbar-thumb:hover {
  background: rgba(15, 23, 42, 0.28);
}

.main-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

/* ===== 通知面板样式 ===== */
.notification-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background-color 0.2s;
}

.notification-item:hover {
  background-color: #f5f7fa;
}

.notification-item--unread {
  background-color: #f0f7ff;
}

.notification-item--unread:hover {
  background-color: #e8f2ff;
}

/* 类型标签 */
.notification-tag {
  flex-shrink: 0;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
  white-space: nowrap;
  line-height: 20px;
}

.tag--success {
  background-color: #e6f7e9;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.tag--danger {
  background-color: #fff1f0;
  color: #ff4d4f;
  border: 1px solid #ffa39e;
}

.tag--primary {
  background-color: #e6f4ff;
  color: #1677ff;
  border: 1px solid #91caff;
}

.tag--warning {
  background-color: #fffbe6;
  color: #faad14;
  border: 1px solid #ffe58f;
}

.tag--info {
  background-color: #f0f0f0;
  color: #666;
  border: 1px solid #d9d9d9;
}

/* 内容 */
.notification-item-body {
  flex: 1;
  min-width: 0;
}

.notification-item-content {
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.notification-item-time {
  flex-shrink: 0;
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  padding-left: 8px;
}

.notification-bell {
  display: flex;
  align-items: center;
  cursor: pointer;
  margin-right: 20px;
}

.notification-drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
}

.notification-drawer-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
}

.notification-drawer-body {
  min-height: 200px;
}

.notification-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: #999;
  font-size: 14px;
}

.notification-pagination {
  display: flex;
  justify-content: center;
  padding: 8px 0;
}
</style>

