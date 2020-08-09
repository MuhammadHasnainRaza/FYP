public interface Heuristic<T, V> {
    public V apply(GraphNode<T, V> node);
}
