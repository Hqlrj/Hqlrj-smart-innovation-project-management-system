<template>
  <div class="home-container">
    <!-- 背景装饰元素 -->
    <div class="background-decoration">
      <!-- 创新创业主题图标 -->
      <div class="decoration-icon icon-lightbulb">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M9 21h6M12 3a6 6 0 0 1 6 6c0 3.314-2.686 6-6 6a6 6 0 0 1-6-6 6 6 0 0 1 6-6z"/>
        </svg>
      </div>
      <div class="decoration-icon icon-rocket">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M4.5 16.5c-1.5 1.26-2 5-2 5s3.74-.5 5-2c.71-.84.7-2.13-.09-2.91a2.18 2.18 0 0 0-2.91-.09z"/>
          <path d="m12 15-3-3a22 22 0 0 1 2-3.95A12.88 12.88 0 0 1 22 2c0 2.72-.78 7.5-6 11a22.35 22.35 0 0 1-4 2z"/>
          <path d="M9 12H4s.55-3.03 2-4c1.62-1.08 5 0 5 0"/>
          <path d="M12 15v5s3.03-.55 4-2c1.08-1.62 0-5 0-5"/>
        </svg>
      </div>
      <div class="decoration-icon icon-chart">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <path d="M3 3v18h18M7 16l4-4 4 4 6-6"/>
        </svg>
        </div>
      <div class="decoration-icon icon-gear">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
          <circle cx="12" cy="12" r="3"/>
          <path d="M12 1v6m0 6v6M5.64 5.64l4.24 4.24m4.24 4.24l4.24 4.24M1 12h6m6 0h6M5.64 18.36l4.24-4.24m4.24-4.24l4.24-4.24"/>
        </svg>
      </div>
      <!-- 浮动粒子 -->
      <div class="particle" v-for="(particle, index) in particles" :key="index"
           :style="{ left: particle.x + '%', top: particle.y + '%', animationDelay: particle.delay + 's' }"></div>
      </div>
    <!-- 左右布局容器 -->
    <div class="content-wrapper">
      <!-- 左侧内容区 -->
      <div class="left-content">
        <div class="content-inner">
        <div class="welcome-icon">
            <svg width="80" height="80" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 2L2 7l10 5 10-5-10-5z" />
            <path d="M2 17l10 5 10-5" />
            <path d="M2 12l10 5 10-5" />
          </svg>
        </div>
          <h2 class="welcome-tag">智能创新</h2>
          <h1 class="main-title">创新创业项目管理系统</h1>
          <p class="subtitle">Innovation and Entrepreneurship Project Management System</p>
          <p class="description">
            致力于为创新创业项目提供全生命周期的智能化管理服务，
            助力项目团队高效协作，实现创新梦想。
          </p>
          <div class="feature-list">
            <div class="feature-item">
              <div class="feature-icon">🚀</div>
              <div class="feature-content">
                <h3>项目申报</h3>
                <p>便捷的项目申报流程，支持在线提交与审核</p>
              </div>
            </div>
            <div class="feature-item">
            <div class="feature-icon">📊</div>
              <div class="feature-content">
                <h3>进度管理</h3>
                <p>实时跟踪项目进度，可视化展示项目状态</p>
              </div>
          </div>
            <div class="feature-item">
            <div class="feature-icon">🏆</div>
              <div class="feature-content">
                <h3>成果记录</h3>
                <p>记录项目获奖情况，展示项目成果</p>
              </div>
          </div>
            <div class="feature-item">
            <div class="feature-icon">👥</div>
              <div class="feature-content">
                <h3>团队协作</h3>
                <p>高效的团队管理，促进成员间协作</p>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 右侧视觉区 -->
      <div class="right-content">
        <div class="visual-container">
          <!-- 中心圆形装饰 -->
          <div class="center-circle">
            <div class="circle-inner">
              <svg width="120" height="120" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5">
                <path d="M12 2L2 7l10 5 10-5-10-5z" />
                <path d="M2 17l10 5 10-5" />
                <path d="M2 12l10 5 10-5" />
              </svg>
            </div>
          </div>
          <!-- 环绕的卡片 -->
          <div class="floating-card card-1">
            <div class="card-icon">💡</div>
            <div class="card-text">创新</div>
          </div>
          <div class="floating-card card-2">
            <div class="card-icon">🎯</div>
            <div class="card-text">目标</div>
          </div>
          <div class="floating-card card-3">
            <div class="card-icon">⚡</div>
            <div class="card-text">效率</div>
          </div>
          <div class="floating-card card-4">
            <div class="card-icon">🌟</div>
            <div class="card-text">成果</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
