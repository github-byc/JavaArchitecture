package com.goldlion.core.framework.excel;

import cn.hutool.core.util.ReflectUtil;
import cn.idev.excel.util.BooleanUtils;
import cn.idev.excel.write.handler.RowWriteHandler;
import cn.idev.excel.write.handler.context.RowWriteHandlerContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;

import java.lang.reflect.Field;

/**
 * <p>
 * 批注
 * </p>
 *
 * @see <a href="https://idev.cn/fastexcel/zh-CN/docs/write/write_hard#%E6%8F%92%E5%85%A5%E6%89%B9%E6%B3%A8">插入批注</a>
 */
public class CommentWriteHandler implements RowWriteHandler {
    private Class<?> clazz;

    public CommentWriteHandler(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public void afterRowDispose(RowWriteHandlerContext context) {
        if (BooleanUtils.isTrue(context.getHead())) {
            Sheet sheet = context.getWriteSheetHolder().getSheet();
            Row row = sheet.getRow(0);

            Field[] excelFields = ReflectUtil.getFields(clazz);
            for (int i = 0; i < excelFields.length; i++) {
                Field excelField = excelFields[i];
                if (excelField.isAnnotationPresent(NotBlank.class) || excelField.isAnnotationPresent(NotNull.class) || excelField.isAnnotationPresent(NotEmpty.class)) {
                    // 创建批注
                    Drawing<?> drawingPatriarch = sheet.createDrawingPatriarch();
                    Comment comment = drawingPatriarch.createCellComment(
                            new XSSFClientAnchor(0, 0, 0, 0, (short) 1, 0, (short) 2, 1));
                    comment.setString(new XSSFRichTextString("必填！"));
                    row.getCell(i).setCellComment(comment);
                }
            }
        }
    }

}
