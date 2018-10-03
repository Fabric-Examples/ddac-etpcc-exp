package org.etpcc.algorithms;

/**
 * Created by liangjiao on 17/11/1.
 */
public class TupleModel<A, B> {
    public final A left;
    public final B right;

    public TupleModel(A a, B b) {
        this.left = a;
        this.right = b;
    }

    public A getLeft() {
        return left;
    }

    public B getRight() {
        return right;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TupleModel<?, ?> that = (TupleModel<?, ?>) o;

        if ((left.equals(that.left) && right.equals(that.right) || (left.equals(that.right) && right.equals(that.left)))) return true;
        return false;
    }

    @Override
    public int hashCode() {
        int result = left.hashCode();
        result = result + right.hashCode();
        return result;
    }
}
