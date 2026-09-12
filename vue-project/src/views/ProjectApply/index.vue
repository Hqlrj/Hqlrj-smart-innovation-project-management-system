<template>
  <div class="project-apply-container">
    <!-- 顶部操作按钮 -->
    <div class="action-buttons">
      <!-- 返回按钮（编辑模式显示） -->
      <el-button
        v-if="projectId"
        type="primary"
        plain
        :icon="ArrowLeft"
        @click="handleBack"
      >
        返回
      </el-button>
      <div class="right-buttons">
        <!-- 编辑模式显示保存按钮 -->
        <el-button v-if="projectId" type="primary" @click="handleSave">保存</el-button>
        <!-- 新建模式显示立即申报和重置按钮 -->
        <template v-if="!projectId">
          <el-button type="success" @click="handleSubmit">立即申报</el-button>
          <el-button @click="handleReset">重置</el-button>
        </template>
      </div>
    </div>

    <!-- 标签页导航 -->
    <el-tabs v-model="activeTab" class="project-tabs">
      <!-- 基本信息标签页 -->
      <el-tab-pane label="基本信息" name="basic">
        <el-form
          ref="basicFormRef"
          :model="formData"
          :rules="basicRules"
          label-width="150px"
          class="project-form"
        >
          <el-form-item label="项目类型" prop="projectType">
            <el-select v-model="formData.projectType" placeholder="请选择项目类型" style="width: 100%">
              <el-option label="全部" value="" />
              <el-option label="科技创新" value="科技创新" />
              <el-option label="社会实践" value="社会实践" />
              <el-option label="创业项目" value="创业项目" />
              <el-option label="学术研究" value="学术研究" />
            </el-select>
          </el-form-item>

          <el-form-item label="项目名称" prop="projectName">
            <el-input v-model="formData.projectName" placeholder="请输入项目名称" />
          </el-form-item>

          <el-form-item label="项目简介" prop="projectIntro">
            <el-input
              v-model="formData.projectIntro"
              type="textarea"
              :rows="4"
              placeholder="请输入项目简介"
              resize="vertical"
            />
          </el-form-item>

          <el-form-item label="项目创新点" prop="innovationPoints">
            <el-input
              v-model="formData.innovationPoints"
              type="textarea"
              :rows="4"
              placeholder="请输入项目创新点"
              resize="vertical"
            />
          </el-form-item>

          <el-form-item label="项目背景" prop="projectBackground">
            <el-input
              v-model="formData.projectBackground"
              type="textarea"
              :rows="4"
              placeholder="请输入项目背景"
              resize="vertical"
            />
          </el-form-item>

          <el-form-item label="项目意义" prop="projectSignificance">
            <el-input
              v-model="formData.projectSignificance"
              type="textarea"
              :rows="4"
              placeholder="请输入项目意义"
              resize="vertical"
            />
          </el-form-item>

          <el-form-item label="市场需求分析" prop="marketDemandAnalysis">
            <el-input
              v-model="formData.marketDemandAnalysis"
              type="textarea"
              :rows="4"
              placeholder="请输入市场需求分析"
              resize="vertical"
            />
          </el-form-item>

          <el-form-item label="项目关键技术介绍" prop="keyTechnology">
            <el-input
              v-model="formData.keyTechnology"
              type="textarea"
              :rows="4"
              placeholder="请输入项目关键技术介绍"
              resize="vertical"
            />
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 计划书标签页 -->
      <el-tab-pane label="计划书" name="plan">
        <div class="plan-section">
          <el-button type="primary" @click="handleUploadPlan">上传计划书(Word/PDF)</el-button>
          <!-- <el-button @click="handleDownloadTemplate">下载计划书(Word)模板</el-button> -->
          <div v-if="formData.plans && formData.plans.length > 0" class="plan-files-list">
            <div v-for="(plan, index) in formData.plans" :key="plan.id || index" class="file-display">
              <el-icon class="file-icon" :size="48">
                <Document v-if="isPdfFile(plan.fileName)" />
                <Document v-else />
              </el-icon>
              <div class="file-info">
                <div class="file-name">{{ plan.fileName }}</div>
                <div class="file-actions">
                  <el-button type="primary" link @click="handleDownloadPlan(plan)">下载</el-button>
                  <el-button type="danger" link @click="handleDeletePlan(plan, index)">删除</el-button>
                </div>
              </div>
            </div>
          </div>
          <div v-else class="plan-empty" style="margin-top: 20px; text-align: center; color: #909399">
            暂无计划书文件
          </div>
        </div>
      </el-tab-pane>

      <!-- 项目成员标签页 -->
      <el-tab-pane label="项目成员" name="members">
        <div class="members-section">
          <el-button type="success" @click="handleAddMember">添加项目成员</el-button>
          
          <el-table
            :data="formData.members || []"
            border
            style="width: 100%; margin-top: 20px"
            :empty-text="'暂无数据'"
          >
            <el-table-column prop="studentId" label="学号" width="120" align="center" header-align="center" />
            <el-table-column prop="name" label="姓名" width="120" align="center" header-align="center" />
            <el-table-column prop="gender" label="性别" width="80" align="center" header-align="center" />
            <el-table-column prop="phone" label="手机号" width="150" align="center" header-align="center" />
            <el-table-column prop="college" label="所属学院" width="150" align="center" header-align="center" />
            <el-table-column prop="className" label="班级" width="120" align="center" header-align="center" />
            <el-table-column prop="introduction" label="介绍" show-overflow-tooltip align="center" header-align="center" />
            <el-table-column label="操作" width="150" fixed="right" align="center" header-align="center">
              <template #default="{ row, $index }">
                <el-button type="primary" size="small" @click="handleEditMember($index)">修改</el-button>
                <el-button type="danger" size="small" @click="handleDeleteMember($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 指导老师标签页 -->
      <el-tab-pane label="指导老师" name="advisors">
        <div class="advisors-section">
          <el-button type="success" @click="handleAddAdvisor">添加指导老师</el-button>
          
          <el-table
            :data="formData.advisors || []"
            border
            style="width: 100%; margin-top: 20px"
            :empty-text="'暂无数据'"
          >
            <el-table-column prop="name" label="姓名" width="120" align="center" header-align="center" />
            <el-table-column prop="gender" label="性别" width="80" align="center" header-align="center" />
            <el-table-column prop="phone" label="手机号" width="150" align="center" header-align="center" />
            <el-table-column prop="college" label="所属学院" width="150" align="center" header-align="center" />
            <el-table-column prop="introduction" label="介绍" show-overflow-tooltip align="center" header-align="center" />
            <el-table-column label="操作" width="150" fixed="right" align="center" header-align="center">
              <template #default="{ row, $index }">
                <el-button type="primary" size="small" @click="handleEditAdvisor($index)">修改</el-button>
                <el-button type="danger" size="small" @click="handleDeleteAdvisor($index)">删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加/编辑成员弹窗 -->
    <el-dialog
      v-model="memberDialogVisible"
      :title="memberEditIndex === -1 ? '添加项目成员' : '修改项目成员'"
      width="600px"
      @close="handleMemberDialogClose"
    >
      <el-form
        ref="memberFormRef"
        :model="memberFormData"
        :rules="memberRules"
        label-width="100px"
      >
        <el-form-item label="学号" prop="studentId">
          <el-input v-model="memberFormData.studentId" placeholder="请输入学号" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="memberFormData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="memberFormData.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="memberFormData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属学院" prop="college">
          <el-input v-model="memberFormData.college" placeholder="请输入所属学院" />
        </el-form-item>
        <el-form-item label="班级" prop="className">
          <el-input v-model="memberFormData.className" placeholder="请输入班级" />
        </el-form-item>
        <el-form-item label="介绍" prop="introduction">
          <el-input
            v-model="memberFormData.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入介绍"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleMemberDialogClose">取消</el-button>
          <el-button type="primary" @click="handleMemberSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 添加/编辑指导老师弹窗 -->
    <el-dialog
      v-model="advisorDialogVisible"
      :title="advisorEditIndex === -1 ? '添加指导老师' : '修改指导老师'"
      width="600px"
      @close="handleAdvisorDialogClose"
    >
      <el-form
        ref="advisorFormRef"
        :model="advisorFormData"
        :rules="advisorRules"
        label-width="100px"
      >
        <el-form-item label="姓名" prop="name">
          <el-input v-model="advisorFormData.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="性别" prop="gender">
          <el-select v-model="advisorFormData.gender" placeholder="请选择性别" style="width: 100%">
            <el-option label="男" value="男" />
            <el-option label="女" value="女" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="advisorFormData.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="所属学院" prop="college">
          <el-input v-model="advisorFormData.college" placeholder="请输入所属学院" />
        </el-form-item>
        <el-form-item label="介绍" prop="introduction">
          <el-input
            v-model="advisorFormData.introduction"
            type="textarea"
            :rows="3"
            placeholder="请输入介绍"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleAdvisorDialogClose">取消</el-button>
          <el-button type="primary" @click="handleAdvisorSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 文件上传组件（隐藏，支持多选） -->
    <input
      ref="fileInputRef"
      type="file"
      accept=".doc,.docx,.pdf"
      multiple
      style="display: none"
      @change="handleFileChange"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Document, ArrowLeft } from '@element-plus/icons-vue'
