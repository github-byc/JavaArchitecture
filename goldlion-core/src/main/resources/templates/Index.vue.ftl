<!--${entity}-->
<template>
  <div class="app-container">
    <!--列表查询-->
    <search-panel :model.sync="searchData" :show-search="showSearch" :filters="searchFilters" @on-clear="resetQuery" @on-search="getList" />

    <!--列表按钮-->
    <el-row :gutter="10" class="mb8">
      <el-col :span="1.5">
        <el-button
          type="primary"
          plain
          icon="el-icon-plus"
          size="mini"
          @click="handleAdd"
        >新增${table.comment!}</el-button>
      </el-col>
      <el-col :span="1.5">
        <el-button
          type="warning"
          plain
          icon="el-icon-download"
          size="mini"
          @click="handleExport"
        >导出Excel</el-button>
      </el-col>
      <right-toolbar :show-search.sync="showSearch" @queryTable="getList" />
    </el-row>

    <!--列表表单-->
    <el-table v-loading="tableLoading" :data="tableData" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="55" align="center" />
      <el-table-column prop="code" label="${table.comment!}编码" />
      <el-table-column prop="name" label="${table.comment!}名称" :show-overflow-tooltip="true" width="150" />
      <#list table.fields as field>
        <el-table-column prop="${field.propertyName}" label="${field.comment}" />
      </#list>
      <el-table-column prop="status" width="85" align="center" label="是否启用" />
      <el-table-column prop="action" label="操作" width="200" align="center" class-name="small-padding fixed-width">
        <template slot-scope="scope">
          <el-button
            size="mini"
            type="text"
            icon="el-icon-view"
            @click="handleView(scope.row)"
          >查看</el-button>
          <el-button
            size="mini"
            type="text"
            icon="el-icon-edit"
            @click="handleEdit(scope.row)"
          >修改</el-button>
          <el-button
            v-if="scope.row.shelvesStatus == '是'"
            size="mini"
            type="text"
            icon="el-icon-remove-outline"
            @click="handleDisable(scope.row)"
          >停用</el-button>
          <el-button
            v-if="scope.row.shelvesStatus != '是'"
            size="mini"
            type="text"
            icon="el-icon-video-play"
            @click="handleEnable(scope.row)"
          >启用</el-button>
          <el-dropdown size="mini" @command="(c) => handleMore(c, scope.row)">
            <el-button size="mini" type="text" icon="el-icon-d-arrow-right">更多</el-button>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="confirm" icon="el-icon-circle-check">确认</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </template>
      </el-table-column>
    </el-table>

    <!--分页-->
    <pagination
      v-show="total>0"
      :total="total"
      :page.sync="pagination.pageNum"
      :limit.sync="pagination.pageSize"
      @pagination="getList"
    />
  </div>
</template>
<script>
import { mapGetters, mapState } from 'vuex';    // 状态管理
import { get${entity}Management, get${entity}List, putawayStatus } from '@/api/base/${entity}ApiService';   // 后台服务

