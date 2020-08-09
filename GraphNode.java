import java.util.ArrayList;
import javafx.util.Pair;

public class GraphNode<T, V> {
    private String name;
    private T value;
    private ArrayList<Pair<GraphNode<T, V>, V> > outgoing_adjacency_list = new ArrayList<>();;
    private ArrayList<Pair<GraphNode<T, V>, V> > incoming_adjacency_list = new ArrayList<>();

    public GraphNode(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public T getVal() {
        return this.value;
    }

    public ArrayList<Pair<GraphNode<T, V>, V> > getOutgoing() {
        return this.outgoing_adjacency_list;
    }

    public ArrayList<Pair<GraphNode<T, V>, V> > getIncoming() {
        return this.incoming_adjacency_list;
    }

    public void addOutgoing(GraphNode<T, V> to_node, V weight) {
        Pair<GraphNode<T, V>, V> outgoing = new Pair<>(to_node, weight);
        this.outgoing_adjacency_list.add(outgoing);
    }

    public void addIncoming(GraphNode<T, V> from_node, V weight) {
        Pair<GraphNode<T, V>, V> incoming = new Pair<>(from_node, weight);
        this.incoming_adjacency_list.add(incoming);
    }
    
    public void removeOutgoing(Pair<GraphNode<T, V>, V> to_pair){
        this.outgoing_adjacency_list.remove(to_pair);
    }
    
    public void removeIncoming(Pair<GraphNode<T, V>, V> from_pair){
        this.incoming_adjacency_list.remove(from_pair);
    }
}