import { useRoute, useRouter } from 'vue-router'
import {
  getProjectById,
  addProject,
  updateProject,
  submitProject,
  uploadPlan,
  deletePlan,
  downloadTemplate,
  type Project,
  type ProjectMember,
  type ProjectAdvisor,
  type ProjectPlan
} from '@/api/project'
import request from '@/utils/request'

const route = useRoute()
const router = useRouter()

// 表单数据
const formData = ref<Partial<Project>>({
  projectType: '',
  projectName: '',
  projectIntro: '',
  innovationPoints: '',
  projectBackground: '',
  projectSignificance: '',
  marketDemandAnalysis: '',
  keyTechnology: '',
  planFilePath: '',
  planFileName: '',
  members: [],
  advisors: [],
  plans: []
})

// 标签页
const activeTab = ref('basic')

// 表单引用
const basicFormRef = ref<FormInstance>()
const memberFormRef = ref<FormInstance>()
const advisorFormRef = ref<FormInstance>()

// 基本信息表单验证规则
const basicRules = ref<FormRules>({
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }]
})

// 成员相关
const memberDialogVisible = ref(false)
const memberEditIndex = ref(-1)
const memberFormData = ref<Partial<ProjectMember>>({
  studentId: '',
  name: '',
  gender: '',
  phone: '',
  college: '',
  className: '',
  introduction: ''
})