<script setup lang="ts">
// 背景粒子数据
const particles = [
  { x: 15, y: 20, delay: 0 },
  { x: 85, y: 15, delay: 0.5 },
  { x: 20, y: 60, delay: 1 },
  { x: 80, y: 70, delay: 1.5 },
  { x: 50, y: 30, delay: 2 },
  { x: 30, y: 80, delay: 2.5 },
  { x: 70, y: 50, delay: 3 },
  { x: 10, y: 40, delay: 3.5 },
  { x: 90, y: 60, delay: 4 },
  { x: 40, y: 10, delay: 4.5 }
]
</script>
<style scoped>
.home-container {
  padding: 0;
  height: calc(100vh - 60px);
  position: relative;
  background: linear-gradient(135deg, #1e3c72 0%, #2a5298 25%, #1e88e5 50%, #00acc1 75%, #26a69a 100%);
  background-size: 400% 400%;
  animation: gradientShift 15s ease infinite;
  overflow: hidden;
}
@keyframes gradientShift {
  0% { background-position: 0% 50%; }
  50% { background-position: 100% 50%; }
  100% { background-position: 0% 50%; }
}
/* 背景装饰 */
.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  top: 0;
  left: 0;
  overflow: hidden;
  z-index: 1;
}
.decoration-icon {
  position: absolute;
  width: 80px;
  height: 80px;
  opacity: 0.15;
  color: rgba(255, 255, 255, 0.3);
  animation: floatIcon 8s ease-in-out infinite;
}

.decoration-icon svg {
  width: 100%;
  height: 100%;
}

.icon-lightbulb {
  top: 10%;
  left: 5%;
  animation-delay: 0s;
}

.icon-rocket {
  top: 20%;
  right: 8%;
  animation-delay: 2s;
}

.icon-chart {
  bottom: 15%;
  left: 8%;
  animation-delay: 4s;
}

.icon-gear {
  bottom: 25%;
  right: 5%;
  animation-delay: 6s;
}

@keyframes floatIcon {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-30px) rotate(10deg);
  }
}

.particle {
  position: absolute;
  width: 6px;
  height: 6px;
  background: rgba(255, 255, 255, 0.6);
  border-radius: 50%;
  animation: particleFloat 6s ease-in-out infinite;
  box-shadow: 0 0 10px rgba(255, 255, 255, 0.5);
}

@keyframes particleFloat {
  0%, 100% {
    transform: translateY(0) scale(1);
    opacity: 0.6;
  }
  50% {
    transform: translateY(-50px) scale(1.2);
    opacity: 1;
  }
}

/* 内容容器 */
.content-wrapper {
  position: relative;
  z-index: 10;
  width: 100%;
  height: 100%;
  display: flex;
  padding: 40px;
  box-sizing: border-box;
}

/* 左侧内容区 */
.left-content {
  flex: 1;
  display: flex;
  align-items: center;
  padding-right: 40px;
}

.content-inner {
  max-width: 600px;
  animation: fadeInLeft 1s ease-out;
}

