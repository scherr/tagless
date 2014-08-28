package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class Evaluator implements Symantics<Evaluator.Repr> {
    static class Repr<T> implements Hi<Repr, T> {
        private T val;

        private Repr(T val) { this.val = val; }

        public String toString() {
            return val.toString();
        }
        public T val() {
            return val;
        }
        public Repr<T> repr() {
            return this;
        }
    }

    public Hi<Repr, Integer> int_(int i) {
        return new Repr<>(i);
    }

    public Hi<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(b);
    }

    public <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f) {
        return new Repr<>(x -> f.apply(new Repr<>(x)).val());
    }

    public <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v) {
        return new Repr<>(f.val().apply(v.val()));
    }

    /*
        public static <A, B> Function<A, B> fix2(Function<Function<A, B>, Function<A, B>> f) {
            class Self { Function<A, B> self = x -> f.apply(this.self).apply(x); }
            return new Self().self;
        }
    */
    /*
        public static <A, B> Function<A, B> fix(Function<Function<A, B>, Function<A, B>> f) {
            return x -> f.apply(fix(f)).apply(x);
        }
    */
    public <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f) {
        /*
        class Self { Hi<Repr, Function<A, B>> self = new Val<>(x -> (f.apply(this.self).val()).apply(x)); }
        return new Self().self;
        */

        class Self { Function<Hi<Repr, A>, Hi<Repr, B>> self = x -> app(f.apply(lam(this.self)), x); }
        return lam(new Self().self);
    }

    public Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>( a.val() + b.val() );
    }

    public Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>( a.val() * b.val() );
    }

    public Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>( a.val() <= b.val() );
    }

    public <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch) {
        return new Repr<>(test.val() ? tBranch.get().val() : fBranch.get().val());
    }
}
