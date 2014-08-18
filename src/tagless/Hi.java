package tagless;

public interface Hi<Repr, A> {
    Repr repr();
    A val();
}
