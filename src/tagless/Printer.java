package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class Printer implements Symantics<Printer.Repr> {
    static class Repr<T> implements Hi<Repr, T> {
        private String s;

        private Repr(String t) { this.s = t; }

        public String toString() {
            return s;
        }
        public String print() {
            return s;
        }
        public Repr repr() {
            return this;
        }
        public T val() {
            throw new UnsupportedOperationException();
        }
    }

    private int id = 0;
    private String genSym() {
        return "x" + id++;
    }

    public Hi<Repr, Integer> int_(int i) {
        return new Repr<>(Integer.toString(i));
    }

    public Hi<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(Boolean.toString(b));
    }

    public <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f) {
        String sym = genSym();
        return new Repr<>("(λ" + sym + "." + f.apply(new Repr<>(sym)) + ")");
    }

    public <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v) {
        return new Repr<>("(" + f.repr().print() + " (" + v.repr().print() + "))");
    }

    public <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f) {
        String sym = genSym();
        return new Repr<>("[θ(λ" + sym + "." + f.apply(new Repr<>(sym)) + ")]");
    }

    public Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>("(" + a.repr().print() + " + " + b.repr().print() + ")" );
    }

    public Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>("(" + a.repr().print() + " * " + b.repr().print() + ")");
    }

    public Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        return new Repr<>("(" + a.repr().print() + " <= " + b.repr().print() + ")");
    }

    public <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch) {
        return new Repr<>("if " + test.repr().print() + " then {" + tBranch.get().repr().print() + "} else {" + fBranch.get().repr().print() + "}");
    }
}
