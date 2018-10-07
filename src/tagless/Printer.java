package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class Printer implements Symantics<Printer.Repr> {
    static class Repr<T> implements Of<Repr, T> {
        private String s;

        private Repr(String t) { this.s = t; }

        public String toString() {
            return s;
        }
        public String print() {
            return s;
        }
        public Repr raw() {
            return this;
        }
    }

    private int id = 0;
    private String genSym() {
        return "x" + id++;
    }

    public Of<Repr, Integer> int_(int i) {
        return new Repr<>(Integer.toString(i));
    }

    public Of<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(Boolean.toString(b));
    }

    public <A, B> Of<Repr, Function<A, B>> lam(Function<Of<Repr, A>, Of<Repr, B>> f) {
        String sym = genSym();
        return new Repr<>("(λ" + sym + "." + f.apply(new Repr<>(sym)) + ")");
    }

    public <A, B> Of<Repr, B> app(Of<Repr, Function<A, B>> f, Of<Repr, A> v) {
        return new Repr<>("(" + f.raw().print() + " (" + v.raw().print() + "))");
    }

    public <A, B> Of<Repr, Function<A, B>> fix(Function<Of<Repr, Function<A, B>>, Of<Repr, Function<A, B>>> f) {
        String sym = genSym();
        return new Repr<>("[θ(λ" + sym + "." + f.apply(new Repr<>(sym)) + ")]");
    }

    public Of<Repr, Integer> add(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>("(" + a.raw().print() + " + " + b.raw().print() + ")" );
    }

    public Of<Repr, Integer> mul(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>("(" + a.raw().print() + " * " + b.raw().print() + ")");
    }

    public Of<Repr, Boolean> leq(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        return new Repr<>("(" + a.raw().print() + " <= " + b.raw().print() + ")");
    }

    public <A> Of<Repr, A> if_(Of<Repr, Boolean> test, Supplier<Of<Repr, A>> tBranch, Supplier<Of<Repr, A>> fBranch) {
        return new Repr<>("if " + test.raw().print() + " then {" + tBranch.get().raw().print() + "} else {" + fBranch.get().raw().print() + "}");
    }
}
