package ${package.Controller};

import ${package.DTO}.${entity}DTO;
import ${package.DTO}.${entity}QueryDTO;
import ${package.DTO}.excel.${entity}UploadDTO;
import ${package.ServiceImpl}.${table.serviceImplName};
import ${package.VO}.${entity}VO;
import ${superControllerClassPackage};
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * ${table.comment!} 前端控制器
 * </p>
 *
 * @author ${author}
 * @since ${date}
 */
@Tag(name = "${table.comment!}-前端控制器")
@RestController
@RequestMapping("/${parentModuleName}/${table.entityPath}")
public class ${table.controllerName} extends ${superControllerClass}<${table.serviceImplName}, ${entity}DTO, ${entity}QueryDTO, ${entity}VO, ${entity}UploadDTO> {
}