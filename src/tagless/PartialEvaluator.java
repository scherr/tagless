package tagless;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class PartialEvaluator implements Symantics<PartialEvaluator.Repr> {
    // We are cheating here, i.e. we need to cast in the partial evaluator, but only in there!
    // T is the represented type, whereas U is the static value's type.
    // See lam: Of<Repr, Function<A, B>>'s static value actually is of type Function<Of<Repr, A>, Of<Repr, B>>
    public static class Repr<T, U> implements Of<Repr, T> {
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
        Optional<U> getStaticValue() { return staticValue; }
        public String toString() {
            // return "(" + code.get() + ", " + staticValue + ")";
            return lazyCode.get();
        }
        public Repr raw() { return this; }
    }

    private static <T> Repr<T, T> c(Of<Repr, T> repr) {
        return (Repr<T, T>) repr;
    }

    private final boolean limitUnfolding;
    private final int maxUnfoldDepth;

    private int id = 0;
    private String genSym() {
        return "x" + id++;
    }

    public PartialEvaluator() {
        this(false, 0);
    }

    public PartialEvaluator(boolean limitUnfolding, int maxUnfoldDepth) {
        this.limitUnfolding = limitUnfolding;
        this.maxUnfoldDepth = maxUnfoldDepth;
    }

    public Of<Repr, Integer> int_(int i) {
        return new Repr<>(() -> Integer.toString(i), i);
    }

    public Of<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(() -> Boolean.toString(b), b);
    }

    public <A, B> Of<Repr, Function<A, B>> lam(Function<Of<Repr, A>, Of<Repr, B>> f) {
        return new Repr<>(
            () -> {
                String sym = genSym();
                return "(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ")";
            },
            f
        );
    }

    public <A, B> Of<Repr, B> app(Of<Repr, Function<A, B>> f, Of<Repr, A> v) {
        Optional<Function<Of<Repr, A>, Of<Repr, B>>> fValue = f.raw().getStaticValue();

        if (fValue.isPresent()) {
            Of<Repr, B> r = fValue.get().apply(v);
            return r;
        } else {
            return new Repr<>(() -> f.raw().getCode() + ".apply(" + v.raw().getCode() + ")");
        }
    }

    public <A, B> Of<Repr, Function<A, B>> fix(Function<Of<Repr, Function<A, B>>, Of<Repr, Function<A, B>>> f) {
        if (limitUnfolding) {
            class Self {
                private int unfoldDepth = 0;

                Function<Of<Repr, A>, Of<Repr, B>> self = x -> {
                    if (this.unfoldDepth <= maxUnfoldDepth) {
                        this.unfoldDepth++;
                        return app(f.apply(lam(this.self)), x);
                    } else {
                        return new Repr<>(
                            () -> {
                                String sym = genSym();
                                return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ").apply(" + x.raw().getCode() + ")";
                            }
                        );
                    }
                };
            }

            return new Repr<>(
                    () -> {
                        String sym = genSym();
                        return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ")";
                    },
                    new Self().self
            );
        } else {
            class Self {
                Function<Of<Repr, A>, Of<Repr, B>> self = x -> {
                    if (x.raw().getStaticValue().isPresent()) {
                        return app(f.apply(lam(this.self)), x);
                    } else {
                        return new Repr<>(
                            () -> {
                                String sym = genSym();
                                return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ").apply(" + x.raw().getCode() + ")";
                            }
                        );
                    }
                };
            }

            return new Repr<>(
                    () -> {
                        String sym = genSym();
                        return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ")";
                    },
                    new Self().self
            );
        }

        /*
        class Self {
            private int unfoldDepth = 0;

            Of<Repr, Function<A, B>> self =
                new Repr<Function<A, B>, Function<Of<Repr, A>, Of<Repr, B>>>(
                        () -> {
                            String sym = genSym();
                            return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ")";
                        },
                        x -> {
                            if (this.unfoldDepth <= maxUnfoldDepth) {
                                this.unfoldDepth++;
                                return ((Function<Of<Repr, A>, Of<Repr, B>>) f.apply(this.self).raw().getStaticValue().get()).apply(x);
                            } else {
                                return new Repr<>(
                                    () -> {
                                        String sym = genSym();
                                        return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ").apply(" + x.raw().getCode() + ")";
                                    }
                                );
                            }
                        }
                );
        }
        */

        /*
        class Self {
            Of<Repr, Function<A, B>> self =
                new Repr<Function<A, B>, Function<Of<Repr, A>, Of<Repr, B>>>(
                    () -> {
                        String sym = genSym();
                        return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ")";
                    },
                    x -> {
                        if (x.raw().getStaticValue().isPresent()) {
                            return ((Function<Of<Repr, A>, Of<Repr, B>>) f.apply(this.self).raw().getStaticValue().get()).apply(x);
                        } else {
                            return new Repr<>(
                                () -> {
                                    String sym = genSym();
                                    return "fix(" + sym + " -> " + f.apply(new Repr<>(() -> sym)).raw().getCode() + ").apply(" + x.raw().getCode() + ")";
                                }
                            );
                        }
                    }
                );
        }

        return new Self().self;
        */
    }

    public Of<Repr, Integer> add(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Optional<Integer> aValue = c(a).getStaticValue();
        Optional<Integer> bValue = c(b).getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Integer result = aValue.get() + bValue.get();
            return new Repr<>(result::toString, result);
        } else if (aValue.isPresent() && aValue.get() == 0) {
            return b;
        } else if (bValue.isPresent() && bValue.get() == 0) {
            return a;
        } else {
            return new Repr<>(() -> "(" + a.raw().getCode() + " + " + b.raw().getCode() + ")");
        }
    }

    public Of<Repr, Integer> mul(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Optional<Integer> aValue = c(a).getStaticValue();
        Optional<Integer> bValue = c(b).getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Integer result = aValue.get() * bValue.get();
            return new Repr<>(result::toString, result);
        } else if (aValue.isPresent() && aValue.get() == 0 || bValue.isPresent() && bValue.get() == 0) {
            return new Repr<>(() -> "(0)", 0);
        } else if (aValue.isPresent() && aValue.get() == 1) {
            return b;
        } else if (bValue.isPresent() && bValue.get() == 1) {
            return a;
        } else {
            return new Repr<>(() -> "(" + a.raw().getCode() + " * " + b.raw().getCode() + ")");
        }
    }

    public Of<Repr, Boolean> leq(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Optional<Integer> aValue = c(a).getStaticValue();
        Optional<Integer> bValue = c(b).getStaticValue();

        if (aValue.isPresent() && bValue.isPresent()) {
            Boolean result = aValue.get() <= bValue.get();
            return new Repr<>(result::toString, result);
        } else {
            return new Repr<>(() -> "(" + a.raw().getCode() + " <= " + b.raw().getCode() + ")");
        }
    }

    public <A> Of<Repr, A> if_(Of<Repr, Boolean> test, Supplier<Of<Repr, A>> tBranch, Supplier<Of<Repr, A>> fBranch) {
        Optional<Boolean> testValue = c(test).getStaticValue();

        if (testValue.isPresent()) {
            if (testValue.get()) {
                return tBranch.get();
            } else {
                return fBranch.get();
            }
        } else {
            return new Repr<>(() -> test.raw().getCode() + " ? " + tBranch.get().raw().getCode() + " : " + fBranch.get().raw().getCode());
        }
    }
}
