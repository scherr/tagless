Tagless
===

An attempt to implement parts of the paper "Finally Tagless Partially Evaluated" by Carette et al.
in Java. Java 8 brings lambda expression syntax and default methods, but lacks higher kinded types. We have to emulate them with a wrapper interface.

Furthermore, Java does not support staging, so we will have to improvise...

Example
---

Programs can be expressed like this:
```Java
interface Program<Repr> extends Symantics<Repr> {
    default void main() {
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
                int_(6))
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

        System.out.println(test1);
        System.out.println(test2);
        System.out.println(app(fact, int_(6)));
    }
}
```
Tagless interpretation of these is then performed as follows:

```Java
class EvaluateProgram extends Evaluator implements Program<Val> {}
new EvaluateProgram().main();

class PrintProgram extends Printer implements Program<Pri> {}
new PrintProgram().main();

class HaskellPrintProgram extends HaskellPrinter implements Program<Pri> {}
new HaskellPrintProgram().main();
```

References
---

* https://github.com/vjovanov/finally-tagless
* http://www.cs.rutgers.edu/~ccshan/tagless/aplas.pdf
