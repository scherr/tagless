package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Symantics<Repr> {
    Of<Repr, Integer> int_(int i);
    Of<Repr, Boolean> bool_(boolean b);
    <A, B> Of<Repr, Function<A, B>> lam(Function<Of<Repr, A>, Of<Repr, B>> f);
    <A, B> Of<Repr, B> app(Of<Repr, Function<A, B>> f, Of<Repr, A> v);
    <A, B> Of<Repr, Function<A, B>> fix(Function<Of<Repr, Function<A, B>>, Of<Repr, Function<A, B>>> f);

    Of<Repr, Integer> add(Of<Repr, Integer> a, Of<Repr, Integer> b);
    Of<Repr, Integer> mul(Of<Repr, Integer> a, Of<Repr, Integer> b);
    Of<Repr, Boolean> leq(Of<Repr, Integer> a, Of<Repr, Integer> b);

    <A> Of<Repr, A> if_(Of<Repr, Boolean> test, Supplier<Of<Repr, A>> tBranch, Supplier<Of<Repr, A>> fBranch);
}
