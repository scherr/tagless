package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

class HaskellPrinter implements Symantics<Pri> {
    private int id = 0;
    private String gensym() {
        return "x" + id++;
    }

    public HiRepr<Pri, Integer> int_(int i) {
        return new Pri<>( "(" + Integer.toString(i) + ")" );
    }

    public HiRepr<Pri, Boolean> bool_(boolean b) {
        return new Pri<>( b ? "(True)" : "(False)" );
    }

    public <A, B> HiRepr<Pri, Function<A, B>> lambda(Function<HiRepr<Pri, A>, HiRepr<Pri, B>> f) {
        String sym = gensym();
        return new Pri<>( "(\\" + sym + " -> " + f.apply(new Pri<>(sym)) + ")" );
    }

    public <A, B> HiRepr<Pri, B> app(HiRepr<Pri, Function<A, B>> f, HiRepr<Pri, A> v) {
        return new Pri<>( "(" + f.repr().print() + "(" + v.repr().print() + "))" );
    }

    public <A, B> HiRepr<Pri, Function<A, B>> fix(Function<HiRepr<Pri, Function<A, B>>, HiRepr<Pri, Function<A, B>>> f) {
        String sym = gensym();
        return new Pri<>( "(fix (\\" + sym + " -> " + f.apply(new Pri<>(sym)) + "))" );
    }

    public HiRepr<Pri, Integer> add(HiRepr<Pri, Integer> a, HiRepr<Pri, Integer> b) {
        return new Pri<>( "(" + a.repr().print() + " + " + b.repr().print() + ")" );
    }

    public HiRepr<Pri, Integer> mul(HiRepr<Pri, Integer> a, HiRepr<Pri, Integer> b) {
        return new Pri<>( "(" + a.repr().print() + " * " + b.repr().print() + ")" );
    }

    public HiRepr<Pri, Boolean> leq(HiRepr<Pri, Integer> a, HiRepr<Pri, Integer> b) {
        return new Pri<>( "(" + a.repr().print() + " <= " + b.repr().print() + ")" );
    }

    public <A> HiRepr<Pri, A> if_(HiRepr<Pri, Boolean> test, Supplier<HiRepr<Pri, A>> tBranch, Supplier<HiRepr<Pri, A>> fBranch) {
        return new Pri<>( "(if " + test.repr().print() + " then " + tBranch.get().repr().print() + " else " + fBranch.get().repr().print() + ")" );
    }
}
