package tagless;

// This serves to express that an implementation associates a type T with a second type U.
// This can be considered similar to an explicit parametric type, e.g. Of<List, Integer> vs. List<Integer>.
// However, that for instance Of<String, Integer> or Of<List<Integer>, Set<List<String>>> is expressible is intentional.
public interface Of<T, U> {
    T raw();
}