@keyframes fadeInLeft {
  from {
    opacity: 0;
    transform: translateX(-50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.welcome-icon {
  margin-bottom: 24px;
  animation: iconFloat 3s ease-in-out infinite;
}

@keyframes iconFloat {
  0%, 100% {
    transform: translateY(0px);
  }
  50% {
    transform: translateY(-10px);
  }
}

.welcome-icon svg {
  width: 80px;
  height: 80px;
  color: rgba(255, 255, 255, 0.95);
  filter: drop-shadow(0 4px 12px rgba(0, 0, 0, 0.3));
}

.welcome-tag {
  font-size: 18px;
  font-weight: 600;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 16px 0;
  letter-spacing: 2px;
  text-transform: uppercase;
}

.main-title {
  font-size: 48px;
  font-weight: 700;
  color: #ffffff;
  margin: 0 0 16px 0;
  line-height: 1.2;
  text-shadow: 0 2px 20px rgba(0, 0, 0, 0.3);
}

.subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 24px 0;
  font-weight: 300;
  letter-spacing: 1px;
}

.description {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.9);
  margin: 0 0 40px 0;
  line-height: 1.8;
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  border: 1px solid rgba(255, 255, 255, 0.2);
  transition: all 0.3s ease;
}

.feature-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(10px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.feature-item .feature-icon {
  font-size: 32px;
  line-height: 1;
  flex-shrink: 0;
}

.feature-content h3 {
  font-size: 18px;
  font-weight: 600;
  color: #ffffff;
  margin: 0 0 6px 0;
}

.feature-content p {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0;
  line-height: 1.6;
}

/* 右侧视觉区 */
.right-content {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding-left: 40px;
}

.visual-container {
  position: relative;
  width: 100%;
  max-width: 500px;
  height: 500px;
  animation: fadeInRight 1s ease-out;
}

@keyframes fadeInRight {
  from {
    opacity: 0;
    transform: translateX(50px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.center-circle {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 200px;
  height: 200px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 2px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  justify-content: center;
  animation: rotateCircle 20s linear infinite;
  box-shadow: 0 0 40px rgba(255, 255, 255, 0.2);
}

@keyframes rotateCircle {
  from {
    transform: translate(-50%, -50%) rotate(0deg);
  }
  to {
    transform: translate(-50%, -50%) rotate(360deg);
  }
}

.circle-inner {
  width: 120px;
  height: 120px;
  color: rgba(255, 255, 255, 0.9);
}

.circle-inner svg {
  width: 100%;
  height: 100%;
  animation: rotateReverse 20s linear infinite;
}

@keyframes rotateReverse {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(-360deg);
  }
}

.floating-card {
  position: absolute;
  width: 120px;
  height: 120px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(15px);
  border-radius: 16px;
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
}

.floating-card:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: scale(1.1);
}

.card-icon {
  font-size: 40px;
  line-height: 1;
}
.card-text {
  font-size: 16px;
  font-weight: 600;
  color: #ffffff;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}
.card-1 {
  top: 10%;
  left: 10%;
  animation: floatCard1 4s ease-in-out infinite;
}

.card-2 {
  top: 10%;
  right: 10%;
  animation: floatCard2 4s ease-in-out infinite 1s;
}

.card-3 {
  bottom: 10%;
  left: 10%;
  animation: floatCard3 4s ease-in-out infinite 2s;
}

.card-4 {
  bottom: 10%;
  right: 10%;
  animation: floatCard4 4s ease-in-out infinite 3s;
}

@keyframes floatCard1 {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(20px, -20px) rotate(5deg);
  }
}

@keyframes floatCard2 {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(-20px, -20px) rotate(-5deg);
  }
}

@keyframes floatCard3 {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(20px, 20px) rotate(-5deg);
  }
}

@keyframes floatCard4 {
  0%, 100% {
    transform: translate(0, 0) rotate(0deg);
  }
  50% {
    transform: translate(-20px, 20px) rotate(5deg);
  }
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .content-wrapper {
    flex-direction: column;
    padding: 30px 20px;
  }

  .left-content {
    padding-right: 0;
    padding-bottom: 30px;
  }

  .right-content {
    padding-left: 0;
  }

  .visual-container {
    height: 400px;
  }

  .main-title {
    font-size: 36px;
  }
}

@media (max-width: 768px) {
  .content-wrapper {
    padding: 20px 15px;
  }
  
  .main-title {
    font-size: 28px;
  }
  
  .subtitle {
    font-size: 14px;
  }
  
  .description {
    font-size: 14px;
  }

  .feature-item {
    padding: 16px;
  }
  
  .feature-content h3 {
    font-size: 16px;
  }
  
  .feature-content p {
    font-size: 13px;
  }

  .visual-container {
    height: 300px;
  }
  
  .center-circle {
    width: 150px;
    height: 150px;
  }
  
  .circle-inner {
    width: 90px;
    height: 90px;
  }
  
  .floating-card {
    width: 90px;
    height: 90px;
  }
  
  .card-icon {
    font-size: 30px;
  }
  
  .card-text {
    font-size: 14px;
  }
}
</style>

