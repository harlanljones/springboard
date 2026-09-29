package ai.primeiq.springboard.items;

/**
 * One example resource. Records everywhere: the API contract is a value, not a mutated bean.
 * Adding a field here is the canonical way to extend the example slice.
 */
public record Item(long id, String name, String status) {
}
