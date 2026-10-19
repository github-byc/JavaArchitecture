package com.goldlion.core.framework.excel.listener;

import cn.idev.excel.context.AnalysisContext;
import cn.idev.excel.read.metadata.holder.ReadRowHolder;
import com.goldlion.core.common.exception.ExcelException;
import com.goldlion.core.fixture.TestUploadDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Excel导入默认监听器测试:逐行校验 + 行数上限保护
 */
class DefaultUploadDataListenerTest {

    private final AnalysisContext ctx = mock(AnalysisContext.class);
    private final ReadRowHolder rowHolder = mock(ReadRowHolder.class);

    private DefaultUploadDataListener listenerWithLimit(int max) {
        DefaultUploadDataListener listener = new DefaultUploadDataListener() {
            @Override
            protected int getMaxRows() {
                return max;
            }
        };
        when(ctx.readRowHolder()).thenReturn(rowHolder);
        when(rowHolder.getRowIndex()).thenReturn(0, 1, 2, 3, 4);
        return listener;
    }

    private TestUploadDTO row(String name) {
        TestUploadDTO d = new TestUploadDTO();
        d.setName(name);
        return d;
    }

    @Test
    @DisplayName("合法行进入数据列表(带行号),校验失败行进入错误列表")
    void validAndInvalidRows() {
        DefaultUploadDataListener listener = listenerWithLimit(100);

        listener.invoke(row("ok"), ctx);
        listener.invoke(row(" "), ctx);

        assertThat(listener.getList()).hasSize(1);
        assertThat(listener.getList().get(0).getLineNum()).isEqualTo(1L);
        assertThat(listener.getErrors()).hasSize(1);
        assertThat(listener.getErrors().get(0).getLineNum()).isEqualTo(2L);
    }

    @Test
    @DisplayName("超过行数上限:立即抛ExcelException终止导入")
    void exceedMaxRows() {
        DefaultUploadDataListener listener = listenerWithLimit(2);

        listener.invoke(row("a"), ctx);
        listener.invoke(row("b"), ctx);

        assertThatThrownBy(() -> listener.invoke(row("c"), ctx))
                .isInstanceOf(ExcelException.class)
                .hasMessageContaining("最大行数");
    }

}
