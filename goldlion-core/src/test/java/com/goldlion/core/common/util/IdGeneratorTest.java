package com.goldlion.core.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * ID生成器测试(未配置workerId时回退默认推导,行为与hutool一致)
 */
class IdGeneratorTest {

    @Test
    @DisplayName("生成的ID为纯数字且单调递增不重复")
    void uniqueNumericIds() {
        String a = IdGenerator.nextIdStr();
        String b = IdGenerator.nextIdStr();

        assertThat(a).matches("\\d{10,}");
        assertThat(a).isNotEqualTo(b);
        assertThat(Long.parseLong(b)).isGreaterThan(Long.parseLong(a));
    }

}
