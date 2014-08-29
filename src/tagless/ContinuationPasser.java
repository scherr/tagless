package tagless;

import java.util.function.Function;
import java.util.function.Supplier;

public class ContinuationPasser implements Symantics<ContinuationPasser.Repr> {
    static class Repr<T> implements Hi<Repr, T> {
        private Function<Function<Object, ?>, ?> continuationConsumer;

        private Repr(Function<Function<Object, ?>, ?> continuationConsumer) {
            this.continuationConsumer = continuationConsumer;
        }

        public Repr repr() {
            return this;
        }

        Function<Function<Object, ?>, ?> getContinuationConsumer() {
            return continuationConsumer;
        }
        void setContinuationConsumer(Function<Function<Object, ?>, ?> continuationConsumer) {
            this.continuationConsumer = continuationConsumer;
        }

        public T val() {
            throw new UnsupportedOperationException();
        }
    }

    private final boolean callByName;
    private final boolean memoize;

    public ContinuationPasser(boolean callByName, boolean memoize) {
        this.callByName = callByName;
        this.memoize = memoize;
    }

    public Hi<Repr, Integer> int_(int i) {
        return new Repr<>(c -> c.apply(i));
    }

    public Hi<Repr, Boolean> bool_(boolean b) {
        return new Repr<>(c -> c.apply(b));
    }

    public <A, B> Hi<Repr, Function<A, B>> lam(Function<Hi<Repr, A>, Hi<Repr, B>> f) {
        if (callByName) {
            return new Repr<>(c -> c.apply(f));
        } else {
            Function<A, Hi<Repr, B>> callByValueInner = vV -> f.apply(new Repr<>(c -> c.apply(vV)));
            Function<Hi<Repr, A>, Hi<Repr, B>> callByValueOuter = v -> (Hi<Repr, B>) v.repr().getContinuationConsumer().apply(callByValueInner);
            return new Repr<>(c -> c.apply(callByValueOuter));
        }
    }

    public <A, B> Hi<Repr, B> app(Hi<Repr, Function<A, B>> f, Hi<Repr, A> v) {
        if (callByName && memoize) {
            return new Repr<>(c -> {
                Function<Function<Function<Hi<Repr, A>, Hi<Repr, B>>, ?>, ?> fContCons = f.repr().getContinuationConsumer();
                return fContCons.apply(fV -> {
                    f.repr().setContinuationConsumer(lam(fV).repr().getContinuationConsumer());
                    return fV.apply(v).repr().getContinuationConsumer().apply(c);
                });
            });
        } else {
            Function<Function<Function<Hi<Repr, A>, Hi<Repr, B>>, ?>, ?> fContCons = f.repr().getContinuationConsumer();
            return new Repr<>(c -> fContCons.apply(fV -> fV.apply(v).repr().getContinuationConsumer().apply(c)));
        }
    }

    public <A, B> Hi<Repr, Function<A, B>> fix(Function<Hi<Repr, Function<A, B>>, Hi<Repr, Function<A, B>>> f) {
        class Self { Function<Hi<Repr, A>, Hi<Repr, B>> self = x -> app(f.apply(lam(this.self)), x); }
        return lam(new Self().self);
    }

    public Hi<Repr, Integer> add(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                aContCons.apply(aV -> {
                    a.repr().setContinuationConsumer(int_(aV).repr().getContinuationConsumer());
                    Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
                    return bContCons.apply(bV -> {
                        b.repr().setContinuationConsumer(int_(bV).repr().getContinuationConsumer());
                        return c.apply(aV + bV);
                    });
                })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV + bV))));
        }
    }

    public Hi<Repr, Integer> mul(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                    aContCons.apply(aV -> {
                        a.repr().setContinuationConsumer(int_(aV).repr().getContinuationConsumer());
                        Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
                        return bContCons.apply(bV -> {
                            b.repr().setContinuationConsumer(int_(bV).repr().getContinuationConsumer());
                            return c.apply(aV * bV);
                        });
                    })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV * bV))));
        }
    }

    public Hi<Repr, Boolean> leq(Hi<Repr, Integer> a, Hi<Repr, Integer> b) {
        Function<Function<Integer, ?>, ?> aContCons = a.repr().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                    aContCons.apply(aV -> {
                        a.repr().setContinuationConsumer(int_(aV).repr().getContinuationConsumer());
                        Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
                        return bContCons.apply(bV -> {
                            b.repr().setContinuationConsumer(int_(bV).repr().getContinuationConsumer());
                            return c.apply(aV <= bV);
                        });
                    })
            );
        } else {
            Function<Function<Integer, ?>, ?> bContCons = b.repr().getContinuationConsumer();
            return new Repr<>(c -> aContCons.apply(aV -> bContCons.apply(bV -> c.apply(aV <= bV))));
        }
    }

    public <A> Hi<Repr, A> if_(Hi<Repr, Boolean> test, Supplier<Hi<Repr, A>> tBranch, Supplier<Hi<Repr, A>> fBranch) {
        Function<Function<Boolean, Object>, Object> testContCons = test.repr().getContinuationConsumer();
        Function<Function<A, Object>, Object> tBranchContCons = tBranch.get().repr().getContinuationConsumer();
        Function<Function<A, Object>, Object> fBranchContCons = fBranch.get().repr().getContinuationConsumer();
        if (callByName && memoize) {
            return new Repr<>(c ->
                testContCons.apply(testV -> {
                    test.repr().setContinuationConsumer(bool_(testV).repr().getContinuationConsumer());
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
    // when extracted should be of type Function<A, B> but are of type Function<Hi<Repr, A>, Hi<Repr, B>>
    // (cf. lam and app methods).
    public <A> A run(Hi<Repr, A> repr) {
        return (A) repr.repr().getContinuationConsumer().apply((Function<A, Object>) a -> {
            if (a instanceof Function) {
                Hi<Repr, Function<Object, Object>> l = lam((Function<Hi<Repr, Object>, Hi<Repr, Object>>) a);
                return (Function) x -> run(app(l, (new Repr<>(c -> c.apply(x)))));
            } else {
                return a;
            }
        });
    }
}
