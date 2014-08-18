package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public interface Symantics<Repr> {
    Hi<Repr, Integer> int_(int i);
    Hi<Repr, Boolean> bool_(boolean b);
    <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f);
    <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v);
    <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f);

    Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b);
    Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b);
    Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b);

    <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch);
}
