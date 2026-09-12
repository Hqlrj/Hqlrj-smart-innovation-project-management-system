<template>
  <!-- 登录页面容器 -->
  <div class="login-container">
    <div class="login-card">
      <!-- 左侧信息展示面板 -->
      <div class="left-panel">
        <h1 class="system-title">智能创新创业项目管理系统</h1>
        <p class="system-slogan">高效申报，协同管理，成果可视</p>
        <div class="features">
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
                <polyline points="10 9 9 9 8 9" />
              </svg>
            </div>
            <p>在线申报项目</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" />
              </svg>
            </div>
            <p>团队协同沟通</p>
          </div>
          <div class="feature-item">
            <div class="feature-icon">
              <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <circle cx="12" cy="12" r="10" />
                <polyline points="12 6 12 12 16 14" />
              </svg>
            </div>
            <p>过程跟踪与成果沉淀</p>
          </div>
        </div>
      </div>

      <!-- 右侧登录表单区域 -->
      <div class="right-panel">
        <div class="login-form">
          <h2 class="welcome-title">欢迎回来</h2>
          <p class="welcome-desc">登录您的账号，开启创新创业项目协同</p>

          <!-- 登录表单 -->
          <form @submit.prevent="handleLogin">
            <div class="form-group">
              <label class="input-label">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle cx="12" cy="7" r="4" />
                </svg>
                账号
              </label>
              <input
                v-model="loginForm.username"
                type="text"
                class="form-input"
                placeholder="请输入账号"
                required
              />
            </div>

            <div class="form-group">
              <label class="input-label">
                <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <rect x="3" y="11" width="18" height="11" rx="2" ry="2" />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
                密码
              </label>
              <input
                v-model="loginForm.password"
                type="password"
                class="form-input"
                placeholder="请输入密码"
                required
              />
            </div>

            <button type="submit" class="login-button" :disabled="loading">
              <span v-if="!loading">登录</span>
              <span v-else>登录中...</span>
            </button>
          </form>

          <div class="login-links">
            <div class="register-link">
              还没有账号?<a href="#" @click.prevent="switchToRegister">立即注册</a>
            </div>
            <div class="forgot-link">
              <a href="#" @click.prevent="showForgotDialog = true">忘记密码?</a>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 注册弹窗 -->
    <el-dialog
      v-model="showRegisterDialog"
      title="注册账号"
      width="520px"
      append-to-body
      class="register-dialog"
      :align-center="true"
      :close-on-click-modal="false"
      @open="resetRegisterForm"
      @close="resetRegisterForm"
    >
      <form class="dialog-form two-col" autocomplete="off" @submit.prevent="handleRegisterSubmit">
        <div class="form-group">
          <label class="input-label">账号（学号/职工号）</label>
          <input
            v-model="registerForm.studentId"
            type="text"
            class="form-input"
            placeholder="请输入账号"
            required
            autocomplete="off"
          />
        </div>

        <div class="form-group">
          <label class="input-label">姓名</label>
          <input
            v-model="registerForm.name"
            type="text"
            class="form-input"
            placeholder="请输入姓名"
            required
          />
        </div>

        <div class="form-group">
          <label class="input-label">手机号</label>
          <input
            v-model="registerForm.phone"
            type="tel"
            class="form-input"
            placeholder="请输入手机号"
            required
            autocomplete="off"
          />
        </div>

        <div class="form-group">
          <label class="input-label">密码</label>
          <input
            v-model="registerForm.password"
            type="password"
            class="form-input"
            placeholder="请输入密码"
            required
            autocomplete="new-password"
          />
        </div>

        <div class="form-group">
          <label class="input-label">角色</label>
          <select v-model="registerForm.roleId" class="form-input select-input" required>
            <option value="" disabled>请选择角色</option>
            <option v-for="role in roles" :key="role.roleId" :value="role.roleId">
              {{ role.roleName }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label class="input-label">学院</label>
          <input
            v-model="registerForm.college"
            type="text"
            class="form-input"
            placeholder="请输入学院"
            required
          />
        </div>

        <div class="dialog-actions">
          <button type="button" class="dialog-button ghost" @click="closeRegisterDialog">取消</button>
          <button type="submit" class="dialog-button" :disabled="registerLoading">
            <span v-if="!registerLoading">提交注册</span>
            <span v-else>提交中...</span>
          </button>
        </div>
      </form>
    </el-dialog>

    <div class="copyright">智能创新创业项目系统©2025</div>
  </div>

  <!-- 忘记密码弹窗 -->
  <el-dialog
    v-model="showForgotDialog"
    title="重置密码"
    width="420px"
    append-to-body
    class="register-dialog"
    :align-center="true"
    :close-on-click-modal="false"
    @open="resetForgotForm"
    @close="resetForgotForm"
  >
    <form class="dialog-form single-col" autocomplete="off" @submit.prevent="handleResetPassword">
      <div class="form-group">
        <label class="input-label">手机号</label>
        <div class="phone-input-row">
          <input
            v-model="forgotForm.phone"
            type="tel"
            class="form-input"
            placeholder="请输入注册手机号"
            required
            autocomplete="off"
            maxlength="11"
          />
          <button
            type="button"
            class="send-code-btn"
            :disabled="codeCountdown > 0 || !isValidPhone(forgotForm.phone)"
            @click="handleSendCode"
          >
            {{ codeCountdown > 0 ? codeCountdown + 's' : '发送验证码' }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label class="input-label">验证码</label>
        <input
          v-model="forgotForm.code"
          type="text"
          class="form-input"
          placeholder="请输入6位验证码"
          required
          autocomplete="off"
          maxlength="6"
        />
      </div>

      <div class="form-group">
        <label class="input-label">新密码</label>
        <input
          v-model="forgotForm.newPassword"
          type="password"
          class="form-input"
          placeholder="请输入新密码"
          required
          autocomplete="new-password"
        />
      </div>

      <div class="form-group">
        <label class="input-label">确认密码</label>
        <input
          v-model="forgotForm.confirmPassword"
          type="password"
          class="form-input"
          placeholder="请再次输入新密码"
          required
          autocomplete="new-password"
        />
      </div>

      <div class="dialog-actions">
        <button type="button" class="dialog-button ghost" @click="showForgotDialog = false">取消</button>
        <button type="submit" class="dialog-button" :disabled="forgotLoading">
          <span v-if="!forgotLoading">确认重置</span>
          <span v-else>重置中...</span>
        </button>
      </div>
    </form>
  </el-dialog>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useLoginStore } from '@/stores/login'
import { register as registerApi, type RegisterRequest, sendResetCode, resetPassword } from '@/api/login'
import { getRoleList, type Role } from '@/api/role'

const router = useRouter()
const loginStore = useLoginStore()

// 登录表单数据
const loginForm = ref({
  username: '',
  password: ''
})

// 状态管理
const loading = ref(false) // 登录加载状态
const registerLoading = ref(false) // 注册加载状态
const roles = ref<Role[]>([]) // 角色列表（用于注册）
const showRegisterDialog = ref(false) // 注册弹窗显示状态

// 注册表单初始值
const initialRegisterForm: RegisterRequest = {
  studentId: '',
  name: '',
  phone: '',
  password: '',
  roleId: '',
  college: ''
}

const registerForm = ref<RegisterRequest>({ ...initialRegisterForm })

// 校验手机号是否合法（中国大陆 11 位手机号）
const isValidPhone = (phone: string) => {
  const reg = /^1[3-9]\d{9}$/
  return reg.test(phone)
}

// 加载角色列表（用于注册时选择）
const loadRoles = async () => {
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 }) as unknown as { list: Role[] }
    roles.value = res.list || []
  } catch (error) {
    console.error('加载角色失败', error)
    alert('加载角色失败，请稍后重试')
  }
}

