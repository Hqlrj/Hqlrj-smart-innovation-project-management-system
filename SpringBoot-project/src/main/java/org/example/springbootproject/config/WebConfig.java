package org.example.springbootproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置跨域访问、拦截器等
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    /**
     * 配置跨域访问
     * 允许前端应用（运行在不同端口）访问后端API
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 允许所有路径
                .allowedOriginPatterns("*") // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 允许的HTTP方法
                .allowedHeaders("*") // 允许所有请求头
                .allowCredentials(true) // 允许携带凭证（如Cookie）
                .maxAge(3600); // 预检请求的缓存时间（秒）
    }

    /**
     * 配置拦截器
     * 注册JWT拦截器，排除不需要验证的接口
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/**") // 拦截所有请求
                .excludePathPatterns(
                        "/login",           // 登录接口
                        "/users/register",  // 注册接口
                        "/roles",           // 获取角色列表（注册时需要）
                        "/auth/send-code",  // 发送验证码（忘记密码）
                        "/auth/reset-password", // 重置密码（忘记密码）
                        "/error",           // 错误页面
                        "/uploads/**"       // 静态资源文件
                );
    }

    /**
     * 配置静态资源映射
     * 将上传的文件目录映射为可访问的URL路径
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射项目计划书文件
        registry.addResourceHandler("/uploads/project-plans/**")
                .addResourceLocations("file:uploads/project-plans/");

        // 映射获奖证明图片
        registry.addResourceHandler("/uploads/award-certificates/**")
                .addResourceLocations("file:uploads/award-certificates/");

        // 映射用户头像
        registry.addResourceHandler("/uploads/avatars/**")
                .addResourceLocations("file:uploads/avatars/");
    }
}

