import javafx.util.Pair;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class DustBin {
    private Pattern pattern;
    private Matcher matcher;

    Pair<String, Integer> register_args(String message) {
        String truck_id_match = "(.+)";
        String int_match = "(-?\\d+)";
        String match = "register\\(" + truck_id_match + ",\\s*" + int_match + "\\s*\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            Pair<String, Integer> pair = new Pair<>(this.matcher.group(1), Integer.parseInt(this.matcher.group(2)));
            return pair;
        } else {
            return null;
        }
    }

    Pair<String, Intersection> my_position_args(String message) {
        String double_match = "(-?\\d*\\.?\\d+)";
        String position_match = "(\\s*" + double_match + "\\s*,\\s*" + double_match + "\\s*)";
        String truck_id_match = "(.+)";
        String match = "my_position\\(" + truck_id_match + "," + position_match + ",\\s*" + double_match + "\\s*\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            double position[] = new double[2];
            position[0] = Double.parseDouble(this.matcher.group(3));
            position[1] = Double.parseDouble(this.matcher.group(4));
            Intersection inter = new Intersection(position, Double.parseDouble(this.matcher.group(5)), 0.0);
            Pair<String, Intersection> pair = new Pair<>(this.matcher.group(1), inter);
            return pair;
        } else {
            return null;
        }
    }

    String busy_args(String message) {
        String truck_id_match = "(.+)";
        String match = "busy\\(" + truck_id_match + "\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            return this.matcher.group(1);
        } else {
            return null;
        }
    }

    String going_args(String message) {
        String truck_id_match = "(.+)";
        String match = "going\\(" + truck_id_match + "\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            return this.matcher.group(1);
        } else {
            return null;
        }
    }
}
