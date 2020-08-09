import java.lang.Math;

public class Road extends RoadItem {

    private double length;

    public Road(double[] position, double orientation, double roadWidth, double length) {
        super(position, orientation, roadWidth);
        this.length = length;
    }
    
    public double getLength() {
      return this.length;
    }

    public boolean isPartOf(double[] position) {
        double delta_x = this.position[0] - position[0];
        double delta_y = this.position[1] - position[1];
        double x_final = Math.cos(this.orientation)*delta_x - Math.sin(this.orientation)*delta_y;
        double y_final = Math.sin(this.orientation)*delta_x + Math.cos(this.orientation)*delta_y;
        return x_final <= this.length + 0.01 && y_final >= -this.roadWidth/2.0 - 0.01 && y_final <= this.roadWidth/2.0 + 0.01;
    }
    
    public String toString() {
      String return_str = super.toString() + " - Length: " + this.length;
      return return_str;
    }
}
