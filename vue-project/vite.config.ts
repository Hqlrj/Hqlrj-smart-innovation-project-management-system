import { fileURLToPath, URL } from "node:url";

import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import vueJsx from "@vitejs/plugin-vue-jsx";

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), vueJsx()],
  resolve: {
    alias: {
      "@": fileURLToPath(new URL("./src", import.meta.url)),
    },
  },
  server: {
    proxy: {
      // 代理配置：将 /api 请求转发到后端服务器
      "/api": {
        target: "http://localhost:8080", // 后端服务器地址
        changeOrigin: true, // 改变请求头中的 origin
        rewrite: (path) => path.replace(/^\/api/, ""), // 重写路径，去掉 /api 前缀
      },
    },
  },
});
