package ${package.VO};

import cn.idev.excel.annotation.ExcelProperty;
import ${superVOClassPackage};
<#list importEntityFrameworkPackages as pkg>
  <#if pkg?contains("baomidou")>
  <#elseif pkg?contains("entity")>
  <#else>
import ${pkg};
  </#if>
</#list>

<#list importEntityJavaPackages as pkg>
import ${pkg};
</#list>

/**
 * <p>
 * ${table.comment!}VO
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Getter
@Setter
@ToString
@Schema(description = "${table.comment!}VO")
public class ${entity}VO extends ${superVOClass} implements java.io.Serializable {

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
  <#if !field.keyFlag>
    @ExcelProperty(value = "${field.comment}")
    @Schema(description = "${field.comment}")
    private ${field.propertyType} ${field.propertyName};

  </#if>
</#list>
<#------------  END 字段循环遍历  ---------->

}
