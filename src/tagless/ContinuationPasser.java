package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContinuationPasser implements Symantics<ContinuationPasser.Repr> {
    static class Repr<T, U, V> implements Hi<Repr, T> {
        private final Function<Function<U, V>, V> continuationConsumer;

        private Repr(Function<Function<U, V>, V> continuationConsumer) {
            this.continuationConsumer = continuationConsumer;
        }

        public Repr repr() {
            return this;
        }

        public Function<Function<U, V>, V> getContinuationConsumer() {
            return continuationConsumer;
        }

        public T val() {
            throw new UnsupportedOperationException();
        }
    }

    public Hi<Repr, Integer> int_(int i) {
        return new Repr<>(c -> c.apply(i));
    }

    public Hi<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(c -> c.apply(b));
    }

    public <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f) {
        // return new Repr<>(c -> c.apply(f));

        Function<A, Hi<Repr, B>> callByValueInner = vV -> f.apply(new Repr<>(c -> c.apply(vV)));
        Function<Hi<Repr, A>, Hi<Repr, B>> callByValueOuter = v -> (Hi<Repr, B>) v.repr().getContinuationConsumer().apply(callByValueInner);
        return new Repr<>(c -> c.apply(callByValueOuter));
    }

    public <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v) {
        Function<Function<Function<Hi<Repr, A>, Hi<Repr, B>>, ?>, ?> fContCons = f.repr().getContinuationConsumer();
        return new Repr<>(c -> fContCons.apply(fV -> fV.apply(v).repr().getContinuationConsumer().apply(c)));
    }

    public <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f) {
        return null;
    }

    public Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
        return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV + bV))));
    }

    public Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
        return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV * bV))));
    }

    public Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
        return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV <= bV))));
    }

    public <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch) {
        return null;
    }

    public static <A> A run(Hi<Repr, A> r) {
        return (A) r.repr().getContinuationConsumer().apply(Function.identity());
    }
}
