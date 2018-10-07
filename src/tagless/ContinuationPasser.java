package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContinuationPasser implements Symantics<ContinuationPasser.Repr> {
    static class Repr<T> implements Of<Repr, T> {
        private Function<Function<Object, ?>, ?> continuationConsumer;

        private Repr(Function<Function<Object, ?>, ?> continuationConsumer) {
            this.continuationConsumer = continuationConsumer;
        }

        public Repr raw() {
            return this;
        }

        Function<Function<Object, ?>, ?> getContinuationConsumer() {
            return continuationConsumer;
        }
        void setContinuationConsumer(Function<Function<Object, ?>, ?> continuationConsumer) {
            this.continuationConsumer = continuationConsumer;
        }
    }

    private final boolean callByName;
    private final boolean memoize;

    public ContinuationPasser(boolean callByName, boolean memoize) {
        this.callByName = callByName;
        this.memoize = memoize;
    }

    public Of<Repr, Integer> int_(int i) {
        return new Repr<>(c -> c.apply(i));
    }

    public Of<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(c -> c.apply(b));
    }

    public <A, B> Of<Repr, Function<A, B>> lam(Function<Of<Repr, A>, Of<Repr, B>> f) {
        if (callByName) {
            return new Repr<>(c -> c.apply(f));
        } else {
            Function<A, Of<Repr, B>> callByValueInner = vV -> f.apply(new Repr<>(c -> c.apply(vV)));
            Function<Of<Repr, A>, Of<Repr, B>> callByValueOuter = v -> (Of<Repr, B>) v.raw().getContinuationConsumer().apply(callByValueInner);
            return new Repr<>(c -> c.apply(callByValueOuter));
        }
    }

    public <A, B> Of<Repr, B> app(Of<Repr, Function<A, B>> f, Of<Repr, A> v) {
        if (callByName && memoize) {
            return new Repr<>(c -> {
                Function<Function<Function<Of<Repr, A>, Of<Repr, B>>, ?>, ?> fContCons = f.raw().getContinuationConsumer();
                return fContCons.apply(fV -> {
                    f.raw().setContinuationConsumer(lam(fV).raw().getContinuationConsumer());
                    return fV.apply(v).raw().getContinuationConsumer().apply(c);
                });
            });
        } else {
            Function<Function<Function<Of<Repr, A>, Of<Repr, B>>, ?>, ?> fContCons = f.raw().getContinuationConsumer();
            return new Repr<>(c -> fContCons.apply(fV -> fV.apply(v).raw().getContinuationConsumer().apply(c)));
        }
    }

    public <A, B> Of<Repr, Function<A, B>> fix(Function<Of<Repr, Function<A, B>>, Of<Repr, Function<A, B>>> f) {
        class Self { Function<Of<Repr, A>, Of<Repr, B>> self = x -> app(f.apply(lam(this.self)), x); }
        return lam(new Self().self);
    }

    public Of<Repr, Integer> add(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.raw().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                aContCons.apply(aV -> {
                    a.raw().setContinuationConsumer(int_(aV).raw().getContinuationConsumer());
                    Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
                    return bContCons.apply(bV -> {
                        b.raw().setContinuationConsumer(int_(bV).raw().getContinuationConsumer());
                        return c.apply(aV + bV);
                    });
                })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV + bV))));
        }
    }

    public Of<Repr, Integer> mul(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.raw().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                    aContCons.apply(aV -> {
                        a.raw().setContinuationConsumer(int_(aV).raw().getContinuationConsumer());
                        Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
                        return bContCons.apply(bV -> {
                            b.raw().setContinuationConsumer(int_(bV).raw().getContinuationConsumer());
                            return c.apply(aV * bV);
                        });
                    })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV * bV))));
        }
    }

    public Of<Repr, Boolean> leq(Of<Repr, Integer> a, Of<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.raw().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                    aContCons.apply(aV -> {
                        a.raw().setContinuationConsumer(int_(aV).raw().getContinuationConsumer());
                        Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
                        return bContCons.apply(bV -> {
                            b.raw().setContinuationConsumer(int_(bV).raw().getContinuationConsumer());
                            return c.apply(aV <= bV);
                        });
                    })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.raw().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV <= bV))));
        }
    }

    public <A> Of<Repr, A> if_(Of<Repr, Boolean> test, Supplier<Of<Repr, A>> tBranch, Supplier<Of<Repr, A>> fBranch) {
        Function<Function<Boolean, Object>, Object> testContCons = test.raw().getContinuationConsumer();
        Function<Function<A, Object>, Object> tBranchContCons = tBranch.get().raw().getContinuationConsumer();
        Function<Function<A, Object>, Object> fBranchContCons = fBranch.get().raw().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                testContCons.apply(testV -> {
                    test.raw().setContinuationConsumer(bool_(testV).raw().getContinuationConsumer());
                    if (testV) {
                        return tBranchContCons.apply((Function<A, Object>) c);
                    } else {
                        return fBranchContCons.apply((Function<A, Object>) c);
                    }
                })
            );
        } else {
            return new Repr<>(c -> testContCons.apply(testV -> testV ? tBranchContCons.apply((Function<A, Object>) c) : fBranchContCons.apply((Function<A, Object>) c)));
        }
    }

    // This is pretty ugly but otherwise we cannot "run", i.e. extract / unlift lambda abstractions which
    // when extracted should be of type Function<A, B> but are of type Function<Of<Repr, A>, Of<Repr, B>>
    // (cf. lam and app methods).
    public <A> A run(Of<Repr, A> repr) {
        return (A) repr.raw().getContinuationConsumer().apply((Function<A, Object>) a -> {
            if (a instanceof Function) {
                Of<Repr, Function<Object, Object>> l = lam((Function<Of<Repr, Object>, Of<Repr, Object>>) a);
                return (Function) x -> run(app(l, (new Repr<>(c -> c.apply(x)))));
            } else {
                return a;
            }
        });
    }
}
