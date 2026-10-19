import request from '@/utils/request';

/**
 * ${table.comment!}
 */
// ${table.comment!}列表
export function get${entity}List(data = {}, pagination) {
  return request({
    // 注意需要替换模块
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}/search',
    method: 'post',
    data,
    pagination
  });
}

// ${table.comment!}启用
export function ${entity?uncap_first}Enable(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}/batchEnable',
    method: 'post',
    data
  });
}

// ${table.comment!}禁用
export function ${entity?uncap_first}Disable(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}/batchDisable',
    method: 'post',
    data,
  });
}

// ${table.comment!}新增
export function create${entity}(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}',
    method: 'post',
    data
  });
}

// ${table.comment!}主表
export function find${entity}ById(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}/id',
    method: 'post',
    data
  });
}

// ${table.comment!}修改
export function update${entity}(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}',
    method: 'put',
    data
  });
}

// ${table.comment!}删除
export function ${entity?uncap_first}Delete(data) {
  return request({
    url: '/${parentModuleName?uncap_first}/${entity?uncap_first}',
    method: 'delete',
    data
  });
}