// 组件挂载时加载角色列表
onMounted(() => {
  loadRoles()
})

// 处理登录
const handleLogin = async () => {
  loading.value = true
  try {
    const result = await loginStore.login({
      username: loginForm.value.username,
      password: loginForm.value.password
    })
    if (result.success) {
      // 登录成功后显示提示消息（会自动消失）
      ElMessage.success('登录成功！欢迎回来')
      // 延迟跳转，让用户看到提示消息
      setTimeout(() => {
        router.push('/index')
      }, 500)
    } else {
      ElMessage.error(result.error || '登录失败')
    }
  } catch (error) {
    console.error('登录失败:', error)
    ElMessage.error('登录失败，请检查账号和密码')
  } finally {
    loading.value = false
  }
}

// 处理注册提交
const handleRegisterSubmit = async () => {
  if (!isValidPhone(registerForm.value.phone || '')) {
    alert('请输入合法的11位手机号')
    return
  }
  if (!registerForm.value.roleId) {
    alert('请选择角色')
    return
  }
  // 设置注册按钮为加载状态(防止重复提交)
  registerLoading.value = true
  try {
    await registerApi(registerForm.value)
    alert('注册成功，请登录')
    resetRegisterForm()
    showRegisterDialog.value = false
    // 将账号填充到登录表单
    loginForm.value.username = registerForm.value.studentId
  } catch (error: any) {
    console.error('注册失败:', error)
    alert(error?.response?.data?.msg || '注册失败，请稍后再试')
  } finally {
    registerLoading.value = false
  }
}

