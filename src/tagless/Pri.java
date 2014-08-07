package tagless;

import javax.naming.OperationNotSupportedException;

class Pri<T> implements HiRepr<Pri, T> {
    String s;
    Pri(String t) { this.s = t; }

    public String toString() {
        return s;
    }
    public String print() {
        return s;
    }
    public Pri repr() {
        return this;
    }
    public T val() {
        throw new UnsupportedOperationException();
    }
}
