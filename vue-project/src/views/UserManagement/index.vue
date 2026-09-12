<template>
  <div class="user-management-container">
    <!-- 搜索过滤栏 -->
    <div class="filter-bar">
      <el-form :model="filters" :inline="true" class="filter-form">
        <el-form-item label="姓名:">
          <el-input
            v-model="filters.name"
            placeholder="请输入名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <!-- <el-form-item label="手机号:">
          <el-input
            v-model="filters.phone"
            placeholder="请输入手机号"
            clearable
            style="width: 180px"
          />
        </el-form-item> -->
        <el-form-item label="角色:">
          <el-select
            v-model="filters.role"
            placeholder="请选择角色"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.roleName"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学院:">
          <el-select
            v-model="filters.college"
            placeholder="全部"
            clearable
            style="width: 180px"
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
        <el-form-item label="状态:">
          <el-select
            v-model="filters.status"
            placeholder="全部"
            clearable
            style="width: 180px"
          >
            <el-option label="已审批" value="已审批" />
            <el-option label="待审批" value="待审批" />
            <el-option label="已停用" value="已停用" />
          </el-select>
        </el-form-item>
        <el-form-item class="button-form-item">
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 新增用户按钮 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
    </div>

    <!-- 用户列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="users"
        border
        style="width: 100%"
        :empty-text="'暂无数据'"
      >
        <el-table-column prop="studentId" label="学号/职工号" width="147" align="center" header-align="center" />
        <el-table-column prop="name" label="姓名" width="100" align="center" header-align="center" />
        <el-table-column prop="phone" label="手机号" width="140" align="center" header-align="center" />
        <el-table-column prop="role" label="角色名称" width="130" align="center" header-align="center" />
        <el-table-column prop="college" label="学院" width="130" align="center" header-align="center" />
        <el-table-column prop="createTime" label="创建时间" width="195" align="center" header-align="center" />
        <el-table-column prop="loginTime" label="登录时间" width="195" align="center" header-align="center" />
        <el-table-column prop="status" label="当前状态" width="130" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag type="success" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center" header-align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleModify(row)">修改</el-button>
            <!-- <el-button type="info" size="small" @click="handleDelete(row)">停用</el-button> -->
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="totalCount"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <!-- 新增用户模态框 -->
    <el-dialog
      v-model="showAddModal"
      title="新增用户"
      width="600px"
      :before-close="handleAddClose"
    >
      <el-form
        ref="addFormRef"
        :model="addFormData"
        :rules="addRules"
        label-width="120px"
      >
        <el-form-item label="学号/职工号" prop="studentId">
          <el-input
            v-model="addFormData.studentId"
            placeholder="请输入学号/职工号"
            clearable
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="addFormData.name"
            placeholder="请输入姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="addFormData.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input
            v-model="addFormData.password"
            type="password"
            placeholder="请输入密码（至少6位）"
            show-password
            clearable
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select
            v-model="addFormData.role"
            placeholder="请选择角色"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.roleName"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学院" prop="college">
          <el-select
            v-model="addFormData.college"
            placeholder="请选择学院"
            style="width: 100%"
            clearable
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

        <el-form-item label="状态" prop="status">
          <el-select
            v-model="addFormData.status"
            placeholder="请选择状态"
            style="width: 100%"
            clearable
          >
            <el-option label="已审批" value="已审批" />
            <el-option label="待审批" value="待审批" />
            <el-option label="已停用" value="已停用" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleAddClose">取消</el-button>
          <el-button type="primary" @click="handleAddSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改用户模态框（统一为 Element Plus 弹窗样式） -->
    <el-dialog
      v-model="showModifyModal"
      title="修改用户"
      width="600px"
      :before-close="handleModifyClose"
    >
      <el-form
        ref="modifyFormRef"
        :model="modifyFormData"
        :rules="modifyRules"
        label-width="120px"
      >
        <el-form-item label="学号/职工号" prop="studentId">
          <el-input
            v-model="modifyFormData.studentId"
            placeholder="请输入学号/职工号"
            clearable
          />
        </el-form-item>

        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="modifyFormData.name"
            placeholder="请输入姓名"
            clearable
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input
            v-model="modifyFormData.phone"
            placeholder="请输入手机号"
            clearable
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select
            v-model="modifyFormData.role"
            placeholder="请选择角色"
            style="width: 100%"
            clearable
          >
            <el-option
              v-for="item in roleOptions"
              :key="item.id"
              :label="item.roleName"
              :value="item.roleName"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="学院" prop="college">
          <el-select
            v-model="modifyFormData.college"
            placeholder="请选择学院"
            style="width: 100%"
            clearable
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

        <el-form-item label="状态" prop="status">
          <el-select
            v-model="modifyFormData.status"
            placeholder="请选择状态"
            style="width: 100%"
            clearable
          >
            <el-option label="已审批" value="已审批" />
            <el-option label="待审批" value="待审批" />
            <el-option label="已停用" value="已停用" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleModifyClose">取消</el-button>
          <el-button type="primary" @click="handleModifySubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getUserList, addUser, updateUser, deleteUser, type User, type UserQueryParams, type PageResult } from '@/api/user'
