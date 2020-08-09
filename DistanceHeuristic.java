public class DistanceHeuristic implements Heuristic<RoadItem, Double> {
    private RoadItem goal;

    public DistanceHeuristic(RoadItem goal) {
        this.goal = goal;
    }

    public Double apply(GraphNode<RoadItem, Double> node) {
        return this.goal.distanceTo(node.getVal());
    }
}
