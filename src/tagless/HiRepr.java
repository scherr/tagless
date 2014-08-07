package tagless;

public interface HiRepr<Repr, A> {
    Repr repr();
    A val();
}
