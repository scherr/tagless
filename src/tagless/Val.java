package tagless;

class Val<T> implements HiRepr<Val, T> {
    private T val;
    public Val(T val) { this.val = val; }

    public String toString() {
        return val.toString();
    }
    public T val() {
        return val;
    }
    public Val<T> repr() {
        return this;
    }
}
