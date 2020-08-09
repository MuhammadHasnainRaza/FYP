import com.cyberbotics.webots.controller.Robot;
import com.cyberbotics.webots.controller.Supervisor;
import com.cyberbotics.webots.controller.Receiver;
import com.cyberbotics.webots.controller.Emitter;
import com.cyberbotics.webots.controller.GPS;
import com.cyberbotics.webots.controller.InertialUnit;
import java.util.ArrayList;
import javafx.util.Pair;

public class go_to_bin {

  public static void main(String[] args) {

    Supervisor robot = new Supervisor();

    // Get the time step of the current world
    int timeStep = (int) Math.round(robot.getBasicTimeStep());
    
    // Enable GPS
    GPS gps = robot.getGPS("gps");
    gps.enable(timeStep);
    
    // Enable IMU
    InertialUnit imu = robot.getInertialUnit("inertial unit");
    imu.enable(timeStep);
    
    // Get receiver channel and enable receiver
    int receiver_channel = Integer.parseInt(args[0]);
    Receiver receiver = robot.getReceiver("receiver");
    receiver.setChannel(receiver_channel);
    receiver.enable(timeStep);
    
    // Retrieve emmitter and set to broadcast channel
    Emitter emitter = robot.getEmitter("emitter");
    emitter.setChannel(Emitter.CHANNEL_BROADCAST);
    
    // Set states
    final int STATE_IDLE = 0;
    final int STATE_POS_RESPOND = 1;
    final int STATE_GO = 2;
    int current_state = STATE_IDLE;
    
    // Initialize incoming message matcher and truck instance
    MessageMatcher msg_matcher = new MessageMatcher();
    Truck truck = new Truck();
    
    // Start loop count to send register message only every 10 time steps
    int loop_count = 0;
    
    // Initialize path array to follow
    ArrayList<Pair<Double, Double> > path = null;
    
    double velocity[] = new double[6];
    velocity[0] = 0;
    velocity[1] = 0;
    velocity[2] = 0;
    velocity[3] = 0;
    velocity[4] = 0;
    velocity[5] = 0;
    robot.getSelf().setVelocity(velocity);

    // Main loop
    while (robot.step(timeStep) != -1) {
      switch(current_state) {
        case STATE_IDLE:
          // Stop truck if it was moving
          velocity[0] = 0;
          velocity[1] = 0;
          velocity[2] = 0;
          velocity[3] = 0;
          velocity[4] = 0;
          velocity[5] = 0;
          robot.getSelf().setVelocity(velocity);
          
          // Send register message
          if(loop_count == 0) {
            String out_msg = "register(" + robot.getName() + "," + receiver_channel + ")";
            emitter.send(out_msg.getBytes());
            System.out.println("Truck " + robot.getName() + ": broadcasting register message");
          } else if(loop_count == 10) {
            loop_count = 0;
          }
          loop_count++;
          
          // Handle incoming messages
          if(receiver.getQueueLength() > 0) {
            String message = new String(receiver.getData());
            int message_type = msg_matcher.setMessage(message);
            if(message_type == MessageMatcher.GIVE_POSITIONS) {
              int channel = truck.give_positions_args(message);
              if(channel >= 0) {
                // Set bin's channel as outgoing
                emitter.setChannel(channel);
                
                // Read current position and orientation of truck using GPS and IMU
                double position_x = gps.getValues()[0];
                double position_y = gps.getValues()[2];
                double orientation = imu.getRollPitchYaw()[2];
                
                // Send current position of truck to channel
                String out_msg = "my_position(" + robot.getName() + "," + position_x + "," + position_y + "," + orientation + ")";
                emitter.send(out_msg.getBytes());
                System.out.println("Truck " + robot.getName() + ": sent location");
                
                // Stop truck if it was moving
                velocity[0] = 0;
                velocity[1] = 0;
                velocity[2] = 0;
                velocity[3] = 0;
                velocity[4] = 0;
                velocity[5] = 0;
                robot.getSelf().setVelocity(velocity);
                
                // Move to STATE_POS_RESPOND
                current_state = STATE_POS_RESPOND;
              }
            }
            receiver.nextPacket();
          }
          break;
        case STATE_POS_RESPOND:
          // Handle incoming messages
          if(receiver.getQueueLength() > 0) {
            String message = new String(receiver.getData());
            int message_type = msg_matcher.setMessage(message);
            if(message_type == MessageMatcher.HELP_ME) {
              // Save given path
              path = truck.give_help_me_args(message);
              System.out.println("Truck " + robot.getName() + ": received " + message);
              System.out.println("Truck " + robot.getName() + ": going towards bin");
              
              // Move to STATE_GO
              current_state = STATE_GO;
            } else if(message_type == MessageMatcher.NO_HELP) {
              current_state = STATE_IDLE;
              System.out.println("Truck " + robot.getName() + ": staying in idle state");
            }
            receiver.nextPacket();
          }
          break;
        case STATE_GO:
          // Handle incoming messages
          if(receiver.getQueueLength() > 0) {
            String message = new String(receiver.getData());
            int message_type = msg_matcher.setMessage(message);
            if(message_type == MessageMatcher.GIVE_POSITIONS) {
              int channel = truck.give_positions_args(message);
              if(channel >= 0) {
                // Set bin's channel as outgoing
                emitter.setChannel(channel);
                
                // Send busy to channel
                String out_msg = "busy(" + robot.getName() + ")";
              }
            }
            receiver.nextPacket();
          }
          
          // Move truck towards next end position
          Pair<Double, Double> first = path.get(0);
          double position_x = gps.getValues()[0];
          double position_y = gps.getValues()[2];
          double orientation = imu.getRollPitchYaw()[2];
          double target_x = first.getKey();
          double target_y = first.getValue();
          double delta_x = target_x - position_x;
          double delta_y = target_y - position_y;
          double distance = Math.sqrt(delta_x*delta_x + delta_y*delta_y);
          double right_pos = Math.cos(orientation)*delta_x - Math.sin(orientation)*delta_y;
          double front_pos = Math.sin(orientation)*delta_x + Math.cos(orientation)*delta_y;
          double angle_error = Math.PI/2.0 - Math.atan2(front_pos,right_pos);
          if(angle_error > Math.PI) {
            angle_error = -(angle_error - Math.PI);
          }
          
          Pair<Double, Double> second = null;
          double target_x_2 = 0.0;
          double target_y_2 = 0.0;
          double delta_x_2 = 0.0;
          double delta_y_2 = 0.0;
          double distance_2 = 0.0;
          double right_pos_2 = 0.0;
          double front_pos_2 = 0.0;
          double angle_error_2 = 0.0;
          if(path.size() > 1) {
            second = path.get(1);
            target_x_2 = second.getKey();
            target_y_2 = second.getValue();
            delta_x_2 = target_x_2 - position_x;
            delta_y_2 = target_y_2 - position_y;
            distance_2 = Math.sqrt(delta_x_2*delta_x_2 + delta_y_2*delta_y_2);
            right_pos_2 = Math.cos(orientation)*delta_x_2 - Math.sin(orientation)*delta_y_2;
            front_pos_2 = Math.sin(orientation)*delta_x_2 + Math.cos(orientation)*delta_y_2;
            angle_error_2 = Math.PI/2.0 - Math.atan2(front_pos_2,right_pos_2);
            if(angle_error_2 > Math.PI) {
              angle_error_2 = -(angle_error_2 - Math.PI);
            }
          }
          
          if(distance > 2) {
            velocity[0] = 0.5*Math.sin(orientation);//x velocity
            velocity[1] = 0;
            velocity[2] = 0.5*Math.cos(orientation);//y velocity
            velocity[3] = 0;
            velocity[4] = 0;//angular velocity
            velocity[5] = 0;
            if(distance > 5 && (angle_error*180.0/Math.PI < -1.5 || angle_error*180.0/Math.PI > 1.5)){
              velocity[4] = 0.5*Math.signum(angle_error);
            } else {
              if(distance*velocity[0] <  10) {
                velocity[0] = distance*velocity[0];
              } else {
                velocity[0] = 10*velocity[0];
              }
              if(distance*velocity[2] <  10) {
                velocity[2] = distance*velocity[2];
              } else {
                velocity[2] = 10*velocity[2];
              }
            }
            if(distance < 5 && second != null) {
              if(distance_2 > 5 && (angle_error_2*180.0/Math.PI < -1.5 || angle_error_2*180.0/Math.PI > 1.5)){
                velocity[4] = 0.25*Math.signum(angle_error_2);
              } else {
                if(velocity[0] < 10) {
                  if(velocity[0] + distance_2*velocity[0] <  10) {
                    velocity[0] = velocity[0] + distance_2*velocity[0];
                  } else {
                    velocity[0] = 10*Math.sin(orientation);
                  }
                }
                if(velocity[2] < 10) {
                  if(velocity[2] + distance_2*velocity[2] <  10) {
                    velocity[2] = velocity[2] + distance_2*velocity[2];
                  } else {
                    velocity[2] = 10*Math.cos(orientation);
                  }
                }
              }
            }
            robot.getSelf().setVelocity(velocity);
          } else {
            path.remove(0);
          }
          
          // If done
          if(path.size() == 0) {
            // Send going() back to dustbin
            String out_msg = "going(" + robot.getName() + ")";
            emitter.send(out_msg.getBytes());
            
            // Reset emitter channel to broadcast and return to STATE_IDLE
            emitter.setChannel(Emitter.CHANNEL_BROADCAST);
            current_state = STATE_IDLE;
            System.out.println("Truck " + robot.getName() + ": arrived at bin");
          }
          break;
      }
    }
  }
}
