import java.util.Comparator;

public class RoadItemComparator implements Comparator<RoadItem> {
    private Road road;

    public RoadItemComparator(Road road){
        this.road = road;
    }

    public int compare(RoadItem r1, RoadItem r2) {
        double delta_x_1 = this.road.getX() - r1.getX();
        double delta_y_1 = this.road.getY() - r1.getY();
        double y_final_1 = Math.sin(-this.road.getOrientation())*delta_x_1 + Math.cos(-this.road.getOrientation())*delta_y_1;
        double delta_x_2 = this.road.getX() - r2.getX();
        double delta_y_2 = this.road.getY() - r2.getY();
        double y_final_2 = Math.sin(-this.road.getOrientation())*delta_x_2 + Math.cos(-this.road.getOrientation())*delta_y_2;
        return ((Double) Math.abs(y_final_1)).compareTo((Double) Math.abs(y_final_2));
    }
}
