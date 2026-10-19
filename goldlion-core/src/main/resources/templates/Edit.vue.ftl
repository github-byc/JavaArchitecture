<!--${entity?lower_case}-edit-->
<#assign colCount=0/>
<template>
  <div class="app-container">
    <h4 class="form-header h4">基本信息</h4>
    <el-form ref="form" v-loading="formLoading" :model="form" :rules="_rules" label-width="120px">
        <#-- ----------  BEGIN 字段循环遍历  ---------->
        <el-row :gutter="20">
            <el-col :span="8">
                <el-form-item label="${table.comment!}编码" prop="code">
                    <el-input v-model="form.code" clearable placeholder="请输入内容" :disabled="disabled" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item label="${table.comment!}名称" prop="name">
                    <el-input v-model="form.name" clearable placeholder="请输入内容" :disabled="disabled" />
                </el-form-item>
            </el-col>
            <el-col :span="8">
                <el-form-item v-if="init.mode != 'add'" label="状态" prop="status">
                    <el-input v-model="form.status" clearable placeholder="请输入内容" disabled />
                </el-form-item>
            </el-col>
        </el-row>
        <el-row :gutter="20">
        <#list table.fields as field>
            <#if field.propertyName == "id" || field.propertyName == "companyId" || field.propertyName == "departmentId" ||
                    field.propertyName == "brandId" || field.propertyName == "createdBy" || field.propertyName == "createdOn" ||
                    field.propertyName == "modifiedBy" || field.propertyName == "modifiedOn" || field.propertyName == "version">
            <#else>
                <#assign colCount=colCount+1/>
                <#if (colCount>1 && ((colCount % 3)=1)) >
            </el-row>
            <el-row :gutter="20">
                </#if>
                <!--${colCount}--><!--${field.propertyName}--><!--${field.propertyType}-->
                <#if field.propertyType=="String">
                <el-col :span="8">
                    <el-form-item label="${field.comment}" prop="${field.propertyName}">
                        <el-input v-model="form.${field.propertyName}" clearable placeholder="请输入内容" :disabled="disabled" />
                    </el-form-item>
                </el-col>
                </#if>
                <#if field.propertyType=="Integer">
                <el-col :span="8">
                    <el-form-item label="${field.comment}" prop="${field.propertyName}">
                        <el-input-number v-model="form.${field.propertyName}" :precision="0" controls-position="right" :disabled="disabled" />
                    </el-form-item>
                </el-col>
                </#if>
                <#if field.propertyType=="Date">
                <el-col :span="8">
                    <el-form-item label="日期时间" prop="${field.propertyName}">
                        <el-date-picker v-model="form.${field.propertyName}" type="datetime" placeholder="选择日期时间" value-format="yyyy-MM-dd HH:mm:ss" :disabled="disabled" />
                    </el-form-item>
                </el-col>
                </#if>
            </#if>
        </#list>
        </el-row>
    </el-form>
    <div style="text-align: center">
        <el-button @click="close">返回</el-button>
        <el-button v-if="!disabled" type="primary" @click="save">确定</el-button>
        <el-button v-if="$route.query.mode == 'add'" @click="resetForm">重置</el-button>
    </div>
  </div>
</template>
<script>
import { mapGetters } from 'vuex';
import { find${entity}ById, create${entity}, update${entity} } from '@/api/base/${entity}ApiService';
import { required, changeRequired, checkRequired, switchRequired, sliderRange, dateRequired, timeRequired, timeRangeRequired, dateRangeRequired, rangeRequired } from '@/utils/form-rule';

export default {
  data() {
    return {
        init: {
            id: '',
            mode: ''
          },
      // 表格加载状态
      formLoading: false,
      // 表格数据
      form: {
        id: '',
        code: '',
        name: '',
        <#list table.fields as field>
            <#if field.propertyType=="Integer">
                ${field.propertyName}: undefined,
            <#else>
                ${field.propertyName}: '',
            </#if>
        </#list>
      },
      // 表格校验
      rules: {
        <#list table.fields as field>
            ${field.propertyName}: [
            <#if !field.metaInfo.nullable>
               <#if field.propertyType=="String">
                required,
                </#if>
                <#if field.propertyType=="Integer">
                required,
                </#if>
                <#if field.propertyType=="Date">
                dateRangeRequired,
                </#if>
            </#if>
            ],
        </#list>
      },
      // 取消表格校验
      norules: {},
    };
  },
  computed: {
    ...mapGetters(['userInfo']),
    disabled() {
      return this.init.mode == 'select';
    },
    _rules() {
      return this.disabled ? this.norules : this.rules;
    }
  },
  created() {
    // 初始化页面
    this.resolve().then(() => {
          this.initPage();
    });
  },
  methods: {
    initPage() {
        const id = this.$route.query.state;
        const mode = this.$route.meta.mode;

        this.init = {
          id: id && this.Base64.decode(id),
          mode
        };

        if (this.init.mode != 'add') {
          this.getData();
        } else {
          this.initFormData();
        }
    },
    // 等待页面状态可用
    resolve() {
      return Promise.resolve();
    },

    // 主表信息
    async getData() {
          this.formLoading = true;
          const id = this.init.id;
          try {
            const data = await find${entity}ById({ id });
            if (data.data) {
              this.form = data.data || {};
              this.formLoading = false;
            }
          } catch (err) {}
    },


    // 初始化表单
    async initFormData() {
      console.log('初始化表单');
    },

    // 返回
    close() {
      // 注意修改路由地址
      const obj = { path: '/xx/xx/xxx' };
      this.$tab.closeOpenPage(obj);
    },

    // 确定
    save() {
      this.$refs.form.validate(async(valid) => {
        if (!valid) return this.$modal.msgWarning('有必填项为空，请检查');
        const { form, $router, Base64, $modal } = this;
        const serviceName = form.id ? update${entity} : create${entity};
        const res = await serviceName(form);

        if (form.id) {
          $modal.msgSuccess(`修改成功`);
          this.getData();
        } else {
          if (res.data) {
            $modal.msgSuccess(`新增成功`);
            this.close();
            const state = Base64.encode(res.data);
                        $router.push({
                          name: '${viewModuleName?cap_first}/select${entity}',
            query: { state }
                        });
          }
        }
      });
    },

    // 重置
    resetForm() {
      this.$refs.form.resetFields();
    }
  }
};
</script>
<style rel="stylesheet/scss" lang="scss" scoped>
.el-form {
	padding: 0 10px 0 0;
}

.el-select, .el-input-number, .el-date-editor {
	width: 100%;
}

.el-checkbox-group {
  height: 36px;
}
</style>