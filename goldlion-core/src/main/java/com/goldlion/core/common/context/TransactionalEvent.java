package com.goldlion.core.common.context;

import org.springframework.context.ApplicationEvent;

/**
 * 事物事件
 */
public class TransactionalEvent<T> extends ApplicationEvent {

    private T object;

    public TransactionalEvent(T object) {
        super(object);
    }

    @Override
    public final T getSource() {
        return (T) super.getSource();
    }

    public T getObject() {
        return this.getSource();
    }

}
