import javafx.util.Pair;

import java.util.*;

public class Graph<T, V> {
    private ArrayList<GraphNode<T, V> > nodes;
    private Numeric<V> numeric;

    public Graph(Numeric<V> numeric) {
        this.nodes = new ArrayList<>();
        this.numeric =  numeric;
    }

    public ArrayList<GraphNode<T, V> > getNodes() {
        return this.nodes;
    }

    public void addNode(String name, T value) {
        this.nodes.add(new GraphNode<>(name, value));
    }

    private GraphNode<T, V> getNode(String name) {
        for(GraphNode<T, V> node: this.nodes) {
            if(node.getName().equals(name)) {
                return node;
            }
        }
        return new GraphNode<>("", null);
    }

    public void addEdge(String from, String to, V weight) {
        GraphNode<T, V> from_node = this.getNode(from);
        if(from_node.getName().equals(from)) {
            GraphNode<T, V> to_node = this.getNode(to);
            if(to_node.getName().equals(to)) {
                from_node.addOutgoing(to_node, weight);
                to_node.addIncoming(from_node, weight);
            }
        }
    }
    
    public void removeNode(String name) {
      GraphNode<T, V> node = this.getNode(name);
      ArrayList<Pair<GraphNode<T, V>, V> > outgoing = node.getOutgoing();
      ArrayList<Pair<GraphNode<T, V>, V> > outgo_list = null;
      ArrayList<Pair<GraphNode<T, V>, V> > incoming = node.getIncoming();
      ArrayList<Pair<GraphNode<T, V>, V> > inco_list = null;
      
      // Remove all outgoing edges from node
      Iterator<Pair<GraphNode<T, V>, V> > iter = outgoing.iterator();
      while(iter.hasNext()) {
        Pair<GraphNode<T, V>, V> outgo_pair = iter.next();
        inco_list = outgo_pair.getKey().getIncoming();
        for(int i = 0; i < inco_list.size(); i++) {
          if(inco_list.get(i).getKey().getName().equals(name)) {
            outgo_pair.getKey().removeIncoming(inco_list.get(i));
          }
        }
      }
      
      // Remove all incoming edges from node
      iter = incoming.iterator();
      while(iter.hasNext()) {
        Pair<GraphNode<T, V>, V> inco_pair = iter.next();
        outgo_list = inco_pair.getKey().getOutgoing();
        for(int i = 0; i < outgo_list.size(); i++) {
          if(outgo_list.get(i).getKey().getName().equals(name)) {
            inco_pair.getKey().removeOutgoing(outgo_list.get(i));
          }
        }
      }
      
      // Remove node from graph
      this.nodes.remove(node);
    }

    public void print() {
        ArrayList<Pair<GraphNode<T, V>, V> > node_list;
        for(GraphNode<T, V> node: this.nodes) {
            node_list = node.getOutgoing();
            System.out.println("Node " + node.getName() + ": " + node.getVal());
            for(Pair<GraphNode<T, V>, V> pair: node_list) {
                System.out.println(node.getName() + " --- " + pair.getValue() + " ---> " + pair.getKey().getName());
            }
        }
    }

    public Pair<ArrayList<GraphNode<T, V> >[], Object[]> dijkstra(String start_node, V min_V, V max_V) {
        ArrayList<GraphNode<T, V> >[] cheapest = new ArrayList[this.nodes.size()];
        int start_node_pos = -1;
        for(int i = 0; i < this.nodes.size(); i++) {
            cheapest[i] = new ArrayList<>();
            if(this.nodes.get(i).getName().equals(start_node)) {
                start_node_pos = i;
            }
        }
        if(start_node_pos == -1) {
            return null;
        }
        cheapest[start_node_pos].add(this.getNode(start_node));

        Object[] cost = new Object[this.nodes.size()];
        for(int i = 0; i < this.nodes.size(); i++){
            cost[i] = max_V;
        }
        cost[start_node_pos] = min_V;

        Set<Integer> rest = new HashSet<>();
        for(int i = 0; i < this.nodes.size(); i++) {
            rest.add(i);
        }

        while(! rest.isEmpty()) {
            Iterator<Integer> it = rest.iterator();
            boolean first = true;
            V min_cost = null;
            int min_pos = -1;
            while(it.hasNext()) {
                int node_pos = it.next();
                if(first) {
                    min_cost = (V) cost[node_pos];
                    min_pos = node_pos;
                    first = false;
                } else if(this.numeric.less_than((V) cost[node_pos], min_cost)) {
                    min_cost = (V) cost[node_pos];
                    min_pos = node_pos;
                }
            }
            rest.remove(min_pos);
            ArrayList<Pair<GraphNode<T, V>, V> > outgoing = this.nodes.get(min_pos).getOutgoing();
            for(Pair<GraphNode<T, V>, V> pair: outgoing) {
                V c = this.numeric.add(((V) cost[min_pos]),pair.getValue());
                if(this.numeric.less_than(c, (V) cost[this.nodes.indexOf(pair.getKey())])) {
                    cheapest[this.nodes.indexOf(pair.getKey())] = (ArrayList<GraphNode<T, V> >) cheapest[min_pos].clone();
                    cheapest[this.nodes.indexOf(pair.getKey())].add(pair.getKey());
                    cost[this.nodes.indexOf(pair.getKey())] = c;
                }
            }
        }

        return new Pair<>(cheapest, cost);
    }

