import { createApp } from "vue";
import { createPinia } from "pinia";
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";
import * as ElementPlusIconsVue from "@element-plus/icons-vue";
import zhCn from "element-plus/dist/locale/zh-cn.mjs";

import App from "./App.vue";
import router from "./router";

import "./assets/main.css";

const app = createApp(App);

// 注册 Pinia
app.use(createPinia());

// 注册 Vue Router
app.use(router);

// 注册 Element Plus
app.use(ElementPlus, {
  locale: zhCn, // 中文语言包
});

// 注册所有 Element Plus 图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}

app.mount("#app");
