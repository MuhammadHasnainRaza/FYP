public abstract class RoadItem {
    protected double[] position = {0.0 ,0.0};
    protected double orientation;
    protected double roadWidth;

    public RoadItem(double[] position, double orientation, double roadWidth) {
        this.position[0] = position[0];
        this.position[1] = position[1];
        this.orientation = orientation;
        this.roadWidth = roadWidth;
    }
    
    public RoadItem(RoadItem road_item) {
      this.position[0] = road_item.position[0];
      this.position[1] = road_item.position[1];
      this.orientation = road_item.orientation;
      this.roadWidth = road_item.roadWidth;
    }

    public double getX() {
        return this.position[0];
    }

    public double getY() {
        return this.position[1];
    }

    public double getOrientation() {
        return this.orientation;
    }
    
    public double getRoadWidth() {
      return this.roadWidth;
    }
    
    public String toString() {
      String return_str = "";
      return_str += "(" + this.position[0] + ", " + this.position[1] + ", " + this.orientation + ")";
      return_str += " - Road Width: " + this.roadWidth;
      return return_str;
    }
    
    public double distanceTo(RoadItem ri2) {
      double delta_x = this.position[0] - ri2.position[0];
      double delta_y = this.position[1] - ri2.position[1];
      return Math.sqrt(delta_x*delta_x + delta_y*delta_y);
    }
    
    public boolean equals(RoadItem item) {
      if(this.position[0] == item.position[0] && this.position[1] == item.position[1] && this.orientation == item.orientation && this.roadWidth == item.roadWidth) {
        return true;
      }
      return false;
    }

    public abstract boolean isPartOf(double[] position);
}