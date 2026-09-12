<template>
  <div class="role-management-container">
    <!-- 顶部操作栏 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增角色</el-button>
    </div>

    <!-- 角色列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="roles"
        border
        style="width: 100%"
        :empty-text="'暂无数据'"
      >
        <el-table-column prop="roleId" label="角色编号" width="110" align="center" header-align="center" />
        <el-table-column prop="roleName" label="角色名" width="150" align="center" header-align="center" />
        <el-table-column prop="description" label="角色描述" width="516" show-overflow-tooltip align="center" header-align="center" />
        <el-table-column prop="createTime" label="角色创建时间" width="200" align="center" header-align="center" />
        <el-table-column prop="updateTime" label="角色更新时间" width="200" align="center" header-align="center">
          <template #default="{ row }">
            {{ row.updateTime || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="210" fixed="right" align="center" header-align="center">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleModify(row)">修改</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '修改角色' : '新增角色'"
      width="520px"
      :before-close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="rules"
        label-width="120px"
      >
        <el-form-item label="角色编号" prop="roleId">
          <el-input v-model="formData.roleId" placeholder="请输入角色编号" />
        </el-form-item>
        <el-form-item label="角色名" prop="roleName">
          <el-input v-model="formData.roleName" placeholder="请输入角色名" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input
            v-model="formData.description"
            type="textarea"
            :rows="3"
            placeholder="请输入角色描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { getRoleList, deleteRole, addRole, updateRole, type Role, type RoleQueryParams, type PageResult } from '@/api/role'

// 角色列表数据
const roles = ref<Role[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const currentRoleId = ref<string>('')
const formData = ref<Partial<Role>>({
  roleId: '',
  roleName: '',
  description: ''
})
const rules = ref<FormRules>({
  roleId: [{ required: true, message: '请输入角色编号', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名', trigger: 'blur' }],
  description: [{ required: true, message: '请输入角色描述', trigger: 'blur' }]
})

// 获取角色列表数据
const fetchRoleList = async () => {
  loading.value = true
  try {
    const params: RoleQueryParams = {}
    const response = await getRoleList(params) as unknown as PageResult<Role>
    roles.value = response?.list ?? []
  } catch (error: any) {
    ElMessage.error(error.message || '获取角色列表失败')
    roles.value = []
  } finally {
    loading.value = false
  }
}

// 打开新增
const handleAdd = () => {
  resetForm()
  isEdit.value = false
  dialogVisible.value = true
}

// 修改角色
const handleModify = (role: Role) => {
  resetForm()
  isEdit.value = true
  currentRoleId.value = role.id
  formData.value = {
    roleId: role.roleId,
    roleName: role.roleName,
    description: role.description
  }
  dialogVisible.value = true
}

// 删除角色
const handleDelete = async (role: Role) => {
  ElMessageBox.confirm(
    `确定要删除角色 "${role.roleName}" 吗？`,
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await deleteRole(role.id)
      ElMessage.success('删除成功')
      fetchRoleList() // 刷新列表
    } catch (error: any) {
      ElMessage.error(error.message || '删除失败')
    }
  }).catch(() => {
    // 用户取消
  })
}

const resetForm = () => {
  formData.value = {
    roleId: '',
    roleName: '',
    description: ''
  }
  formRef.value?.clearValidate()
}

const handleDialogClose = () => {
  dialogVisible.value = false
  resetForm()
}

// 提交新增/修改
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await updateRole(currentRoleId.value, formData.value)
      ElMessage.success('修改成功')
    } else {
      await addRole(formData.value)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchRoleList()
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 组件挂载时获取数据
onMounted(() => {
  fetchRoleList()
})
</script>

<style scoped>
.role-management-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.action-bar {
  margin-bottom: 20px;
  padding: 0 0 0 0;
}

.table-card {
  background: white;
}

.table-card :deep(.el-card__body) {
  padding: 0;
}

/* 确保表格颜色为白色 */
:deep(.el-table) {
  background: white;
}

:deep(.el-table th) {
  background: white;
  color: #333;
  font-weight: 600;
}

:deep(.el-table__body tr td) {
  background: white;
}

:deep(.el-table__body tr:hover > td) {
  background: white;
}

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