// 成员表单验证规则
const memberRules = ref<FormRules>({
  studentId: [{ required: true, message: '请输入学号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入合法的手机号', trigger: 'blur' }
  ]
})

// 指导老师相关
const advisorDialogVisible = ref(false)
const advisorEditIndex = ref(-1)
const advisorFormData = ref<Partial<ProjectAdvisor>>({
  name: '',
  gender: '',
  phone: '',
  college: '',
  introduction: ''
})

// 指导老师表单验证规则
const advisorRules = ref<FormRules>({
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入合法的手机号', trigger: 'blur' }
  ]
})

// 文件上传
const fileInputRef = ref<HTMLInputElement>()

// 检查是否有项目ID（编辑模式）
const projectId = ref<number | null>(null)

// 加载项目数据
const loadProject = async () => {
  const id = route.params.id as string
  if (id && id !== 'new') {
    try {
      projectId.value = parseInt(id)
      // request 拦截器已经处理了 Result 格式，直接返回 data 部分
      const response = await getProjectById(projectId.value) as unknown as Project
      if (response && response.id) {
        formData.value = {
          ...response,
          members: response.members || [],
          advisors: response.advisors || [],
          plans: response.plans || []
        }
      }
    } catch (error: any) {
      ElMessage.error(error.message || '加载项目数据失败')
    }
  }
}

