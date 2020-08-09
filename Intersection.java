import java.lang.Math;

public class Intersection extends RoadItem {

    public Intersection(double[] position, double orientation, double roadWidth) {
        super(position, orientation, roadWidth);
    }
    
    public Intersection(Intersection intersection) {
      super(intersection);
    }

    public boolean isPartOf(double[] position) {
        double pos_delta[] = {position[0] - this.position[0], position[1] - this.position[1]};
        return Math.sqrt((pos_delta[0]*pos_delta[0]) + (pos_delta[1]*pos_delta[1])) <= (this.roadWidth + 0.1);
    }
}
