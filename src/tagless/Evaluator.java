package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class Evaluator implements Symantics<Evaluator.Repr> {
    static class Repr<T> implements Of<Repr, T> {
        private T val;

        private Repr(T val) { this.val = val; }

        public String toString() {
            return val.toString();
        }
        public Repr<T> raw() { return this; }
    }

    // Simplifies casting from Of<Repr, T> to Repr<T>:
    private static <T> Repr<T> c(Of<Repr, T> repr) {
        return (Repr<T>) repr;
    }

    public Of<Repr, Integer> int_(int i) {
        return new Repr<>(i);
    }

    public Of<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(b);
    }

    public <A, B> Of<Repr, Function<A, B>> lam(Function<Of<Repr, A>, Of<Repr, B>> f) {
        return new Repr<>(x -> c(f.apply(new Repr<>(x))).val);
    }

    public <A, B> Of<Repr, B> app(Of<Repr, Function<A, B>> f, Of<Repr, A> v) {
        return new Repr<>(c(f).val.apply(((Repr<A>) v).val));
    }

    public <A, B> Of<Repr, Function<A, B>> fix(Function<Of<Repr, Function<A, B>>, Of<Repr, Function<A, B>>> f) {
        class Self {
            Function<Of<Repr, A>, Of<Repr, B>> self = x -> app(f.apply(lam(this.self)), x);
        }
        return lam(new Self().self);
    }

    public Of<Repr, Integer> add(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>( c(a).val + c(b).val );
    }

    public Of<Repr, Integer> mul(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>( c(a).val * c(b).val );
    }

    public Of<Repr, Boolean> leq(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>( c(a).val <= c(b).val );
    }

    public <A> Of<Repr, A> if_(Of<Repr, Boolean> test, Supplier<Of<Repr, A>> tBranch, Supplier<Of<Repr, A>> fBranch) {
        return new Repr<>(c(test).val ? c(tBranch.get()).val : c(fBranch.get()).val);
    }
}
