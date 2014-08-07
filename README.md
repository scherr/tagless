Tagless
===

An attempt to implement parts of the paper "Finally Tagless Partially Evaluated" by Carette et al.
in Java. Java 8 brings lambda expression syntax and default methods, but lacks higher kinded types. We have to emulate them with a wrapper interface.

Furthermore, Java does not support multi-staged programming (MSP), so we will have to improvise... (WIP)

Example
---

Programs can be expressed like this:
```Java
interface Program<Repr> extends Symantics<Repr> {
    default void main() {
        // ((λx0.(x0 + (1)))(3))
        HiRepr<Repr, Integer> test1 =
                app(
                    lambda(x ->
                        add(x, int_(1))
                    ),
                    int_(3)
                )
        ;

        // (((λx1.(λx2.(x1 + x2)))(4))(6))
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

        // ([θ λx3.(λx4.(if (x4 <= (0)) then {(1)} else {(x4 * (x3(x4 + (-1))))}))](6))
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
Tagless interpretations are then performed as follows:

```Java
class EvaluateProgram extends Evaluator implements Program<Val> {}
new EvaluateProgram().main();
```
Output:
```
4
10
720
```

```Java
class PrintProgram extends Printer implements Program<Pri> {}
new PrintProgram().main();
```
Output:
```
((λx0.(x0 + (1)))(3))
(((λx1.(λx2.(x1 + x2)))(4))(6))
([θ λx3.(λx4.(if (x4 <= (0)) then {(1)} else {(x4 * (x3(x4 + (-1))))}))](6))
```

```Java
class HaskellPrintProgram extends HaskellPrinter implements Program<Pri> {}
new HaskellPrintProgram().main();
```
Output:
```
((\x0 -> (x0 + (1)))((3)))
(((\x1 -> (\x2 -> (x1 + x2)))((4)))((6)))
((fix (\x3 -> (\x4 -> (if (x4 <= (0)) then (1) else (x4 * (x3((x4 + (-1)))))))))((6)))
```

References
---

* The reference paper with Haskell and OCaml implementations: http://www.cs.rutgers.edu/~ccshan/tagless/aplas.pdf
* An implementation in Scala: https://github.com/vjovanov/finally-tagless
