package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

class Evaluator implements Symantics<Val> {
    public HiRepr<Val, Integer> int_(int i) {
        return new Val<>(i);
    }

    public HiRepr<Val, Boolean> bool_(boolean b) {
        return new Val<>(b);
    }

    public <A, B> HiRepr<Val, Function<A, B>> lambda(Function<HiRepr<Val, A>, HiRepr<Val, B>> f) {
        return new Val<>( x -> f.apply(new Val<>(x)).val() );
    }

    public <A, B> HiRepr<Val, B> app(HiRepr<Val, Function<A, B>> f, HiRepr<Val, A> v) {
        return new Val<>( f.val().apply(v.val()) );
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
    public <A, B> HiRepr<Val, Function<A, B>> fix(Function<HiRepr<Val, Function<A, B>>, HiRepr<Val, Function<A, B>>> f) {
        class Self { HiRepr<Val, Function<A, B>> self = new Val<>(x -> (f.apply(this.self).val()).apply(x)); }
        return new Self().self;
    }

    public HiRepr<Val, Integer> add(HiRepr<Val, Integer> a, HiRepr<Val, Integer> b) {
        return new Val<>( a.val() + b.val() );
    }

    public HiRepr<Val, Integer> mul(HiRepr<Val, Integer> a, HiRepr<Val, Integer> b) {
        return new Val<>( a.val() * b.val() );
    }

    public HiRepr<Val, Boolean> leq(HiRepr<Val, Integer> a, HiRepr<Val, Integer> b) {
        return new Val<>( a.val() <= b.val() );
    }

    public <A> HiRepr<Val, A> if_(HiRepr<Val, Boolean> test, Supplier<HiRepr<Val, A>> tBranch, Supplier<HiRepr<Val, A>> fBranch) {
        return new Val<>(test.val() ? tBranch.get().val() : fBranch.get().val());
    }
}
