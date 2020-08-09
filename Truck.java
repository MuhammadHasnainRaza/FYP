import javafx.util.Pair;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.ArrayList;

public class Truck {
    private Pattern pattern;
    private Matcher matcher;

    int give_positions_args(String message) {
        String int_match = "(-?\\d+)";
        String match = "give_positions\\(\\s*" + int_match + "\\s*\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            return Integer.parseInt(this.matcher.group(1));
        } else {
            return -1;
        }
    }

    ArrayList<Pair<Double, Double> > give_help_me_args(String message) {
        String double_match = "(-?\\d*\\.?\\d+)";
        String position_match = "(\\s*" + double_match + "\\s*,\\s*" + double_match + "\\s*)";
        String path_match = "\\(" + position_match + "\\)" + "(\\s*,\\s*\\(" + position_match + "\\))*";
        String match = "help_me\\(\\s*" + path_match + "\\s*\\)";
        this.pattern = Pattern.compile(match);
        this.matcher = this.pattern.matcher(message);
        if(this.matcher.matches()) {
            ArrayList<Pair<Double, Double> > list = new ArrayList<>();
            this.pattern = Pattern.compile(position_match);
            this.matcher = this.pattern.matcher(message);
            while(this.matcher.find()) {
                Pattern pattern2 = Pattern.compile(double_match);
                Matcher matcher2 = pattern2.matcher(matcher.group());
                matcher2.find();
                double x = Double.parseDouble(matcher2.group());
                matcher2.find();
                double y = Double.parseDouble(matcher2.group());
                Pair<Double, Double> pair = new Pair<>(x, y);
                list.add(pair);
            }
            return list;
        } else {
            return null;
        }
    }
}
