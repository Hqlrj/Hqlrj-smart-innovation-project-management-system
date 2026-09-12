/**
 * 权限管理工具
 * 根据角色控制菜单和路由访问权限
 */

// 角色菜单权限映射
export const roleMenuMap: Record<string, string[]> = {
  // 系统管理员：拥有所有权限
  '系统管理员': [
    '系统管理',
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ],
  
  // 其他角色：项目管理 + 公告管理
  '项目管理员': [
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ],
  
  '项目负责人': [
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ],
  
  '指导老师': [
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ],
  
  '评委老师': [
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ],
  
  '审批人': [
    '项目管理',
    '公告管理'
    // '项目空间' // 项目空间功能已注释
  ]
}

// 角色路由权限映射（更细粒度的路由控制）
export const roleRouteMap: Record<string, string[]> = {
  // 系统管理员：所有路由
  '系统管理员': [
    'index',
    'user-management',
    'role-management',
    // 'menu-management', // 菜单管理功能已注释
    // 'permission-management', // 权限管理功能已注释
    'project-query',
    'project-detail',
    'project-apply',
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list',
    'notice-publish'
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ],
  
  // 其他所有角色：只有项目管理相关的路由
  '项目管理员': [
    'index',
    'project-query',
    'project-detail',
    'project-apply',
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list',
    'notice-publish'
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ],
  
  '项目负责人': [
    'index',
    'project-query',
    'project-detail',
    'project-apply',
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list' // 项目负责人只能查看公告
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ],
  
  '指导老师': [
    'index',
    'project-query',
    'project-detail',
    'project-apply',
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list',
    'notice-publish'
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ],
  
  '评委老师': [
    'index',
    'project-query',
    'project-detail',
    // 'project-apply', // 评委老师不显示项目申报
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list',
    'notice-publish'
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ],
  
  '审批人': [
    'index',
    'project-query',
    'project-detail',
    // 'project-apply', // 审批人不显示项目申报
    'progress-management',
    // 'project-revoke', // 项目撤销功能已注释
    'project-overview',
    'project-awards',
    'notice-list',
    'notice-publish'
    // 'space-apply', // 项目空间功能已注释
    // 'space-query', // 项目空间功能已注释
    // 'space-inspection' // 项目空间功能已注释
  ]
}

/**
 * 检查角色是否有菜单权限
 * @param role 角色名称
 * @param menuName 菜单名称
 * @returns 是否有权限
 */
export const hasMenuPermission = (role: string | undefined, menuName: string): boolean => {
  if (!role) return false
  
  // 项目负责人：项目管理 + 公告管理
  if (role === '项目负责人') {
    return menuName === '项目管理' || menuName === '公告管理'
  }
  
  // 其他角色按原逻辑
  const allowedMenus = roleMenuMap[role] || []
  return allowedMenus.includes(menuName)
}

/**
 * 检查角色是否有特定路由权限（用于菜单项）
 * @param role 角色名称
 * @param routeName 路由名称
 * @returns 是否有权限
 */
export const hasMenuItemPermission = (role: string | undefined, routeName: string): boolean => {
  return hasRoutePermission(role, routeName)
}

/**
 * 检查角色是否有路由权限
 * @param role 角色名称
 * @param routeName 路由名称
 * @returns 是否有权限
 */
export const hasRoutePermission = (role: string | undefined, routeName: string): boolean => {
  if (!role) return false
  
  // 首页和登录页所有角色都可以访问
  if (routeName === 'index' || routeName === 'login') {
    return true
  }
  
  // 项目负责人只能访问项目管理相关的路由
  if (role === '项目负责人') {
    const allowedRoutes = roleRouteMap[role] || []
    return allowedRoutes.includes(routeName)
  }
  
  // 其他角色按原逻辑
  const allowedRoutes = roleRouteMap[role] || []
  return allowedRoutes.includes(routeName)
}

/**
 * 获取角色允许访问的路由列表
 * @param role 角色名称
 * @returns 允许的路由名称数组
 */
export const getAllowedRoutes = (role: string | undefined): string[] => {
  if (!role) return ['index']
  return roleRouteMap[role] || ['index']
}

