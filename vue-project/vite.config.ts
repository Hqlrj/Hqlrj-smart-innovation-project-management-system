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
    port: 5173, // 前端开发服务器端口
    proxy: {
      // 代理配置：将 /api 请求转发到后端服务器
      "/api": {
        target: "http://localhost:8080", // 后端服务器地址
        changeOrigin: true, // 改变请求头中的 origin
        rewrite: (path) => path.replace(/^\/api/, ""), // 重写路径，去掉 /api 前缀
        configure: (proxy, options) => {
          proxy.on('error', (err, req, res) => {
            console.log('代理错误:', err);
          });
          proxy.on('proxyReq', (proxyReq, req, res) => {
            console.log('代理请求:', req.method, req.url);
          });
          proxy.on('proxyRes', (proxyReq, req, res) => {
            console.log('代理响应:', proxyReq.statusCode, req.url);
          });
        }
      },
    },
  },
});
