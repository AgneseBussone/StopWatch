package watch.stopwatch;

import java.text.DecimalFormat;

/**
 * Class that represent a time instant.
 */

public class Time {
    public int h;
    public int m;
    public int s;
    public int ms;
    private DecimalFormat df = new DecimalFormat("00");
    private DecimalFormat msf = new DecimalFormat("000");

    public Time(int hours, int minutes, int seconds, int milliseconds){
        h = hours;
        m = minutes;
        s = seconds;
        ms = milliseconds;
    }

    public String getFormattedTime(){
        String text = "";
        text += df.format(h) + ":";
        text += df.format(m) + ":";
        text += df.format(s) + ":";
        text += msf.format(ms);
        return text;
    }
}
