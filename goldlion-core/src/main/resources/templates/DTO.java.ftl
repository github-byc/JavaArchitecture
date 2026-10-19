package ${package.DTO};

import ${superDTOClassPackage};
import io.swagger.v3.oas.annotations.media.Schema;
<#assign isNotBlank=true/>
<#assign isNotNull=true/>
<#list table.fields as field>
  <#if !field.keyFlag>
    <#if !field.metaInfo.nullable && (isNotBlank || isNotNull)>
      <#if field.propertyType=="String">
import jakarta.validation.constraints.NotBlank;
        <#assign isNotBlank=false/>
      <#else>
import jakarta.validation.constraints.NotNull;
        <#assign isNotNull=false/>
      </#if>
    </#if>
  </#if>
</#list>
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

<#list importEntityJavaPackages as pkg>
import ${pkg};
</#list>

/**
 * <p>
 * ${table.comment!}DTO
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Getter
@Setter
@ToString
@Schema(description = "${table.comment!}DTO")
public class ${entity}DTO extends ${superDTOClass} implements java.io.Serializable {

<#-- ----------  BEGIN 字段循环遍历  ---------->
<#list table.fields as field>
  <#if !field.keyFlag>
    <#if !field.metaInfo.nullable>
      <#if field.propertyType=="String">
    @NotBlank(message = "${field.comment}不能为空")
      <#else>
    @NotNull(message = "${field.comment}不能为空")
      </#if>
    </#if>
    @Schema(description = "${field.comment}")
    private ${field.propertyType} ${field.propertyName};

  </#if>
</#list>
<#------------  END 字段循环遍历  ---------->

}
