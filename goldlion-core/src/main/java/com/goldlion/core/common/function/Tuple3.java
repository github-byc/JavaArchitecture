package com.goldlion.core.common.function;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

@Getter
@Setter
public class Tuple3<T1, T2, T3> extends Tuple2<T1, T2> {
    final T3 t3;

    public Tuple3(T1 t1, T2 t2, T3 t3) {
        super(t1, t2);
        this.t3 = Objects.requireNonNull(t3, "t3");
    }
}