// 重置注册表单
const resetRegisterForm = () => {
  registerForm.value = { ...initialRegisterForm }
}

// 打开注册弹窗
const switchToRegister = () => {
  resetRegisterForm()
  showRegisterDialog.value = true
}

// 关闭注册弹窗
const closeRegisterDialog = () => {
  showRegisterDialog.value = false
  resetRegisterForm()
}

// ==================== 忘记密码 ====================
const showForgotDialog = ref(false)
const forgotLoading = ref(false)
const codeCountdown = ref(0)
let countdownTimer: ReturnType<typeof setInterval> | null = null

const forgotForm = ref({
  phone: '',
  code: '',
  newPassword: '',
  confirmPassword: ''
})

const resetForgotForm = () => {
  forgotForm.value = { phone: '', code: '', newPassword: '', confirmPassword: '' }
  codeCountdown.value = 0
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const handleSendCode = async () => {
  if (!isValidPhone(forgotForm.value.phone)) {
    ElMessage.warning('请输入合法的11位手机号')
    return
  }
  try {
    await sendResetCode(forgotForm.value.phone)
    ElMessage.success('验证码已发送，请查看控制台输出')
    // 启动倒计时
    codeCountdown.value = 60
    countdownTimer = setInterval(() => {
      codeCountdown.value--
      if (codeCountdown.value <= 0) {
        if (countdownTimer) clearInterval(countdownTimer)
        countdownTimer = null
      }
    }, 1000)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '发送验证码失败')
  }
}

const handleResetPassword = async () => {
  if (!isValidPhone(forgotForm.value.phone)) {
    ElMessage.warning('请输入合法的11位手机号')
    return
  }
  if (!forgotForm.value.code || forgotForm.value.code.length !== 6) {
    ElMessage.warning('请输入6位验证码')
    return
  }
  if (forgotForm.value.newPassword.length < 6) {
    ElMessage.warning('新密码至少6位')
    return
  }
  if (forgotForm.value.newPassword !== forgotForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }
  forgotLoading.value = true
  try {
    await resetPassword(forgotForm.value.phone, forgotForm.value.code, forgotForm.value.newPassword)
    ElMessage.success('密码重置成功，请使用新密码登录')
    showForgotDialog.value = false
    // 用手机号填充登录表单账号（通过手机号反查学号需要额外请求，这里只提示用户）
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.msg || '重置失败，请检查验证码是否正确')
  } finally {
    forgotLoading.value = false
  }
}
</script>

<style scoped>
/* 登录容器：全屏居中布局，渐变背景 */
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 50%, #90caf9 100%);
  position: relative;
  overflow: hidden;
  padding: 20px;
}

/* 背景装饰动画圆圈 */
.login-container::before,
.login-container::after {
  content: '';
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.15);
  pointer-events: none;
  filter: blur(40px);
}

.login-container::before {
  width: 500px;
  height: 500px;
  top: -200px;
  right: -200px;
  animation: float 20s ease-in-out infinite;
}

.login-container::after {
  width: 400px;
  height: 400px;
  bottom: -150px;
  left: -150px;
  animation: float 25s ease-in-out infinite reverse;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0) translateX(0);
  }
  50% {
    transform: translateY(-30px) translateX(20px);
  }
}

/* 登录卡片：白色卡片，左右分栏布局 */
.login-card {
  width: 100%;
  max-width: 900px;
  min-height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.12);
  display: flex;
  overflow: hidden;
  position: relative;
  z-index: 1;
}

/* 左侧信息面板：蓝色渐变背景 */
.left-panel {
  flex: 1;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  padding: 40px 36px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: white;
  position: relative;
}

.system-title {
  font-size: 30px;
  font-weight: bold;
  margin: 0 0 16px 0;
  letter-spacing: 1.5px;
}

