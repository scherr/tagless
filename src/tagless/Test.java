package tagless;

import java.util.function.Function;

interface Program<Repr> extends Symantics<Repr> {
    default Of<Repr, Function<Integer, Function<Integer, Integer>>> main() {
        Of<Repr, Integer> test1 =
            app(
                lam(x ->
                    add(x, int_(1))
                ),
                int_(3)
            )
        ;

        Of<Repr, Integer> test2 =
            app(
                app(
                    lam(x ->
                        lam(y -> add(x, y))
                    ),
                    int_(4)
                ),
                int_(6)
            )
        ;

        Of<Repr, Function<Integer, Integer>> fact =
            fix(self ->
                lam(n ->
                    if_(
                        leq(n, int_(0)),
                        () -> int_(1),
                        () -> mul(n, app(self, add(n, int_(-1))))
                    )
                )
            )
        ;

        Of<Repr, Function<Integer, Function<Integer, Integer>>> test3 =
            lam(x ->
                lam(y -> add(x, y))
            )
        ;

        Of<Repr, Function<Integer, Function<Integer, Integer>>> test4 =
            lam(x ->
                lam(y -> mul(add(int_(-4), x), add(x, y)))
            )
        ;

        Of<Repr, Function<Integer, Function<Integer, Integer>>> power =
            fix(self ->
                lam(exp ->
                    lam(base ->
                        if_(
                            leq(exp, int_(0)),
                            () -> int_(1),
                            () -> mul(base, app(app(self, add(exp, int_(-1))), base))
                        )
                    )
                )
            )
        ;

        Of<Repr, Function<Function<Integer, Function<Integer, Integer>>, Function<Integer, Function<Integer, Integer>>>> flip =
            lam(f ->
                lam((Of<Repr, Integer> x) ->
                    lam((Of<Repr, Integer> y) -> app(app(f, y), x))
                )
            )
        ;

        // HiRepr<Repr, Function<Integer, Function<Integer, Integer>>> powerFlipped = app(flip, power);
        Of<Repr, Function<Integer, Function<Integer, Integer>>> powerFlipped =
            fix(self ->
                lam(base ->
                    lam(exp ->
                        if_(
                            leq(exp, int_(0)),
                            () -> int_(1),
                            () -> mul(base, app(app(self, base), add(exp, int_(-1))))
                        )
                    )
                )
            )
        ;


        System.out.println(test1);
        System.out.println(test2);
        System.out.println(test3);
        System.out.println(app(test3, int_(3)));
        System.out.println(app(fact, int_(6)));
        System.out.println(app(power, int_(20)));
        System.out.println(app(app(flip, powerFlipped), int_(3)));
        System.out.println(app(app(app(flip, powerFlipped), int_(3)), int_(900)));
        // System.out.println(app(powerFlipped, int_(10)));
        System.out.println(power);
        System.out.println(powerFlipped);
        System.out.println((lam((Of<Repr, Integer> x) -> app(app(power, int_(2)), add(x, x)))));
        Of<Repr, Function<Integer, Integer>> f = lam(x -> app(lam(y -> mul(y, int_(4))), x));
        System.out.println(f);

        return test3;
    }

    default Of<Repr, Function<Object, Integer>> contTest() {
        Of<Repr, Integer> test1 =
            app(
                lam(x ->
                    add(x, x)
                ),
                app(
                    lam(x -> {
                        System.out.println("Test!");
                        return mul(int_(3), int_(2));
                    }),
                    int_(0)
                )
            )
        ;

        Of<Repr, Function<Integer, Integer>> inner =
            app(
                lam(x -> {
                    System.out.println("Test x -> ...!");
                    return lam(y -> {
                        System.out.println("Test y -> ...!");
                        return int_(2);
                    });
                }),
                int_(0)
            )
        ;

        // It seems blocks in lambda expressions interfere with Java's type inference.
        // The following shows no warnings while the definition above does!
        Of<Repr, Function<Integer, Integer>> innerNoWarning =
            app(
                lam(x ->
                    lam(y -> int_(2))
                ),
                int_(0)
            )
        ;

        Of<Repr, Integer> test2 =
            app(
                lam(f ->
                    add(app(f, int_(0)), app(f, int_(0)))
                ),
                inner
            )
        ;

        // This does not terminate under call-by-value but does under call-by-name.
        Of<Repr, Integer> diverge =
            app(
                lam(x -> int_(1)),
                app(fix(f -> f), int_(2))
            )
        ;

        return lam(x -> diverge);
    }
}

public class Test {
    public static <A, B> Function<A, B> fix(Function<Function<A, B>, Function<A, B>> f) {
        class Self { Function<A, B> self = x -> f.apply(this.self).apply(x); }
        return new Self().self;
    }

    public static void main(String[] args) {
        class EvaluateProgram extends Evaluator implements Program<Evaluator.Repr> {}
        class PrintProgram extends Printer implements Program<Printer.Repr> {}
        class HaskellPrintProgram extends HaskellPrinter implements Program<HaskellPrinter.Repr> {}
        class PartialEvaluatorProgram extends PartialEvaluator implements Program<PartialEvaluator.Repr> {
            PartialEvaluatorProgram() { super(false, 0); }
        }
        class ContinuationPasserProgram extends ContinuationPasser implements Program<ContinuationPasser.Repr> {
            ContinuationPasserProgram() { super(true, true); }
        }

        new EvaluateProgram().main();
        System.out.println();
        new PrintProgram().main();
        System.out.println();
        new HaskellPrintProgram().main();
        System.out.println();
        new PartialEvaluatorProgram().main();
        System.out.println();

        ContinuationPasserProgram p = new ContinuationPasserProgram();
        System.out.println(p.run(p.contTest()).apply(123));

        /*
        System.out.println(new HaskellPrintProgram().main().raw());
        System.out.println(new EvaluateProgram().main().val().apply(2).apply(10));
        */

        /*
        Function<Integer, Function<Integer, Integer>> power = fix(x8 -> (x9 -> (x10 -> (x9 <= 0) ? 1 : (x10 * x8.apply((x9 + -1)).apply(x10)))));
        Function<Integer, Integer> power20 = power.apply(20);
        Function<Integer, Integer> power20Partial = (x3 -> (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * (x3 * x3))))))))))))))))))));

        for (int i = 0; i < 10000000; i++) {
            // power20.apply(3);
            power20Partial.apply(3);
        }

        long start = System.currentTimeMillis();

        for (int i = 0; i < 10000000; i++) {
            // power20.apply(3);
            power20Partial.apply(3);
        }

        System.out.println(System.currentTimeMillis() - start);
        */
    }
}