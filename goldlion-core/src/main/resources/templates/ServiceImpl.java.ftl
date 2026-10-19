package ${package.ServiceImpl};

import ${package.Mapper}.${table.mapperName};
import ${package.DTO}.${entity}DTO;
import ${package.DTO}.${entity}QueryDTO;
import ${package.Entity}.${entity};
import ${package.Service}.${table.serviceName};
import ${superServiceImplClassPackage};
import org.springframework.stereotype.Service;

/**
 * <p>
 * ${table.comment!} 服务实现类
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Service
public class ${table.serviceImplName}
        extends ${superServiceImplClass}<${table.mapperName}, ${entity}, ${entity}DTO, ${entity}QueryDTO>
        implements ${table.serviceName} {
}
