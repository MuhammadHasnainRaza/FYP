import java.lang.Math; 
import java.util.ArrayList;
import javafx.util.Pair; 
import java.util.Collections;
import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.Supervisor;
import com.cyberbotics.webots.controller.Node;
import com.cyberbotics.webots.controller.Field;
import com.cyberbotics.webots.controller.Receiver;
import com.cyberbotics.webots.controller.Emitter;
import com.cyberbotics.webots.controller.Keyboard;

public class bin_controller {

  public static void main(String[] args) {
    // Create the Robot instance.
    Supervisor robot = new Supervisor();
    
    // Hide shape
    Field this_children = robot.getSelf().getField("children");
    Field transparency = null;
    int child_count = this_children.getCount();
    for(int i = 0; i < child_count; i++){
      if(this_children.getMFNode(i).getType() == Node.SHAPE) {
        Field app = this_children.getMFNode(i).getField("appearance");
        transparency = app.getSFNode().getField("material").getSFNode().getField("transparency");
        break;
      }
    }
    transparency.setSFFloat(1.0);
    
    // Create an empty Graph
    Numeric<Double> numeric = new DoubleNumeric();
    Graph<RoadItem, Double> graph = new Graph<RoadItem, Double>(numeric);
    
    // Get number of nodes in Scene Tree
    Node root = robot.getRoot();
    Node child;
    Field children = root.getField("children");
    int nr_children = children.getCount();
    
    // Initialize variables
    int intersection_id = 0;
    int road_id = 0;
    ArrayList<Road> roads = new ArrayList<>();
    ArrayList<RoadItem> bins = new ArrayList<>();
    double[] position_tmp = new double[2];
    double roadsWidth;
    ArrayList<Pair<Road, Intersection> > bins_on_road = new ArrayList<>();
    RoadItem this_bin_inter = null;
    String self_str = "";
    
    // Iterate over all nodes in scene tree
    for(int i = 0; i < nr_children; i++) {
      child = children.getMFNode(i);
      
      // Only consider nodes that are either roads or intersections
      if(child.getType() == Node.SOLID || child.getType() == Node.ROBOT) {
         // Get position and orientation of node
        double[] position = child.getPosition();
        double x = position[0];
        double z = position[2];
        position_tmp[0] = x;
        position_tmp[1] = z;
        double[] orientation = child.getOrientation();
        double angle = Math.atan2(orientation[2],orientation[0]);
        
        // If the node is a road or road intersection
        if(child.getField("name").getSFString().contains("road")) {
          // If node is a road intersection, add it to the graph
          // If it is a road, add it to the list of roads to use them later to connect intersections in graph
          if(child.getField("name").getSFString().contains("road intersection")) {
            RoadItem item = new Intersection(position_tmp, angle, child.getField("roadsWidth").getSFFloat());
            graph.addNode("inter_" + intersection_id, item);
            intersection_id++;
          } else {
            double length = child.getField("length").getSFFloat();
            Road road = new Road(position_tmp, angle, child.getField("width").getSFFloat(), length);
            roads.add(road);
          }
        } else if(child.getField("name").getSFString().contains("robot")) {
          // If the node is a robot (a dustbin)
          RoadItem intersection = new Intersection(position_tmp, 0.0, 0.0);
          bins.add(intersection);
          if(child.getField("name").getSFString().equals(robot.getSelf().getField("name").getSFString())) {
            this_bin_inter = intersection;
          }
        }
      }
    }
    
    // Iterate over all bins found
    for(RoadItem bin: bins) {
      // Get closest road (smallest perpendicular distance to a road)
      double delta_x = 0.0;
      double delta_y = 0.0;
      double x_final = 0.0;
      double y_final = 0.0;
      double min_x = 0.0;
      Road closest_road = null;
      boolean first = true;
      double pt_x = 0.0;
      double pt_y = 0.0;
      for(Road road: roads) {
        delta_x = road.getX() - bin.getX();
        delta_y = road.getY() - bin.getY();
        x_final = Math.cos(-road.getOrientation())*delta_x - Math.sin(-road.getOrientation())*delta_y;
        y_final = Math.sin(-road.getOrientation())*delta_x + Math.cos(-road.getOrientation())*delta_y;
        if(y_final - road.getRoadWidth() <= road.getLength() && Math.abs(y_final) <= road.getLength() + road.getRoadWidth()) {
            if(first) {
                min_x = Math.abs(x_final);
                closest_road = road;
                // Get point at which the perpendicular distance is smaller
                pt_x = road.getX() + Math.sin(road.getOrientation())*y_final;
                pt_y = road.getY() - Math.cos(road.getOrientation())*y_final;
                first = false;
            } else if(Math.abs(x_final) < min_x) {
                min_x = Math.abs(x_final);
                closest_road = road;
                // Get point at which the perpendicular distance is smaller
                pt_x = road.getX() + Math.sin(road.getOrientation())*y_final;
                pt_y = road.getY() - Math.cos(road.getOrientation())*y_final;
            }
        }
      }
      
      // Create new intersection at perpendicular point
      position_tmp[0] = pt_x;
      position_tmp[1] = pt_y;
      Intersection inter = new Intersection(position_tmp, 0.0, 0.0);
      if(this_bin_inter.equals(bin)) {
        this_bin_inter = inter;
      }
      
      // Add pair of point and road to put in-between afterward
      Pair<Road, Intersection> pair = new Pair<>(closest_road, inter);
      bins_on_road.add(pair);
    }
    
    // Iterate over all roads found
    for(Road road: roads) {
      // Get end position of the road
      double x_2 = road.getX() + road.getLength()*Math.sin(road.getOrientation());
      double z_2 = road.getY() + road.getLength()*Math.cos(road.getOrientation());
      position_tmp[0] = road.getX();
      position_tmp[1] = road.getY();
      
      // Create Intersection instance at beginning of road (width = 0 for a single point)
      RoadItem inter_2 = new Intersection(position_tmp, road.getOrientation(), 0);
      RoadItem prev_inter = inter_2;
      boolean check = false;
      String equal_node_2 = "road_start_" + road_id;
      
      // Check that this point does not correspond to an intersection already in the graph
      // If it doesn't, add it as a new node to the graph
      // If it does, connect the road to the intersection already in the graph
      ArrayList<GraphNode<RoadItem, Double> > nodes = graph.getNodes();
      for(GraphNode<RoadItem, Double> node: nodes) {
        if(node.getVal().isPartOf(position_tmp)) {
          check = true;
          equal_node_2 = node.getName();
          prev_inter = node.getVal();
          break;
        }
      }
      if(!check) {
        graph.addNode(equal_node_2, inter_2);  
      }
      
      // Do the same as above for the end of the road
      check = false;
      String equal_node_1 = "road_end_" + road_id;
      position_tmp[0] = x_2;
      position_tmp[1] = z_2;
      RoadItem inter_1 = new Intersection(position_tmp, road.getOrientation(), 0);
      RoadItem end_node = inter_1;
      for(GraphNode<RoadItem, Double> node: nodes) {
        if(node.getVal().isPartOf(position_tmp)) {
          check = true;
          equal_node_1 = node.getName();
          end_node = node.getVal();
          break;
        }
      }
      if(!check) {
        graph.addNode(equal_node_1, inter_1);  
      }
      
      // Check if any of the bins is in this road
      ArrayList<Intersection> bins_on_road_sort = new ArrayList<>();
      for(Pair<Road, Intersection> pair: bins_on_road) {
        if(pair.getKey() == road) {
          bins_on_road_sort.add(pair.getValue());
        }
      }
      
      // Sort bins by proximity to start of road
      RoadItemComparator comparator = new RoadItemComparator(road);
      Collections.sort(bins_on_road_sort, comparator);
      
      // Iterate over bins by adding them and the connections to the graph
      String prev_id = equal_node_2;
      int bin_id = 0;
      for(Intersection bin_intersection: bins_on_road_sort) {
        String insert_id = "road_" + road_id + "_bin_" + bin_id;
        graph.addNode(insert_id, bin_intersection);
        graph.addEdge(prev_id, insert_id, prev_inter.distanceTo(bin_intersection));
        graph.addEdge(insert_id, prev_id, prev_inter.distanceTo(bin_intersection));
        prev_id = insert_id;
        prev_inter = bin_intersection;
        bin_id++;
        if(this_bin_inter.equals(bin_intersection)) {
          self_str = insert_id;
        }
      }
      
      // Add edge to end of road
      graph.addEdge(prev_id, equal_node_1, prev_inter.distanceTo(end_node));
      graph.addEdge(equal_node_1, prev_id, prev_inter.distanceTo(end_node));
      road_id++;
    }
    
    // Set variables for states and matching messages
    final int STATE_IDLE = 0;
    final int STATE_RECEIVE_POSITIONS = 1;
    final int STATE_ASTAR = 2;
    final int STATE_WAIT_TRUCK = 3;
    int current_state = STATE_IDLE;
    MessageMatcher msg_matcher = new MessageMatcher();
    
    // Get the time step of the current world
    int timeStep = (int) Math.round(robot.getBasicTimeStep());
    
    // Get receiver channel and enable receiver
    int receiver_channel = Integer.parseInt(args[0]);
    Receiver receiver = robot.getReceiver("receiver");
    receiver.setChannel(receiver_channel);
    receiver.enable(timeStep);
    
    // Retrieve emmitter and set to broadcast channel
    Emitter emitter = robot.getEmitter("emitter");
    emitter.setChannel(Emitter.CHANNEL_BROADCAST);
    
    // Initialize list of registered trucks
    ArrayList<Pair<String, Integer> > registered_trucks = new ArrayList<>();
    int truck_count = 0;
    
    // Initialize DustBin object
    DustBin dustbin = new DustBin();
    
    // Initialize keyboard to listen to key presses
    Keyboard kboard = new Keyboard();
    kboard.enable(timeStep);
    
    ArrayList<Pair<String, Intersection> > checked_truck_pairs = new ArrayList<>();
    ArrayList<String> busy_trucks = new ArrayList<>();
    int checked_trucks = 0;
    String helping_truck = "";
    
    // Main loop
    while (robot.step(timeStep) != -1) {
      switch(current_state) {
          case STATE_IDLE: // DONE
            transparency.setSFFloat(1.0);
            // Handle incoming messages
            if(receiver.getQueueLength() > 0) {
              String message = new String(receiver.getData());
              int message_type = msg_matcher.setMessage(message);
              if(message_type == MessageMatcher.REGISTER) {
                // Check if this truck has been registered before
                // If so, unregister it and register it again (maybe a new channel)
                Pair<String, Integer> truck = dustbin.register_args(message);
                for(Pair<String, Integer> truck_reg: registered_trucks) {
                  if(truck_reg.getKey() == truck.getKey()) {
                    registered_trucks.remove(truck_reg);
                    truck_count--;
                    break;
                  }
                }
                registered_trucks.add(truck);
                System.out.println("Bin " + robot.getName() + ": registered " + truck.getKey() + " at channel " + truck.getValue());
                truck_count++;
              }
              receiver.nextPacket();
            }
            
            // Listen to key press events
            int key = kboard.getKey();
            if(key >= 0) {
              if(key == Integer.toString(receiver_channel).charAt(0)) {
                String out_msg = "give_positions(" + receiver_channel + ")";
                emitter.send(out_msg.getBytes());
                current_state = STATE_RECEIVE_POSITIONS;
                checked_truck_pairs = new ArrayList<>();
                busy_trucks = new ArrayList<>();
                checked_trucks = 0;
                helping_truck = "";
                System.out.println("Bin " + robot.getName() + ": received key press");
              }
            }
            break;
          case STATE_RECEIVE_POSITIONS:
            transparency.setSFFloat(0.0);
            // Handle incoming messages
            if(receiver.getQueueLength() > 0) {
              String message = new String(receiver.getData());
              int message_type = msg_matcher.setMessage(message);
              if(message_type == MessageMatcher.MY_POSITION) {
                // Get this truck's positions as an intersection
                Pair<String, Intersection> truck_pair = dustbin.my_position_args(message);
                System.out.println("Bin " + robot.getName() + ": received truck " + truck_pair.getKey()  + "'s position");
                boolean truck_is_there = false;
                for(Pair<String, Intersection> pair_t: checked_truck_pairs) {
                  if(truck_pair.getKey() == pair_t.getKey()) {
                    truck_is_there = true;
                  }
                }
                if(!truck_is_there) {
                  checked_truck_pairs.add(truck_pair);
                  checked_trucks++;
                }
                
                // Add truck to graph
                graph.addNode(truck_pair.getKey(), truck_pair.getValue());
                
                // Find closest intersection in front of truck
                boolean first = true;
                String closest_inter = "";
                double closest_dist = 0.0;
                ArrayList<GraphNode<RoadItem, Double> > graph_nodes = graph.getNodes();
                for(GraphNode<RoadItem, Double> graph_node: graph_nodes){
                  double delta_x = graph_node.getVal().getX() - truck_pair.getValue().getX();
                  double delta_y = graph_node.getVal().getY() - truck_pair.getValue().getY();
                  double right_pos = - Math.cos(truck_pair.getValue().getOrientation())*delta_x + Math.sin(truck_pair.getValue().getOrientation())*delta_y;
                  double front_pos = Math.sin(truck_pair.getValue().getOrientation())*delta_x + Math.cos(truck_pair.getValue().getOrientation())*delta_y;
                  if(front_pos > 0 && Math.atan(Math.abs(right_pos)/Math.abs(front_pos)) <= Math.PI/6.0) {
                    double dist = truck_pair.getValue().distanceTo(graph_node.getVal());
                    if(first) {
                      closest_inter = graph_node.getName();
                      closest_dist = dist;
                      first = false;
                    } else if(dist < closest_dist) {
                      closest_inter = graph_node.getName();
                      closest_dist = dist;
                    }
                  }
                }
                
                // Add edge from truck to that intersection (one-way edge)
                graph.addEdge(truck_pair.getKey(), closest_inter, closest_dist);
              } else if(message_type == MessageMatcher.BUSY) {
                // Set truck as busy
                String truck_id = dustbin.busy_args(message);
                System.out.println("Bin " + robot.getName() + ": truck " + truck_id  + " is busy");
                boolean truck_is_there = false;
                for(String truck_str: busy_trucks) {
                  if(truck_str.equals(truck_id)) {
                    truck_is_there = true;
                  }
                }
                if(!truck_is_there) {
                  busy_trucks.add(truck_id);
                  checked_trucks++;
                }
              }
              receiver.nextPacket();
              
              // Check if all trucks have been accounted for, if so,
              // move to STATE_ASTAR
              if(checked_trucks == truck_count) {
                current_state = STATE_ASTAR;
              }
            }
            break;
          case STATE_ASTAR:
            // Calculate shortest paths and select shortest
            System.out.println("Bin " + robot.getName() + ": calculating shortest paths");
            DistanceHeuristic h = new DistanceHeuristic(this_bin_inter);
            double min_cost = 0;
            boolean first = true;
            String closest_truck = "";
            ArrayList<GraphNode<RoadItem, Double> > curr_path = null;
            for(Pair<String, Intersection> checked_truck_pair: checked_truck_pairs){
              Pair<ArrayList<GraphNode<RoadItem, Double> >, Double> results = graph.aStar(checked_truck_pair.getKey(), self_str, Double.POSITIVE_INFINITY, h);
              if(first){
                curr_path = results.getKey();
                min_cost = results.getValue();
                closest_truck = checked_truck_pair.getKey();
                first = false;
              } else if(results.getValue() < min_cost) {
                curr_path = results.getKey();
                min_cost = results.getValue();
                closest_truck = checked_truck_pair.getKey();
              }
            }
            System.out.println("Bin " + robot.getName() + ": shortest path has length " + min_cost);
            
            // Make path into a message
            String out_msg = "help_me(";
            int curr_path_size = curr_path.size();
            int i = 0;
            for(GraphNode<RoadItem, Double> graph_node: curr_path) {
              out_msg = out_msg + "(" + graph_node.getVal().getX() + "," + graph_node.getVal().getY() + ")";
              i++;
              if(i != curr_path_size){
                out_msg = out_msg + ",";
              }
            }
            out_msg = out_msg + ")";
            
            // Find truck's channel from registration list
            int truck_channel = -1;
            for(Pair<String, Integer> reg_truck: registered_trucks) {
              if(reg_truck.getKey().equals(closest_truck)){
                truck_channel = reg_truck.getValue();
              } else {
                // Send no help messages to other trucks to free them
                emitter.setChannel(reg_truck.getValue());
                String no_help_msg = "no_help";
                emitter.send(no_help_msg.getBytes());
              }
            }
            
            // Switch to truck's channel and send path to truck
            helping_truck = closest_truck;
            System.out.println("Bin " + robot.getName() + ": setting channel " + truck_channel);
            emitter.setChannel(truck_channel);
            emitter.send(out_msg.getBytes());
            System.out.println("Bin " + robot.getName() + ": called truck " + closest_truck + " to help");
            
            // Move to STATE_WAIT_TRUCK
            current_state = STATE_WAIT_TRUCK;
            break;
          case STATE_WAIT_TRUCK:
            // Handle incoming messages
            if(receiver.getQueueLength() > 0) {
              String message = new String(receiver.getData());
              int message_type = msg_matcher.setMessage(message);
              if(message_type == MessageMatcher.GOING) {
                // If the truck ID in the message matches the one the path was
                // sent to, remove all trucks from graph and move to STATE_IDLE
                String truck_id = dustbin.going_args(message);
                if(truck_id.equals(helping_truck)) {
                  for(Pair<String, Intersection> pair_truck: checked_truck_pairs) {
                    graph.removeNode(pair_truck.getKey());
                  }
                  System.out.println("Bin " + robot.getName() + ": truck is on its way");
                  current_state = STATE_IDLE;
                  emitter.setChannel(Emitter.CHANNEL_BROADCAST);
                }
              }
              receiver.nextPacket();
            }
            break;
        }
    };
  }
}
