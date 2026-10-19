package com.goldlion.core.common.function;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Tuple2<T1, T2> {
    final T1 t1;
    final T2 t2;

    public Tuple2(T1 t1, T2 t2) {
        this.t1 = Objects.requireNonNull(t1, "t1");
        this.t2 = Objects.requireNonNull(t2, "t2");
    }

    public Tuple2(T1 t1, T2 t2, boolean allowedT2Empty) {
        this.t1 = Objects.requireNonNull(t1, "t1");
        if (allowedT2Empty) {
            this.t2 = t2;
        } else {
            this.t2 = Objects.requireNonNull(t2, "t2");
        }
    }

}
