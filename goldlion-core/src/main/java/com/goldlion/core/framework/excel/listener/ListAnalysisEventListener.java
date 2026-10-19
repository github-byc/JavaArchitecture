package com.goldlion.core.framework.excel.listener;

import cn.idev.excel.event.AnalysisEventListener;
import com.goldlion.core.framework.excel.ErrorMessage;

import java.util.List;

/**
 * 集合侦听器
 */
public abstract class ListAnalysisEventListener<T> extends AnalysisEventListener<T> {

    /**
     * 获取 excel 解析的对象列表
     *
     * @return 集合
     */
    public abstract List<T> getList();

    /**
     * 获取异常校验结果
     *
     * @return 集合
     */
    public abstract List<ErrorMessage> getErrors();
}
