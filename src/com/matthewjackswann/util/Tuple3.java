package com.matthewjackswann.util;

public class Tuple3<Q, R, S> {
    private final Q first;
    private final R second;
    private final S third;

    public Tuple3(Q first, R second, S third) {
        this.first = first;
        this.second = second;
        this.third = third;
    }

    public Q getFirst() {
        return first;
    }

    public R getSecond() {
        return second;
    }

    public S getThird() {
        return third;
    }

    @Override
    public String toString() {
        return "Tuple3{first=" + first +
                ", second=" + second +
                ", third=" + third +
                '}';
    }
}