    private ArrayList<GraphNode<T, V> > reconstructPath(Integer[] cameFrom, int curr) {
        ArrayList<GraphNode<T, V> > total_path = new ArrayList<>();
        total_path.add(this.nodes.get(curr));
        int current = curr;
        while(cameFrom[current] != -1){
            current = cameFrom[current];
            total_path.add(this.nodes.get(current));
        }
        Collections.reverse(total_path);
        return total_path;
    }

    public Pair<ArrayList<GraphNode<T, V> >, V> aStar(String start_node_str, String goal_node_str, V max_V, Heuristic<T, V> h){
        int start_node_pos = -1;
        GraphNode<T, V> start_node;
        int goal_node_pos = -1;
        GraphNode<T, V> end_node;

        Set<Integer> openSet = new HashSet<>();

        Integer[] cameFrom = new Integer[this.nodes.size()];

        Object[] gScore = new Object[this.nodes.size()];

        Object[] fScore = new Object[this.nodes.size()];

        // Set values for the above
        for(int i = 0; i < this.nodes.size(); i++) {
            if(this.nodes.get(i).getName().equals(start_node_str)) {
                start_node_pos = i;
            } else if(this.nodes.get(i).getName().equals(goal_node_str)) {
                goal_node_pos = i;
            }
            gScore[i] = max_V;
            fScore[i] = max_V;
            cameFrom[i] = -1;
        }
        if(start_node_pos == -1 || goal_node_pos == -1) {
            return null;
        }
        gScore[start_node_pos] = 0.0;
        fScore[start_node_pos] = h.apply(this.nodes.get(start_node_pos));
        openSet.add(start_node_pos);

        // Main loop
        while(! openSet.isEmpty()) {
            boolean first = true;
            V lowest_fscore = null;
            int current = -1;
            for(Integer pos: openSet) {
                if(first) {
                    lowest_fscore = (V) fScore[pos];
                    current = pos;
                    first = false;
                } else if(this.numeric.less_than((V) fScore[pos], lowest_fscore)) {
                    lowest_fscore = (V) fScore[pos];
                    current = pos;
                }
            }
            if(current == goal_node_pos) {
                ArrayList<GraphNode<T, V> > return_path = this.reconstructPath(cameFrom, current);
                Pair<ArrayList<GraphNode<T, V> >, V> return_val = new Pair<>(return_path, (V) fScore[current]);
                return return_val;
            }

            openSet.remove(current);
            for(Pair<GraphNode<T, V>, V> pair: this.nodes.get(current).getOutgoing()) {
                int neighbor = this.nodes.indexOf(pair.getKey());
                V tentative_gScore = (V) this.numeric.add((V) gScore[current], pair.getValue());
                if(this.numeric.less_than(tentative_gScore, (V) gScore[neighbor])){
                    cameFrom[neighbor] = current;
                    gScore[neighbor] = tentative_gScore;
                    fScore[neighbor] = this.numeric.add((V) gScore[neighbor], h.apply(pair.getKey()));
                    if(! openSet.contains(neighbor)) {
                        openSet.add(neighbor);
                    }
                }
            }
        }
        return null;
    }
}
