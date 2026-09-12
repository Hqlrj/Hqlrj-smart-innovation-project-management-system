import { createRouter, createWebHistory } from "vue-router";
import { useLoginStore } from "@/stores/login";
import { hasRoutePermission } from "@/utils/permission";
import { ElMessage } from "element-plus";

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("../views/Login/index.vue"), // 登录页
    },
    {
      path: "/",
      name: "layout",
      component: () => import("../views/Layout/index.vue"), // 主布局组件
      redirect: "/index", // 访问根路径时重定向到首页
      // 子路由配置（在主布局内显示）
      children: [
        {
          path: "index",
          name: "index",
          component: () => import("../views/index/index.vue"), // 首页
        },
        {
          path: "user-management",
          name: "user-management",
          component: () => import("../views/UserManagement/index.vue"), // 用户管理
        },
        {
          path: "role-management",
          name: "role-management",
          component: () => import("../views/RoleManagement/index.vue"), // 角色管理
        },
        // 菜单管理功能已注释
        // {
        //       path: "menu-management",
        //   name: "menu-management",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建菜单管理页面
        // },
        // 权限管理功能已注释
        // {
        //       path: "permission-management",
        //   name: "permission-management",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建权限管理页面
        // },
        {
          path: "project-query",
          name: "project-query",
          component: () => import("../views/ProjectQuery/index.vue"), // 项目查询页面
        },
        {
          path: "project-detail/:id",
          name: "project-detail",
          component: () => import("../views/ProjectDetail/index.vue"), // 项目详情页面
        },
        {
          path: "project-apply/:id?",
          name: "project-apply",
          component: () => import("../views/ProjectApply/index.vue"), // 项目申报页面（支持编辑）
        },
        {
          path: "progress-management",
          name: "progress-management",
          component: () => import("../views/ProgressManagement/index.vue"), // 进度管理页面
        },
        // 项目撤销功能已注释
        // {
        //       path: "project-revoke",
        //   name: "project-revoke",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建项目撤销页面
        // },
        {
          path: "project-overview",
          name: "project-overview",
          component: () => import("../views/ProjectOverview/index.vue"), // 项目一览页面
        },
        {
          path: "project-awards",
          name: "project-awards",
          component: () => import("../views/ProjectAwards/index.vue"), // 项目获奖记录页面
        },
        {
          path: "notice-list",
          name: "notice-list",
          component: () => import("../views/NoticeManagement/List.vue"), // 查看公告
        },
        {
          path: "notice-publish",
          name: "notice-publish",
          component: () => import("../views/NoticeManagement/Publish.vue"), // 发布公告
        },
        // 项目空间相关功能已注释
        // {
        //       path: "space-apply",
        //   name: "space-apply",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建项目空间申请页面
        // },
        // {
        //       path: "space-query",
        //   name: "space-query",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建项目空间查询页面
        // },
        // {
        //       path: "space-inspection",
        //   name: "space-inspection",
        //       component: () => import("../views/index/index.vue"), // TODO: 创建空间卫生检查页面
        //     },
      ],
    },
  ],
});

// 路由守卫：检查登录状态和角色权限
router.beforeEach((to, from, next) => {
  const loginStore = useLoginStore();

  // 初始化用户信息（从 localStorage 恢复）
  loginStore.initUserInfo();

  // 登录页面不需要验证
  if (to.path === "/login") {
    if (loginStore.isLoggedIn()) {
      next("/index");
    } else {
      next();
    }
    return;
  }

  // 其他页面需要登录
  if (!loginStore.isLoggedIn()) {
    next("/login");
    return;
  }

  // 检查角色权限
  const userRole = loginStore.userInfo?.role;
  const routeName = to.name as string;

  // 检查是否有路由访问权限
  if (!hasRoutePermission(userRole, routeName)) {
    ElMessage.error('您没有权限访问该页面');
    next("/index"); // 无权限时跳转到首页
    return;
  }

  next();
});

export default router;
