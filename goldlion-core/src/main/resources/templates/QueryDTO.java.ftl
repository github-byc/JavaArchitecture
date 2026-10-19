package ${package.DTO};

import ${superQueryDTOClassPackage};
<#list importEntityFrameworkPackages as pkg>
  <#if pkg?contains("baomidou")>
  <#elseif pkg?contains("entity")>
  <#else>
import ${pkg};
  </#if>
</#list>

/**
 * <p>
 * ${table.comment!}QueryDTO
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Getter
@Setter
@ToString
@Schema(description = "${table.comment!}QueryDTO")
public class ${entity}QueryDTO extends ${superQueryDTOClass} implements java.io.Serializable {
}
