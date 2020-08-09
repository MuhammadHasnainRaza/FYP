import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class MessageMatcher {
    private Pattern pattern;
    private Matcher matcher;
    private String[] match_strings;
    private int matched_pos = -1;

    public static final int REGISTER = 0;
    public static final int GIVE_POSITIONS = 1;
    public static final int MY_POSITION = 2;
    public static final int BUSY = 3;
    public static final int HELP_ME = 4;
    public static final int GOING = 5;
    public static final int NO_HELP = 6;

    public MessageMatcher() {
        String double_match = "(-?\\d*\\.?\\d+)";
        String position_match = "(\\s*" + double_match + "\\s*,\\s*" + double_match + "\\s*)";
        String match_help = "help\\(" + position_match + "\\)";
        String truck_id_match = "(.+)";
        String int_match = "(-?\\d+)";
        String match_register = "register\\(" + truck_id_match + ",\\s*" + int_match + "\\s*\\)";
        String match_give_positions = "give_positions\\(\\s*" + int_match + "\\s*\\)";
        String match_my_position = "my_position\\(" + truck_id_match + "," + position_match + ",\\s*" + double_match + "\\s*\\)";
        String match_busy = "busy\\(" + truck_id_match + "\\)";
        String path_match = "\\(" + position_match + "\\)" + "(\\s*,\\s*\\(" + position_match + "\\))*";
        String match_help_me = "help_me\\(\\s*" + path_match + "\\s*\\)";
        String match_going = "going\\(" + truck_id_match + "\\)";
        String match_no_help = "no_help";
        this.match_strings = new String[7];
        this.match_strings[REGISTER] = match_register;
        this.match_strings[GIVE_POSITIONS] = match_give_positions;
        this.match_strings[MY_POSITION] = match_my_position;
        this.match_strings[BUSY] = match_busy;
        this.match_strings[HELP_ME] = match_help_me;
        this.match_strings[GOING] = match_going;
        this.match_strings[NO_HELP] = match_no_help;
    }

    public int setMessage(String message) {
        boolean one_match = false;
        for(int i = 0; i < this.match_strings.length; i++) {
            this.pattern = Pattern.compile(this.match_strings[i]);
            this.matcher = this.pattern.matcher(message);
            if(this.matcher.matches()) {
                one_match = true;
                this.matched_pos = i;
                break;
            }
        }
        if(!one_match) {
            this.matched_pos = -1;
        }
        return this.matched_pos;
    }
}
