package ${package.Service};

import ${package.DTO}.${entity}DTO;
import ${package.DTO}.${entity}QueryDTO;
import ${package.Entity}.${entity};
import ${superServiceClassPackage};

/**
 * <p>
 * ${table.comment!} 服务类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
public interface ${table.serviceName} extends ${superServiceClass}<${entity}, ${entity}DTO, ${entity}QueryDTO> {
}
