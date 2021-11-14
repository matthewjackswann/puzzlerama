package com.swannyscode.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Tuple2<Q, R> {

    private final Q first;
    private final R second;

    public Tuple2(Q first, R second) {
        this.first = first;
        this.second = second;
    }

    public Q getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }

    public static <L, M, N, O> List<Tuple2<Tuple2<L, M>, Tuple2<N, O>>> cartesianProduct(List<Tuple2<L, M>> list1, List<Tuple2<N, O>> list2) {
        List<Tuple2<Tuple2<L, M>, Tuple2<N, O>>> result = new ArrayList<>();
        for (Tuple2<L, M> elem1 : list1) {
            for (Tuple2<N, O> elem2 : list2) {
                result.add(new Tuple2<>(elem1, elem2));
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "Tuple2{" + first + "," + second + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Tuple2)) return false;
        Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
        if (!Objects.equals(first, tuple2.first)) return false;
        return Objects.equals(second, tuple2.second);
    }

    @Override
    public int hashCode() {
        int result = first != null ? first.hashCode() : 0;
        result = 31 * result + (second != null ? second.hashCode() : 0);
        return result;
    }
}