export default {
  data() {
    return {
      // 显示顶部
      showShop: true,
      // 顶部信息
      shopInfo: '',
      // 显示搜索
      showSearch: true,

      // 搜索数据
      searchData: {},
      searchFilters: [
         {
           label: '编码', // 文本信息
           field: 'code', // 字段
           type: 'input', // 类型
           placeholder: '请输入编码',
           cvalue: '=' // 条件默认值
         },
         {
            label: '名称', // 文本信息
            field: 'name', // 字段
            type: 'input', // 类型
            placeholder: '请输入名称',
            cvalue: 'LIKE' // 条件默认值
          },
          {
            label: '是否启用',
            field: 'status',
            type: 'select',
            source: 'yesNo'
          },
         <#list table.fields as field>
            <#if field.propertyType=="Date">
                {
                  label: '${field.comment}范围',
                  field: '${field.propertyName}',
                  type: 'daterange',
                  csource: 'betweenConditions',
                  cvalue: 'BETWEEN'
                },
            <#else>
                {
                    label: '${field.comment}', // 文本信息
                    field: '${field.propertyName}', // 字段
                    type: 'input', // 类型
                    placeholder: '请输入${field.comment}',
                    cvalue: '=' // 条件默认值
                  },
            </#if>
         </#list>
      ],

      // 分页
      pagination: {
        pageNum: 1,
        pageSize: 10
      },
      // 总条数
      total: 0,
      // 表格选中集合
      selection: [],
      // 表格是否为加载状态
      tableLoading: false,
      // 表格数据
      tableData: []
    };
  },

  computed: {
    // 状态管理-系统设置
    ...mapState(['settings']),

    // 状态管理-用户信息
    ...mapGetters(['userInfo'])
  },

  created() {
    // 加载列表数据
    this.getList();
  },

  methods: {
    // 列表数据
    async getList() {
      this.tableLoading = true;
      const { searchData, pagination } = this;
      try {
        const res = await get${entity}List(searchData.condition, pagination);
        const { total, records } = res.data || {};
        this.total = total || 0;
        this.tableData = records || [];
        this.tableLoading = false;
      } catch (err) {
        this.tableLoading = false;
      }
    },

    // 重置
    resetQuery() {
      this.getList();
    },

    // 新增
    handleAdd() {
      this.$router.push({
        name: '${menuCode?cap_first}/add${entity}'
      });
    },

    // 查看
    handleView(row) {
      const { $router, Base64 } = this;
      const state = Base64.encode(row.id);
      $router.push({
        name: '${menuCode?cap_first}/select${entity}',
        query: { state }
      });
    },

    // 编辑
    handleEdit(row) {
      const { $router, Base64 } = this;
      const state = Base64.encode(row.id);
      $router.push({
        name: '${menuCode?cap_first}/update${entity}',
        query: { state }
      });
    },

    // 更多操作
    handleMore(command, row) {
      switch (command) {
        default:
          break;
      }
    },

    // 停用
    async handleDisable(row) {
      this.$modal.confirm('确认要停用数据吗？').then(() => {
        const { userInfo } = this;
        const params = {
          userId: userInfo.userCode,
          id: row.id,
          version: row.version,
          shelvesStatus: '否'
        };
        return putawayStatus(params);
      }).then((res) => {
        if (res && res.messageType == 'SUCCESS') {
          this.$modal.msgSuccess(res.messageContent);
        } else {
          this.$modal.msgError(res.messageContent);
        }
        this.getList();
      });
    },

    // 启用
    async handleEnable(row) {
      this.$modal.confirm('确认要启用数据吗？').then(() => {
        const { userInfo } = this;
        const params = {
          userId: userInfo.userCode,
          id: row.id,
          version: row.version,
          shelvesStatus: '是'
        };
        return putawayStatus(params);
      }).then((res) => {
        if (res && res.messageType == 'SUCCESS') {
          this.$modal.msgSuccess(res.messageContent);
        } else {
          this.$modal.msgError(res.messageContent);
        }
        this.getList();
      });
    },

    // 表格选择
    handleSelectionChange(selection) {
      this.selection = selection;
    },

    // 导出
    handleExport() {
      const { searchData } = this;
      const exportData = Object.assign({}, searchData.condition, {
        excelCondition: {
          fileName: '${table.comment!}', // 导出文件名称（传给后台）
          includeColumns: [], // 配置的导出字段
          excludeColumns: [] // 排除的导出字段
        }
      });
      //注意修改模块路径
      this.download('/${parentModuleName?cap_first}/${entity}/excel/download', exportData, `${table.comment!}_导出列表_${r"${new Date().getTime()}"}.xlsx`); // 前端导出保存本地方法
    }
  }
};
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.card-box {
  color: #fff;
  height: 40px;
  line-height: 40px;
  font-size: 14px;
  text-align: center;

  ::v-deep .el-card__body  {
    padding: 0;
  }
}

.tag-over {
  display: inline-block;
  vertical-align: middle;
  max-width: 200px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
</style>
