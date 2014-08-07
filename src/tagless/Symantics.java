package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Symantics<Repr> {
    HiRepr<Repr, Integer> int_(int i);
    HiRepr<Repr, Boolean> bool_(boolean b);
    <A, B> HiRepr<Repr, Function<A, B>> lambda(Function<HiRepr<Repr, A>, HiRepr<Repr, B>> f);
    <A, B> HiRepr<Repr, B> app(HiRepr<Repr, Function<A, B>> f, HiRepr<Repr, A> v);
    <A, B> HiRepr<Repr, Function<A, B>> fix(Function<HiRepr<Repr, Function<A, B>>, HiRepr<Repr, Function<A, B>>> f);

    HiRepr<Repr, Integer> add(HiRepr<Repr, Integer> a, HiRepr<Repr, Integer> b);
    HiRepr<Repr, Integer> mul(HiRepr<Repr, Integer> a, HiRepr<Repr, Integer> b);
    HiRepr<Repr, Boolean> leq(HiRepr<Repr, Integer> a, HiRepr<Repr, Integer> b);

    <A> HiRepr<Repr, A> if_(HiRepr<Repr, Boolean> test, Supplier<HiRepr<Repr, A>> tBranch, Supplier<HiRepr<Repr, A>> fBranch);
}
