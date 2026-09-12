<template>
  <div class="project-awards-container">
    <!-- 搜索/筛选区域 -->
    <div class="search-bar">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="项目名称:">
          <el-input
            v-model="searchForm.projectName"
            placeholder="请输入项目名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="比赛名称:">
          <el-input
            v-model="searchForm.competitionName"
            placeholder="请输入比赛名称"
            clearable
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="比赛级别:">
          <el-select
            v-model="searchForm.competitionLevel"
            placeholder="请选择比赛级别"
            clearable
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option label="校级" value="校级" />
            <el-option label="省级" value="省级" />
            <el-option label="国家级" value="国家级" />
            <el-option label="国际级" value="国际级" />
          </el-select>
        </el-form-item>
        <!-- <el-form-item label="项目获奖等级:">
          <el-select
            v-model="searchForm.awardLevel"
            placeholder="请选择项目获奖等级"
            clearable
            style="width: 180px"
          >
            <el-option label="全部" value="" />
            <el-option label="一等奖" value="一等奖" />
            <el-option label="二等奖" value="二等奖" />
            <el-option label="三等奖" value="三等奖" />
            <el-option label="优秀奖" value="优秀奖" />
          </el-select>
        </el-form-item> -->
        <el-form-item>
          <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
          <el-button :icon="Refresh" @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 操作按钮区域：只有审核人和评委老师不显示新增按钮，其他角色都显示 -->
    <div v-if="!isJudgeOrAuditor" class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">新增</el-button>
    </div>

    <!-- 获奖记录列表表格 -->
    <el-card class="table-card" shadow="never">
      <el-table
        v-loading="loading"
        :data="awardList"
        border
        style="width: 100%"
        :empty-text="'暂无数据'"
      >
        <!-- 用 min-width 让表格在无滚动时自动撑满，避免右侧留白 -->
        <el-table-column prop="projectName" label="项目名称" min-width="200" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="competitionName" label="比赛名称" min-width="260" align="center" header-align="center" show-overflow-tooltip />
        <el-table-column prop="competitionLevel" label="比赛级别" width="120" align="center" header-align="center" />
        <el-table-column prop="awardLevel" label="获奖等级" width="120" align="center" header-align="center" />
        <el-table-column prop="status" label="当前状态" width="120" align="center" header-align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="awardCertificate" label="获奖证明" min-width="200" align="center" header-align="center">
          <template #default="{ row }">
            <el-image
              v-if="row.awardCertificate"
              :src="getImageUrl(row.awardCertificate)"
              :preview-src-list="[getImageUrl(row.awardCertificate)]"
              style="width: 100px; height: 100px; cursor: pointer"
              fit="cover"
              :preview-teleported="true"
            />
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="300" fixed="right" align="center" header-align="center">
          <template #default="{ row }">
            <!-- 项目负责人：只有自己的项目显示修改、删除按钮，别人的只有详情 -->
            <template v-if="isProjectLeader">
              <template v-if="isMyProject(row)">
                <!-- 自己的项目：未审核显示修改、详情、删除，已审核只显示详情、删除 -->
                <template v-if="row.status === 'pending'">
                  <el-button type="primary" size="small" @click="handleEdit(row)">修改</el-button>
                  <el-button size="small" @click="handleDetail(row)">详情</el-button>
                  <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
                <template v-else>
                  <!-- 已审核：只显示详情、删除 -->
                  <el-button size="small" @click="handleDetail(row)">详情</el-button>
                  <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
                </template>
              </template>
              <template v-else>
                <!-- 别人的项目：只显示详情 -->
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
              </template>
            </template>
            <!-- 评委老师或审核人：已审核只显示详情，未审核显示审批和详情 -->
            <template v-else-if="isJudgeOrAuditor">
              <template v-if="row.status === 'pending'">
                <!-- 未审核：显示审批、详情 -->
                <el-button type="success" size="small" @click="handleApprove(row)">审批</el-button>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
              </template>
              <template v-else>
                <!-- 已审核：只显示详情 -->
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
              </template>
            </template>
            <!-- 其他角色：保持原逻辑 -->
            <template v-else>
              <!-- 未审批状态：显示修改、审批、详情、删除 -->
              <template v-if="row.status === 'pending'">
                <el-button type="primary" size="small" @click="handleEdit(row)">修改</el-button>
                <el-button type="success" size="small" @click="handleApprove(row)">审批</el-button>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
              <!-- 已审批状态：显示详情、删除 -->
              <template v-else>
                <el-button size="small" @click="handleDetail(row)">详情</el-button>
                <el-button type="danger" size="small" @click="handleDelete(row)">删除</el-button>
              </template>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 分页组件 -->
    <div class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      @close="handleDialogClose"
    >
      <el-form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
      >
        <el-form-item label="项目名称" prop="projectId">
          <el-select
            v-model="formData.projectId"
            placeholder="请选择项目名称"
            style="width: 100%"
            :disabled="isEdit"
            @change="handleProjectChange"
          >
            <template #empty>
              <div style="padding: 10px 0; color: #909399; text-align: center;">
                暂无已审核通过的项目
              </div>
            </template>
            <el-option
              v-for="project in projectOptions"
              :key="project.id"
              :label="project.projectName"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="比赛名称" prop="competitionName">
          <el-input v-model="formData.competitionName" placeholder="请输入比赛名称" />
        </el-form-item>
        <el-form-item label="比赛级别" prop="competitionLevel">
          <el-select v-model="formData.competitionLevel" placeholder="请选择比赛级别" style="width: 100%">
            <el-option label="校级" value="校级" />
            <el-option label="省级" value="省级" />
            <el-option label="国家级" value="国家级" />
            <el-option label="国际级" value="国际级" />
          </el-select>
        </el-form-item>
        <el-form-item label="获奖等级" prop="awardLevel">
          <el-select v-model="formData.awardLevel" placeholder="请选择获奖等级" style="width: 100%">
            <el-option label="一等奖" value="一等奖" />
            <el-option label="二等奖" value="二等奖" />
            <el-option label="三等奖" value="三等奖" />
            <el-option label="优秀奖" value="优秀奖" />
          </el-select>
        </el-form-item>
        <el-form-item label="获奖证明" prop="awardCertificate">
          <el-upload
            ref="uploadRef"
            :file-list="fileList"
            :before-upload="beforeUpload"
            :http-request="handleCustomUpload"
            :on-remove="handleRemove"
            :limit="1"
            list-type="picture-card"
            accept="image/*"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="handleDialogClose">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="获奖记录详情"
      width="600px"
    >
      <el-descriptions :column="1" border v-if="currentAward">
        <el-descriptions-item label="项目名称">{{ currentAward.projectName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="比赛名称">{{ currentAward.competitionName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="比赛级别">{{ currentAward.competitionLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="获奖等级">{{ currentAward.awardLevel || '-' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentAward.status)" size="small">
            {{ getStatusText(currentAward.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item v-if="currentAward.status === 'rejected' && currentAward.rejectReason" label="未通过原因">
          <div style="color: #f56c6c; white-space: pre-wrap;">{{ currentAward.rejectReason }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="获奖证明">
          <el-image
            v-if="currentAward.awardCertificate"
            :src="getImageUrl(currentAward.awardCertificate)"
            :preview-src-list="[getImageUrl(currentAward.awardCertificate)]"
            style="width: 200px; height: 200px; cursor: pointer"
            fit="cover"
            :preview-teleported="true"
          />
          <span v-else>-</span>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ formatDate(currentAward.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ formatDate(currentAward.updateTime) }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>

    <!-- 审批确认弹窗 -->
    <el-dialog
      v-model="approveDialogVisible"
      title="审批确认"
      width="500px"
    >
      <el-form :model="approveForm" label-width="100px">
        <el-form-item label="项目名称">
          <span>{{ approveForm.projectName }}</span>
        </el-form-item>
        <el-form-item label="比赛名称">
          <span>{{ approveForm.competitionName }}</span>
        </el-form-item>
        <el-form-item label="审批操作">
          <el-radio-group v-model="approveForm.action">
            <el-radio label="approve">通过</el-radio>
            <el-radio label="reject">未通过</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="approveForm.action === 'reject'" label="未通过原因" prop="rejectReason">
          <el-input
            v-model="approveForm.rejectReason"
            type="textarea"
            :rows="4"
            placeholder="请输入未通过的原因说明"
            resize="vertical"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="approveDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleApproveConfirm">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { useLoginStore } from '@/stores/login'
import {
  getAwardList,
  getAwardById,
  addAward,
  updateAward,
  deleteAward,
  approveAward,
  rejectAward,
  uploadAwardCertificate,
  getMyProjects,
  type Award,
  type Project,
  type PageResult
} from '@/api/project'
import request from '@/utils/request'

const loginStore = useLoginStore()

// 判断是否是项目负责人
const isProjectLeader = computed(() => {
  return loginStore.userInfo?.role === '项目负责人'
})

// 判断是否是评委老师或审核人
const isJudgeOrAuditor = computed(() => {
  const role = loginStore.userInfo?.role
  return role === '评委老师' || role === '审批人'
})

const loading = ref(false)
const awardList = ref<Award[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 保存自己的项目ID列表（用于判断是否是自己的项目）
const myProjectIds = ref<Set<number>>(new Set())

// 搜索表单
const searchForm = reactive({
  projectName: '',
  competitionName: '',
  competitionLevel: '',
  awardLevel: ''
})

// 弹窗相关
const dialogVisible = ref(false)
const dialogTitle = ref('新增获奖记录')
const isEdit = ref(false)
const formRef = ref<FormInstance>()
const formData = reactive<Partial<Award>>({
  id: undefined,
  projectId: undefined,
  projectName: '',
  competitionName: '',
  competitionLevel: '',
  awardLevel: '',
  awardCertificate: ''
})

// 项目选项列表
const projectOptions = ref<Project[]>([])

// 表单验证规则
const formRules = ref<FormRules>({
  projectId: [{ required: true, message: '请选择项目名称', trigger: 'change' }],
  competitionName: [{ required: true, message: '请输入比赛名称', trigger: 'blur' }],
  competitionLevel: [{ required: true, message: '请选择比赛级别', trigger: 'change' }],
  awardLevel: [{ required: true, message: '请选择获奖等级', trigger: 'change' }]
})

// 文件上传相关
const uploadRef = ref()
const fileList = ref<any[]>([])

// 详情弹窗
const detailDialogVisible = ref(false)
const currentAward = ref<Award | null>(null)

// 审批弹窗
const approveDialogVisible = ref(false)
const approveForm = reactive({
  id: undefined as number | undefined,
  projectName: '',
  competitionName: '',
  action: 'approve' as 'approve' | 'reject',
  rejectReason: ''
})

// 判断获奖记录是否是自己的项目
const isMyProject = (award: Award): boolean => {
  if (!award.projectId) return false
  return myProjectIds.value.has(award.projectId)
}

// 获取图片URL
const getImageUrl = (path: string) => {
  if (!path) return ''
  // 如果是完整URL，直接返回
  if (path.startsWith('http')) return path
  // 如果路径以 uploads 开头，直接使用（通过代理访问）
  if (path.startsWith('uploads/')) {
    return `/api/${path}`
  }
  // 否则拼接API地址
  const baseURL = request.defaults.baseURL || '/api'
  return `${baseURL}/${path}`
}

// 获取获奖记录列表
const fetchAwardList = async () => {
  loading.value = true
  try {
    // 项目负责人需要特殊处理：显示所有已审批的记录 + 自己未审批的记录
    if (isProjectLeader.value) {
      // 获取自己的项目列表并保存
      const myProjects = await getMyProjects() as unknown as Project[]
      myProjectIds.value = new Set(myProjects.map(p => p.id).filter((id): id is number => id !== undefined))
      
      // 先获取所有已审批和已驳回的记录
      const approvedResponse = await getAwardList({
        projectName: searchForm.projectName || undefined,
        competitionName: searchForm.competitionName || undefined,
        competitionLevel: searchForm.competitionLevel || undefined,
        awardLevel: searchForm.awardLevel || undefined,
        page: 1,
        pageSize: 10000 // 获取所有已审批/已驳回的记录
      }) as unknown as PageResult<Award>
      
      // 包括 approved 和 rejected 状态的记录
      const approvedList = (approvedResponse?.list ?? []).filter(award => 
        award.status === 'approved' || award.status === 'rejected'
      )
      
      // 再获取所有未审批的记录，然后过滤出自己未审批的记录
      const allPendingResponse = await getAwardList({
        projectName: searchForm.projectName || undefined,
        competitionName: searchForm.competitionName || undefined,
        competitionLevel: searchForm.competitionLevel || undefined,
        awardLevel: searchForm.awardLevel || undefined,
        page: 1,
        pageSize: 10000
      }) as unknown as PageResult<Award>
      
      // 过滤出自己未审批的记录（通过 projectId 判断）
      const myPendingList = (allPendingResponse?.list ?? []).filter(award => 
        award.status === 'pending' && award.projectId && myProjectIds.value.has(award.projectId)
      )
      
      // 合并列表：已审批的在前，自己未审批的在后
      const allList = [...approvedList, ...myPendingList]
      
      // 分页处理
      const start = (pagination.page - 1) * pagination.pageSize
      const end = start + pagination.pageSize
      awardList.value = allList.slice(start, end)
      pagination.total = allList.length
    } else if (isJudgeOrAuditor.value) {
      // 评委老师或审核人：显示所有获奖记录（包括已审核和未审核的）
      const response = await getAwardList({
        projectName: searchForm.projectName || undefined,
        competitionName: searchForm.competitionName || undefined,
        competitionLevel: searchForm.competitionLevel || undefined,
        awardLevel: searchForm.awardLevel || undefined,
        page: pagination.page,
        pageSize: pagination.pageSize
      }) as unknown as PageResult<Award>
      
      // 响应拦截器已经处理了Result格式，直接返回data部分（PageBean/PageResult）
      awardList.value = response?.list ?? []
      pagination.total = response?.total ?? 0
    } else {
      // 其他角色：正常查询
      const response = await getAwardList({
        projectName: searchForm.projectName || undefined,
        competitionName: searchForm.competitionName || undefined,
        competitionLevel: searchForm.competitionLevel || undefined,
        awardLevel: searchForm.awardLevel || undefined,
        page: pagination.page,
        pageSize: pagination.pageSize
      }) as unknown as PageResult<Award>
      
      // 响应拦截器已经处理了Result格式，直接返回data部分（PageBean/PageResult）
      awardList.value = response?.list ?? []
      pagination.total = response?.total ?? 0
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取获奖记录列表失败')
  } finally {
    loading.value = false
  }
}

// 获取项目列表（用于下拉选择）
// 新增获奖记录：只显示已审批/已入驻的项目
// 编辑获奖记录：为了兼容历史数据，可加载全部项目以保证回显
const fetchProjectOptions = async (includeAll: boolean = false) => {
  try {
    const response = await getMyProjects() as unknown as Project[]
    // 响应拦截器已经处理了Result格式，直接返回data部分
    const list = response || []
    if (includeAll) {
      projectOptions.value = list
    } else {
      projectOptions.value = list.filter(p => p.status === 'approved' || p.status === 'settled')
    }
  } catch (error: any) {
    console.error('获取项目列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  pagination.page = 1
  fetchAwardList()
}

// 重置
const handleReset = () => {
  searchForm.projectName = ''
  searchForm.competitionName = ''
  searchForm.competitionLevel = ''
  searchForm.awardLevel = ''
  pagination.page = 1
  fetchAwardList()
}

// 新增
const handleAdd = () => {
  dialogTitle.value = '新增获奖记录'
  isEdit.value = false
  resetForm()
  fileList.value = []
  dialogVisible.value = true
  fetchProjectOptions(false)
}

// 编辑
const handleEdit = async (row: Award) => {
  dialogTitle.value = '修改获奖记录'
  isEdit.value = true
  resetForm()
  
  try {
    const response = await getAwardById(row.id!) as unknown as Award
    // 响应拦截器已经处理了Result格式，直接返回data部分
    if (response) {
      const award = response
      formData.id = award.id
      formData.projectId = award.projectId
      formData.projectName = award.projectName
      formData.competitionName = award.competitionName
      formData.competitionLevel = award.competitionLevel
      formData.awardLevel = award.awardLevel
      formData.awardCertificate = award.awardCertificate
      
      // 设置文件列表（编辑时显示已有图片）
      if (award.awardCertificate) {
        fileList.value = [{
          name: '获奖证明',
          url: getImageUrl(award.awardCertificate),
          uid: Date.now() // 生成唯一ID
        }]
      } else {
        fileList.value = []
      }
      
      dialogVisible.value = true
      fetchProjectOptions(true)
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取获奖记录详情失败')
  }
}

// 详情
const handleDetail = async (row: Award) => {
  try {
    const response = await getAwardById(row.id!) as unknown as Award
    // 响应拦截器已经处理了Result格式，直接返回data部分
    if (response) {
      currentAward.value = response
      // 调试日志：查看是否有rejectReason
      console.log('获奖记录详情:', response)
      console.log('未通过原因:', response.rejectReason)
      console.log('未通过原因类型:', typeof response.rejectReason)
      detailDialogVisible.value = true
    }
  } catch (error: any) {
    ElMessage.error(error.message || '获取获奖记录详情失败')
  }
}

// 审批
const handleApprove = (row: Award) => {
  approveForm.id = row.id
  approveForm.projectName = row.projectName || ''
  approveForm.competitionName = row.competitionName || ''
  approveForm.action = 'approve'
  approveForm.rejectReason = ''
  approveDialogVisible.value = true
}

// 审批确认
const handleApproveConfirm = async () => {
  try {
    // 如果选择未通过，必须填写原因
    if (approveForm.action === 'reject' && !approveForm.rejectReason?.trim()) {
      ElMessage.warning('请填写未通过原因')
      return
    }
    
    if (approveForm.action === 'approve') {
      await approveAward(approveForm.id!)
      ElMessage.success('审批通过')
    } else {
      await rejectAward(approveForm.id!, approveForm.rejectReason)
      ElMessage.success('审批未通过')
    }
    approveDialogVisible.value = false
    fetchAwardList()
  } catch (error: any) {
    ElMessage.error(error.message || '操作失败')
  }
}

// 删除
const handleDelete = async (row: Award) => {
  try {
    await ElMessageBox.confirm('确定要删除该获奖记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await deleteAward(row.id!)
    ElMessage.success('删除成功')
    fetchAwardList()
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.message || '删除失败')
    }
  }
}

// 项目选择变化
const handleProjectChange = (projectId: number) => {
  const project = projectOptions.value.find(p => p.id === projectId)
  if (project) {
    formData.projectName = project.projectName
  }
}

// 文件上传前检查
const beforeUpload = (file: File) => {
  const isImage = file.type.startsWith('image/')
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isImage) {
    ElMessage.error('只能上传图片文件!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!')
    return false
  }
  return true
}

// 自定义上传方法
const handleCustomUpload = async (options: any) => {
  const { file, onSuccess, onError } = options
  try {
    // 使用API函数上传文件（会经过request拦截器处理）
    const filePath = await uploadAwardCertificate(file) as unknown as string
    // uploadAwardCertificate 经过拦截器后直接返回 data，即文件路径字符串
    formData.awardCertificate = filePath
    // 更新文件列表以显示图片
    const imageUrl = getImageUrl(filePath)
    fileList.value = [{
      name: file.name,
      url: imageUrl,
      uid: file.uid || Date.now()
    }]
    ElMessage.success('上传成功')
    onSuccess && onSuccess({ code: 1, data: filePath }, file)
  } catch (error: any) {
    console.error('上传失败:', error)
    ElMessage.error(error.message || '上传失败')
    fileList.value = []
    onError && onError(error)
  }
}

// 文件移除
const handleRemove = () => {
  formData.awardCertificate = ''
  fileList.value = []
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const submitData = {
      projectId: formData.projectId,
      projectName: formData.projectName,
      competitionName: formData.competitionName,
      competitionLevel: formData.competitionLevel,
      awardLevel: formData.awardLevel,
      awardCertificate: formData.awardCertificate || undefined // 确保是字符串或undefined
    }
    
    if (isEdit.value) {
      await updateAward(formData.id!, submitData)
      ElMessage.success('修改成功')
    } else {
      await addAward(submitData)
      ElMessage.success('新增成功')
    }
    
    dialogVisible.value = false
    fetchAwardList()
  } catch (error: any) {
    if (error?.message && !error?.message.includes('validate')) {
      ElMessage.error(error.message || '操作失败')
    }
  }
}

// 关闭弹窗
const handleDialogClose = () => {
  dialogVisible.value = false
  resetForm()
  fileList.value = []
  formRef.value?.clearValidate()
}

// 重置表单
const resetForm = () => {
  formData.id = undefined
  formData.projectId = undefined
  formData.projectName = ''
  formData.competitionName = ''
  formData.competitionLevel = ''
  formData.awardLevel = ''
  formData.awardCertificate = ''
}

// 分页变化
const handleSizeChange = (size: number) => {
  pagination.pageSize = size
  pagination.page = 1
  fetchAwardList()
}

const handlePageChange = (page: number) => {
  pagination.page = page
  fetchAwardList()
}

// 获取状态类型
const getStatusType = (status?: string) => {
  switch (status) {
    case 'approved':
      return 'success'
    case 'rejected':
      return 'danger'
    case 'pending':
      return 'warning'
    default:
      return ''
  }
}

// 获取状态文本
const getStatusText = (status?: string) => {
  switch (status) {
    case 'approved':
      return '已审批'
    case 'rejected':
      return '已审批未通过'
    case 'pending':
      return '未审批'
    default:
      return status || '-'
  }
}

// 格式化日期
const formatDate = (dateStr?: string) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

// 组件挂载时获取数据
onMounted(() => {
  fetchAwardList()
})
</script>

<style scoped>
.project-awards-container {
  padding: 20px;
  background: white;
  min-height: calc(100vh - 60px);
}

.search-bar {
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 4px;
}

.search-form {
  margin: 0;
}

.action-bar {
  margin-bottom: 20px;
}

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

/* 图片预览样式 */
:deep(.el-image) {
  border-radius: 4px;
}
</style>

