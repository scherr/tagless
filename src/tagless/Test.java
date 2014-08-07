package tagless;

import java.util.function.Function;

interface Program<Repr> extends Symantics<Repr> {
    default HiRepr<Repr, Function<Integer, Function<Integer, Integer>>> main() {
        HiRepr<Repr, Integer> test1 =
                app(
                    lambda(x ->
                        add(x, int_(1))
                    ),
                    int_(3)
                )
        ;

        HiRepr<Repr, Integer> test2 =
                app(
                    app(
                        lambda(x ->
                            lambda(y -> add(x, y))
                        ),
                        int_(4)
                    ),
                    int_(6)
                )
        ;

        HiRepr<Repr, Function<Integer, Integer>> fact =
                fix(self ->
                    lambda(n ->
                        if_(
                            leq(n, int_(0)),
                            () -> int_(1),
                            () -> mul(n, app(self, add(n, int_(-1))))
                        )
                    )
                )
        ;

        HiRepr<Repr, Function<Integer, Function<Integer, Integer>>> test3 =
                lambda(x ->
                    lambda(y -> add(x, y))
                )
        ;

        System.out.println(test1);
        System.out.println(test2);
        System.out.println(app(fact, int_(6)));

        return test3;
    }
}

public class Test {
    public static void main(String[] args) {
        class EvaluateProgram extends Evaluator implements Program<Val> {}
        class PrintProgram extends Printer implements Program<Pri> {}
        class HaskellPrintProgram extends HaskellPrinter implements Program<Pri> {}

        new EvaluateProgram().main();
        System.out.println();
        new PrintProgram().main();
        System.out.println();
        new HaskellPrintProgram().main();

        System.out.println(new HaskellPrintProgram().main().repr());
        System.out.println(new EvaluateProgram().main().val().apply(2).apply(10));
    }
}