import { getRoleList, type Role, type PageResult as RolePageResult } from '@/api/role'

// 用户列表数据
const users = ref<User[]>([])
const loading = ref(false)

// 筛选条件
const filters = reactive({
  name: '',
  phone: '',
  role: '',
  college: '',
  status: ''
})

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalCount = ref(0)

// 新增用户模态框
const showAddModal = ref(false)
const addFormRef = ref<FormInstance>()
const addFormData = reactive<Partial<User> & { password?: string }>({
  studentId: '',
  name: '',
  phone: '',
  password: '',
  role: '',
  college: '',
  status: '待审批'
})
const addRules = reactive<FormRules>({
  studentId: [{ required: true, message: '请输入学号/职工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

// 修改用户模态框
const showModifyModal = ref(false)
const selectedUser = ref<User | null>(null)
const roleOptions = ref<Role[]>([])

// 修改用户表单数据
interface ModifyUserForm {
  id?: string
  studentId: string
  name: string
  phone: string
  role: string
  college: string
  status: string
}

const modifyFormRef = ref<FormInstance>()
const modifyFormData = reactive<ModifyUserForm>({
  studentId: '',
  name: '',
  phone: '',
  role: '',
  college: '',
  status: ''
})

const modifyRules = reactive<FormRules>({
  studentId: [{ required: true, message: '请输入学号/职工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  college: [{ required: true, message: '请选择学院', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
})

const initialModifyData = ref<ModifyUserForm>({
  studentId: '',
  name: '',
  phone: '',
  role: '',
  college: '',
  status: ''
})

// 监听selectedUser变化，初始化修改表单数据
watch(
  () => selectedUser.value,
  (newUser) => {
    if (newUser) {
      Object.assign(modifyFormData, {
        id: newUser.id,
        studentId: newUser.studentId,
        name: newUser.name,
        phone: newUser.phone,
        role: newUser.role,
        college: newUser.college,
        status: newUser.status
      })
      initialModifyData.value = { ...modifyFormData }
      // 打开弹窗时清理上次校验状态
      modifyFormRef.value?.clearValidate()
    }
  }
)

// 获取用户列表数据
const fetchUserList = async () => {
  loading.value = true
  try {
    const params: UserQueryParams = {
      name: filters.name || undefined,
      phone: filters.phone || undefined,
      role: filters.role || undefined,
      college: filters.college || undefined,
      status: filters.status || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await getUserList(params) as unknown as PageResult<User>
    if (response) {
      users.value = response.list ?? []
      totalCount.value = response.total ?? 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取用户列表失败')
    users.value = []
    totalCount.value = 0
  } finally {
    loading.value = false
  }
}

// 获取角色下拉数据
const fetchRoleOptions = async () => {
  try {
    const res = await getRoleList({ page: 1, pageSize: 100 }) as unknown as RolePageResult<Role>
    roleOptions.value = res?.list ?? []
  } catch (error: any) {
    ElMessage.error(error.message || '获取角色列表失败')
    roleOptions.value = []
  }
}

// 搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchUserList()
}

// 重置
const handleReset = () => {
  filters.name = ''
  filters.phone = ''
  filters.role = ''
  filters.college = ''
  filters.status = ''
  currentPage.value = 1
  fetchUserList()
}

// 重置新增表单
const resetAddForm = () => {
  addFormData.studentId = ''
  addFormData.name = ''
  addFormData.phone = ''
  addFormData.password = ''
  addFormData.role = ''
  addFormData.college = ''
  addFormData.status = '待审批'
  addFormRef.value?.clearValidate()
}

// 打开新增
const handleAdd = () => {
  resetAddForm()
  showAddModal.value = true
}

// 关闭新增
const handleAddClose = () => {
  showAddModal.value = false
  resetAddForm()
}

// 提交新增
const handleAddSubmit = async () => {
  if (!addFormRef.value) return
  try {
    await addFormRef.value.validate()
    await addUser(addFormData)
    ElMessage.success('新增用户成功')
    handleAddClose()
    fetchUserList()
  } catch (error: any) {
    // 如果是校验失败，Element Plus 已提示；否则提示接口错误
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '新增用户失败')
    }
  }
}

// 修改用户
const handleModify = (user: User) => {
  selectedUser.value = { ...user }
  showModifyModal.value = true
}

// 删除用户
const handleDelete = (user: User) => {
  ElMessageBox.confirm(
    `确定要删除用户 ${user.name} 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteUser(String(user.id))
      ElMessage.success('删除成功')
      fetchUserList()
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

// // 停用用户
// const handleDisable = (user: User) => {
//   // 使用 Element Plus 的确认对话框
//   ElMessageBox.confirm(
//     `确定要停用用户 ${user.name} 吗？`,
//     '提示',
//     {
//       confirmButtonText: '确定',
//       cancelButtonText: '取消',
//       type: 'warning',
//     }
//   ).then(() => {
//     console.log('停用用户:', user)
//     // TODO: 实现停用逻辑，调用后端 API
//     ElMessage.success('停用成功')
//   }).catch(() => {
//     // 用户取消
//   })
// }

// 确认修改
const handleModifySubmit = async () => {
  if (!modifyFormRef.value) return
  try {
    await modifyFormRef.value.validate()
    if (!selectedUser.value?.id) {
      ElMessage.error('用户ID不存在')
      return
    }
    const payload = { ...modifyFormData }
    await updateUser(selectedUser.value.id, payload)
    ElMessage.success('修改用户成功')
    fetchUserList() // 刷新列表
    showModifyModal.value = false
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '修改用户失败')
    }
  }
}

// 重置修改表单
const handleModifyReset = () => {
  Object.assign(modifyFormData, initialModifyData.value)
}

// 关闭修改弹窗
const handleModifyClose = () => {
  showModifyModal.value = false
  handleModifyReset()
  modifyFormRef.value?.clearValidate()
}

// 分页大小变化
const handleSizeChange = (val: number) => {
  pageSize.value = val
  currentPage.value = 1
  fetchUserList()
}

// 当前页变化
const handleCurrentChange = (val: number) => {
  currentPage.value = val
  fetchUserList()
}

// 组件挂载时获取数据
onMounted(() => {
  fetchUserList()
  fetchRoleOptions()
})
</script>


<style scoped>
.user-management-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
  width: 100%;
  box-sizing: border-box;
}

.filter-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
}

.filter-form {
  margin: 0;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
}

.action-bar {
  margin-bottom: 20px;
  padding: 0 20px;
}

/* .filter-form :deep(.button-form-item) {
  margin-top: 15px;
} */

.table-card {
  margin-bottom: 20px;
  background: white;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

.pagination-container {
  display: flex;
  justify-content: flex-start;
  padding: 20px 0;
  background: white;
}

/* 确保表格样式 */
:deep(.el-table) {
  background: white;
}

:deep(.el-table th) {
  background: white;
  color: #333;
  font-weight: 600;
}

:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) {
  background: white;
}

:deep(.el-table__body tr:hover > td) {
  background: white;
}

:deep(.el-table__body tr td) {
  background: white;
}

/* 修改用户模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-container {
  background: white;
  border-radius: 8px;
  width: 600px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #eee;
}

.modal-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.modal-close {
  background: none;
  border: none;
  font-size: 28px;
  color: #999;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: color 0.3s;
}

.modal-close:hover {
  color: #333;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.form-row {
  margin-bottom: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
}

.form-label {
  font-size: 14px;
  color: #333;
  margin-bottom: 8px;
  font-weight: 500;
}

.required {
  color: #f44336;
  margin-right: 4px;
}

.form-input {
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  transition: border-color 0.3s;
}

.form-input:focus {
  outline: none;
  border-color: #1976d2;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 20px 24px;
  border-top: 1px solid #eee;
}

.btn-confirm,
.btn-reset,
.btn-cancel {
  padding: 10px 24px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
}

.btn-confirm {
  background: #1976d2;
  color: white;
}

.btn-confirm:hover {
  background: #1565c0;
}

.btn-reset {
  background: #f5f5f5;
  color: #333;
}

.btn-reset:hover {
  background: #e0e0e0;
}

.btn-cancel {
  background: #f5f5f5;
  color: #333;
}

.btn-cancel:hover {
  background: #e0e0e0;
}

/* 新增弹窗底部按钮布局 */
:deep(.dialog-footer) {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表格单元格换行，长文本自动折行 */
:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-all;
}
</style>