// 保存项目
const handleSave = async () => {
  if (!basicFormRef.value) return
  
  try {
    // 验证基本信息
    await basicFormRef.value.validate()
    
    const projectData = { ...formData.value }
    
    if (projectId.value) {
      // 更新项目
      await updateProject(projectId.value, projectData)
      ElMessage.success('保存成功')
    } else {
      // 新增项目
      const response = await addProject(projectData)
      if (response && response.id) {
        projectId.value = response.id
        formData.value.id = response.id
        ElMessage.success('保存成功')
      } else {
        ElMessage.success('保存成功，但未获取到项目ID')
      }
    }
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '保存失败')
    }
  }
}

// 提交申报
const handleSubmit = async () => {
  if (!basicFormRef.value) return
  
  try {
    // 验证基本信息
    await basicFormRef.value.validate()
    
    // 先保存项目
    const projectData = { ...formData.value }
    
    // 如果 plans 数组不为空，将第一个计划书的信息同步到 planFilePath 和 planFileName
    // 这样可以确保详情页面能正确显示计划书
    if (projectData.plans && projectData.plans.length > 0) {
      const firstPlan = projectData.plans[0]
      if (firstPlan.filePath) {
        projectData.planFilePath = firstPlan.filePath
      }
      if (firstPlan.fileName) {
        projectData.planFileName = firstPlan.fileName
      }
    }
    
    if (projectId.value) {
      await updateProject(projectId.value, projectData)
    } else {
      const response = await addProject(projectData)
      if (response && response.id) {
        projectId.value = response.id
        formData.value.id = response.id
      } else {
        ElMessage.error('保存项目失败，无法提交申报')
        return
      }
    }
    
    // 提交申报
    if (projectId.value) {
      await submitProject(projectId.value)
      ElMessage.success('申报成功')
      router.push('/project-query')
    } else {
      ElMessage.error('项目ID不存在，无法提交')
    }
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '申报失败')
    }
  }
}

// 重置表单
const handleReset = () => {
  ElMessageBox.confirm('确定要重置表单吗？所有未保存的数据将丢失。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formData.value = {
      projectType: '',
      projectName: '',
      projectIntro: '',
      innovationPoints: '',
      projectBackground: '',
      projectSignificance: '',
      marketDemandAnalysis: '',
      keyTechnology: '',
      planFilePath: '',
      planFileName: '',
      members: [],
      advisors: [],
      plans: []
    }
    projectId.value = null
    basicFormRef.value?.clearValidate()
    ElMessage.success('已重置')
  }).catch(() => {
    // 用户取消
  })
}

// 上传计划书
const handleUploadPlan = () => {
  fileInputRef.value?.click()
}

// 文件选择变化
const handleFileChange = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const files = target.files
  if (!files || files.length === 0) return

  // 转换为数组并检查文件类型
  const fileArray = Array.from(files)
  const invalidFiles: string[] = []
  
  fileArray.forEach(file => {
    const fileName = file.name.toLowerCase()
    if (!fileName.endsWith('.doc') && !fileName.endsWith('.docx') && !fileName.endsWith('.pdf')) {
      invalidFiles.push(file.name)
    }
  })

  if (invalidFiles.length > 0) {
    ElMessage.error(`以下文件格式不支持：${invalidFiles.join(', ')}。只支持上传Word文档（.doc或.docx格式）或PDF文件（.pdf格式）`)
    target.value = ''
    return
  }

  // 调用上传API
  try {
    const response = await uploadPlan(fileArray, projectId.value || undefined)
    const plans = (response as any)?.data || response || []
    if (plans && plans.length > 0) {
      // 初始化 plans 数组
      if (!formData.value.plans) {
        formData.value.plans = []
      }
      // 添加新上传的计划书
      formData.value.plans.push(...plans)
      ElMessage.success(`成功上传 ${plans.length} 个文件`)
    } else {
      ElMessage.warning('没有文件上传成功')
    }
  } catch (error: any) {
    ElMessage.error(error.message || '上传失败')
  } finally {
    // 清空文件选择
    if (target) {
      target.value = ''
    }
  }
}

