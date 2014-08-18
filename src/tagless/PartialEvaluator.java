package tagless;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PartialEvaluator implements Symantics<PartialEvaluator.Repr> {
    // We are cheating here, i.e. we need to cast in the partial evaluator, but only in there!
    // T is the represented type, whereas U is the static value's type.
    // See lam: Hi<Repr, Function<A, B>>'s static value actually is of type Function<Hi<Repr, A>, Hi<Repr, B>>
    // This could be "solved" by adding another generic type parameter to Hi.
    public static class Repr<T, U> implements Hi<Repr, T> {
        private final Supplier<String> lazyCode;
        private final Optional<U> staticValue;

        private Repr(Supplier<String> lazyCode) {
            this.lazyCode = lazyCode;
            this.staticValue = Optional.empty();
        }

        public Repr(Supplier<String> lazyCode, U staticValue) {
            this.lazyCode = lazyCode;
            this.staticValue = Optional.of(staticValue);
        }

        // public Supplier<String> getLazyCode() { return lazyCode; }
        public String getCode() { return lazyCode.get(); }
        public Optional<U> getStaticValue() { return staticValue; }
        public String toString() {
            // return "(" + code.get() + ", " + staticValue + ")";
            return lazyCode.get();
        }
        public Repr repr() { return this; }
        public T val() {
            throw new UnsupportedOperationException();
        }
    }

    private int maxUnfoldDepth = 10;

    private int id = 0;
    private String genSym() {
        return "x" + id++;
    }

    public Hi<Repr, Integer> int_(int i) {
        return new Repr<>(() -> Integer.toString(i), i);
    }

    public Hi<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(() -> Boolean.toString(b), b);
    }

    public <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f) {
        return new Repr<>(
            () -> {
                String sym = genSym();
                return "(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).repr().getCode() + ")";
            },
            f
        );
    }

    public <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v) {
        Optional<Function<Hi<Repr, A>, Hi<Repr, B>>> fValue = f.repr().getStaticValue();

        if (fValue.isPresent()) {
            Hi<Repr, B> r = fValue.get().apply(v);
            return r;
        } else {
            return new Repr<>(() -> f.repr().getCode() + ".apply(" + v.repr().getCode() + ")");
        }
    }

    public <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f) {
        /*
        class Self {
            private int unfoldDepth = 0;

            Function<HiRepr<Par, A>, HiRepr<Par, B>> self = x -> {
                if (this.unfoldDepth <= maxUnfoldDepth) {
                    this.unfoldDepth++;
                    return app(f.apply(lam(this.self)), x);
                } else {
                    return new Par<>(
                        () -> {
                            String sym = genSym();
                            return "fix(" + sym + " -> " + f.apply(new Par<>(() -> sym)).repr().getCode() + ").apply(" + x.repr().getCode() + ")";
                        }
                    );
                }
            };
        }
        */

        class Self {
            Function<Hi<Repr, A>, Hi<Repr, B>> self = x -> {
                if (x.repr().getStaticValue().isPresent()) {
                    return app(f.apply(lam(this.self)), x);
                } else {
                    return new Repr<>(
                        () -> {
                            String sym = genSym();
                            return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).repr().getCode() + ").apply(" + x.repr().getCode() + ")";
                        }
                    );
                }
            };
        }

        return new Repr<>(
            () -> {
                String sym = genSym();
                return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).repr().getCode() + ")";
            },
            new Self().self
        );

        /*
        class Self {
            private int unfoldDepth = 0;

            HiRepr<Par, Function<A, B>> self =
                    new Par<Function<A, B>, Function<HiRepr<Par, A>, HiRepr<Par, B>>>(
                            () -> {
                                String sym = genSym();
                                return "fix(" + sym + " -> " + f.apply(new Par<>(() -> sym)).repr().getCode() + ")";
                            },
                            x -> {
                                if (this.unfoldDepth <= maxUnfoldDepth) {
                                    this.unfoldDepth++;
                                    return ((Function<HiRepr<Par, A>, HiRepr<Par, B>>) f.apply(this.self).repr().getValue().get()).apply(x);
                                } else {
                                    return new Par<>(
                                        () -> {
                                            String sym = genSym();
                                            return "fix(" + sym + " -> " + f.apply(new Par<>(() -> sym)).repr().getCode() + ").apply(" + x.repr().getCode() + ")";
                                        }
                                    );
                                }
                            }
                    );
        }
        */

        /*
        class Self {
            HiRepr<Par, Function<A, B>> self =
                new Par<Function<A, B>, Function<HiRepr<Par, A>, HiRepr<Par, B>>>(
                    () -> {
                        String sym = genSym();
                        return "fix(" + sym + " -> " + f.apply(new Par<>(() -> sym)).repr().getCode() + ")";
                    },
                    x -> {
                        if (x.repr().getValue().isPresent()) {
                            return ((Function<HiRepr<Par, A>, HiRepr<Par, B>>) f.apply(this.self).repr().getValue().get()).apply(x);
                        } else {
                            return new Par<>(
                                () -> {
                                    String sym = genSym();
                                    return "fix(" + sym + " -> " + f.apply(new Par<>(() -> sym)).repr().getCode() + ").apply(" + x.repr().getCode() + ")";
                                }
                            );
                        }
                    }
                );
        }

        return new Self().self;
        */
    }

    public Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Optional<Integer> aValue = a.repr().getStaticValue();
        Optional<Integer> bValue = b.repr().getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Integer result = aValue.get() + bValue.get();
            return new Repr<>(() -> result.toString(), result);
        } else if (aValue.isPresent() && aValue.get() == 0) {
            return b;
        } else if (bValue.isPresent() && bValue.get() == 0) {
            return a;
        } else {
            return new Repr<>(() -> "(" + a.repr().getCode() + " + " + b.repr().getCode() + ")");
        }
    }

    public Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Optional<Integer> aValue = a.repr().getStaticValue();
        Optional<Integer> bValue = b.repr().getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Integer result = aValue.get() * bValue.get();
            return new Repr<>(() -> result.toString(), result);
        } else if (aValue.isPresent() && aValue.get() == 0 || bValue.isPresent() && bValue.get() == 0) {
            return new Repr<>(() -> "(0)", 0);
        } else if (aValue.isPresent() && aValue.get() == 1) {
            return b;
        } else if (bValue.isPresent() && bValue.get() == 1) {
            return a;
        } else {
            return new Repr<>(() -> "(" + a.repr().getCode() + " * " + b.repr().getCode() + ")");
        }
    }

    public Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Optional<Integer> aValue = a.repr().getStaticValue();
        Optional<Integer> bValue = b.repr().getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Boolean result = aValue.get() <= bValue.get();
            return new Repr<>(() -> result.toString(), result);
        } else {
            return new Repr<>(() -> "(" + a.repr().getCode() + " <= " + b.repr().getCode() + ")");
        }
    }

    public <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch) {
        Optional<Boolean> testValue = test.repr().getStaticValue();

        if (testValue.isPresent()) {
            if (testValue.get()) {
                return tBranch.get();
            } else {
                return fBranch.get();
            }
        } else {
            return new Repr<>(() -> test.repr().getCode() + " ? " + tBranch.get().repr().getCode() + " : " + fBranch.get().repr().getCode());
        }
    }
}