.system-slogan {
  font-size: 14px;
  opacity: 0.9;
  margin: 0;
  font-weight: 300;
}

.features {
  display: flex;
  flex-direction: column;
  gap: 22px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 15px;
}

.feature-icon {
  width: 50px;
  height: 50px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.feature-item p {
  margin: 0;
  font-size: 14px;
  font-weight: 400;
}

/* 右侧登录表单面板：白色背景 */
.right-panel {
  flex: 1;
  background: white;
  padding: 40px 36px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-form {
  width: 100%;
  max-width: 360px;
}

.welcome-title {
  font-size: 26px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
}

.welcome-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 28px 0;
}

.form-group {
  margin-bottom: 20px;
}

.input-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.input-label svg {
  color: #1976d2;
}

.form-input {
  width: 100%;
  padding: 12px 14px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  font-size: 13px;
  transition: all 0.3s ease;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #1976d2;
  box-shadow: 0 0 0 3px rgba(25, 118, 210, 0.1);
}

.form-input::placeholder {
  color: #999;
  font-size: 13px;
}

.select-input {
  appearance: none;
  background-color: #fff;
}

.login-button {
  width: 100%;
  padding: 14px;
  background: linear-gradient(135deg, #1976d2 0%, #42a5f5 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 8px;
}

.login-button:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 20px rgba(25, 118, 210, 0.3);
}

.login-button:active:not(:disabled) {
  transform: translateY(0);
}

.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.register-link {
  text-align: center;
  margin-top: 30px;
  font-size: 14px;
  color: #666;
}

.register-link a {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
  margin-left: 5px;
  transition: color 0.3s ease;
}

.register-link a:hover {
  color: #1565c0;
  text-decoration: underline;
}

/* 注册表单：两列网格布局 */
.dialog-form {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px 16px;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 4px;
  grid-column: 1 / -1;
}

.dialog-button {
  padding: 10px 16px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  background: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%);
  color: white;
  font-weight: 600;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.dialog-button:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.dialog-button:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 8px 24px rgba(25, 118, 210, 0.25);
}

.dialog-button.ghost {
  background: #f1f4f9;
  color: #4f5d75;
  border: 1px solid #d9e1ec;
}

.dialog-button.ghost:hover {
  box-shadow: none;
}

.two-col .form-group {
  margin-bottom: 0;
}

.register-dialog {
  top: 50% !important;
  transform: translateY(-50%);
  margin: 0 auto;
}

@media (max-width: 640px) {
  .dialog-form {
    grid-template-columns: 1fr;
  }

  .dialog-actions {
    justify-content: stretch;
  }
}

.copyright {
  position: absolute;
  bottom: 20px;
  left: 50%;
  transform: translateX(-50%);
  font-size: 12px;
  color: rgba(0, 0, 0, 0.5);
  z-index: 1;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .login-card {
    flex-direction: column;
    height: auto;
    max-width: 500px;
  }

  .left-panel {
    padding: 40px 30px;
    min-height: 300px;
  }

  .features {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 20px;
  }

  .feature-item {
    flex: 1;
    min-width: 120px;
  }

  .right-panel {
    padding: 40px 30px;
  }

  .system-title {
    font-size: 28px;
  }
}

/* 忘记密码链接区域 */
.login-links {
  text-align: center;
  margin-top: 30px;
  font-size: 14px;
  color: #666;
}

.login-links a {
  color: #1976d2;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.3s ease;
}

.login-links a:hover {
  color: #1565c0;
  text-decoration: underline;
}

.link-divider {
  margin: 0 8px;
  color: #ccc;
}

.forgot-link {
  margin-top: 8px;
}

/* 忘记密码弹窗 - 单列表单 */
.single-col {
  grid-template-columns: 1fr;
}

/* 手机号+发送验证码行 */
.phone-input-row {
  display: flex;
  gap: 10px;
  align-items: stretch;
}

.phone-input-row .form-input {
  flex: 1;
}

.send-code-btn {
  flex-shrink: 0;
  padding: 0 16px;
  background: linear-gradient(135deg, #42a5f5 0%, #1976d2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.3s ease;
}

.send-code-btn:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(25, 118, 210, 0.3);
}

.send-code-btn:disabled {
  background: #c0c4cc;
  cursor: not-allowed;
  transform: none;
  box-shadow: none;
}
</style>