// 获取文件URL
const getPlanFileUrl = (path: string) => {
  if (!path) return ''
  // 如果是完整URL，直接返回
  if (path.startsWith('http')) return path
  // 如果路径以 uploads 开头，通过代理访问
  if (path.startsWith('uploads/')) {
    return `/api/${path}`
  }
  // 否则拼接API地址
  const baseURL = request.defaults.baseURL || '/api'
  return `${baseURL}/${path}`
}

// 判断是否为PDF文件
const isPdfFile = (fileName: string) => {
  return fileName?.toLowerCase().endsWith('.pdf')
}

// 下载计划书
const handleDownloadPlan = (plan: ProjectPlan) => {
  if (!plan.filePath) return
  const fileUrl = getPlanFileUrl(plan.filePath)
  // 创建临时a标签下载
  const link = document.createElement('a')
  link.href = fileUrl
  link.download = plan.fileName || '计划书'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 删除计划书
const handleDeletePlan = async (plan: ProjectPlan, index: number) => {
  if (!plan.id) {
    // 如果还没有保存到数据库（刚上传但项目未保存），直接从列表中删除
    if (formData.value.plans) {
      formData.value.plans.splice(index, 1)
      ElMessage.success('删除成功')
    }
    return
  }

  try {
    await ElMessageBox.confirm('确定要删除该计划书吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deletePlan(plan.id)
    if (formData.value.plans) {
      formData.value.plans.splice(index, 1)
    }
    ElMessage.success('删除成功')
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 下载模板
const handleDownloadTemplate = async () => {
  try {
    const response = await downloadTemplate()
    ElMessage.info('模板下载功能待实现，模板路径：' + response.data)
    // 实际项目中可以实现文件下载
  } catch (error: any) {
    ElMessage.error(error.message || '下载失败')
  }
}

// 添加成员
const handleAddMember = () => {
  memberEditIndex.value = -1
  memberFormData.value = {
    studentId: '',
    name: '',
    gender: '',
    phone: '',
    college: '',
    className: '',
    introduction: ''
  }
  memberDialogVisible.value = true
}

// 编辑成员
const handleEditMember = (index: number) => {
  if (!formData.value.members) return
  memberEditIndex.value = index
  memberFormData.value = { ...formData.value.members[index] }
  memberDialogVisible.value = true
}

// 删除成员
const handleDeleteMember = (index: number) => {
  if (!formData.value.members) return
  ElMessageBox.confirm('确定要删除该成员吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formData.value.members?.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消
  })
}

// 成员表单提交
const handleMemberSubmit = async () => {
  if (!memberFormRef.value) return
  
  try {
    await memberFormRef.value.validate()
    
    // 验证通过后，确保必需字段存在
    if (!memberFormData.value.studentId || !memberFormData.value.name) {
      ElMessage.error('学号和姓名不能为空')
      return
    }
    
    if (!formData.value.members) {
      formData.value.members = []
    }
    
    // 创建符合 ProjectMember 类型的成员对象
    const member: ProjectMember = {
      studentId: memberFormData.value.studentId,
      name: memberFormData.value.name,
      gender: memberFormData.value.gender,
      phone: memberFormData.value.phone,
      college: memberFormData.value.college,
      className: memberFormData.value.className,
      introduction: memberFormData.value.introduction
    }
    
    if (memberEditIndex.value === -1) {
      // 添加成员
      formData.value.members.push(member)
    } else {
      // 更新成员
      formData.value.members[memberEditIndex.value] = member
    }
    
    memberDialogVisible.value = false
    ElMessage.success(memberEditIndex.value === -1 ? '添加成功' : '修改成功')
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 成员弹窗关闭
const handleMemberDialogClose = () => {
  memberDialogVisible.value = false
  memberFormRef.value?.clearValidate()
}

// 添加指导老师
const handleAddAdvisor = () => {
  advisorEditIndex.value = -1
  advisorFormData.value = {
    name: '',
    gender: '',
    phone: '',
    college: '',
    introduction: ''
  }
  advisorDialogVisible.value = true
}

// 编辑指导老师
const handleEditAdvisor = (index: number) => {
  if (!formData.value.advisors) return
  advisorEditIndex.value = index
  advisorFormData.value = { ...formData.value.advisors[index] }
  advisorDialogVisible.value = true
}

// 删除指导老师
const handleDeleteAdvisor = (index: number) => {
  if (!formData.value.advisors) return
  ElMessageBox.confirm('确定要删除该指导老师吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formData.value.advisors?.splice(index, 1)
    ElMessage.success('删除成功')
  }).catch(() => {
    // 用户取消
  })
}

// 指导老师表单提交
const handleAdvisorSubmit = async () => {
  if (!advisorFormRef.value) return
  
  try {
    await advisorFormRef.value.validate()
    
    // 验证通过后，确保必需字段存在
    if (!advisorFormData.value.name) {
      ElMessage.error('姓名不能为空')
      return
    }
    
    if (!formData.value.advisors) {
      formData.value.advisors = []
    }
    
    // 创建符合 ProjectAdvisor 类型的指导老师对象
    const advisor: ProjectAdvisor = {
      name: advisorFormData.value.name as string,
      gender: advisorFormData.value.gender,
      phone: advisorFormData.value.phone,
      college: advisorFormData.value.college,
      introduction: advisorFormData.value.introduction
    }
    
    if (advisorEditIndex.value === -1) {
      // 新增
      formData.value.advisors.push(advisor)
    } else {
      // 编辑
      formData.value.advisors[advisorEditIndex.value] = advisor
    }
    
    advisorDialogVisible.value = false
    ElMessage.success(advisorEditIndex.value === -1 ? '添加成功' : '修改成功')
  } catch (error: any) {
    console.error('表单验证失败:', error)
  }
}

// 关闭指导老师弹窗
const handleAdvisorDialogClose = () => {
  advisorDialogVisible.value = false
  advisorFormData.value = {
    name: '',
    gender: '',
    phone: '',
    college: '',
    introduction: ''
  }
  advisorFormRef.value?.clearValidate()
}

// 返回上一页
const handleBack = () => {
  router.back()
}

// 组件挂载时加载数据
onMounted(() => {
  loadProject()
})
</script>

<style scoped>
.project-apply-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.action-buttons {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.right-buttons {
  display: flex;
  gap: 12px;
}

.project-tabs {
  margin-top: 20px;
}

.project-form {
  max-width: 800px;
  margin: 20px auto;
}

/* 确保表单标签文字在一行显示 */
.project-form :deep(.el-form-item__label) {
  white-space: nowrap;
  overflow: visible;
}

.plan-section {
  padding: 40px 20px;
  text-align: center;
}

.plan-section .el-button {
  margin: 0 10px;
}

.plan-files-list {
  margin-top: 30px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.plan-file-info {
  margin-top: 30px;
  display: flex;
  justify-content: center;
}

.file-display {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  background: #f5f7fa;
  width: 40%;
  max-width: 250px;
}

.file-icon {
  color: #409eff;
  flex-shrink: 0;
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
  word-break: break-all;
}

.file-actions {
  display: flex;
  gap: 12px;
}

.members-section {
  padding: 20px 0;
}

.advisors-section {
  padding: 20px 0;
  padding-top: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* 表格样式 */
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

:deep(.el-table .cell) {
  white-space: normal;
  word-break: break-all;
}
</style>
