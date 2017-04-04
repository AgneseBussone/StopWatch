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

    public Time(long elapsed){
        h = (int)(elapsed / (3600 * 1000));
        int remaining = (int)(elapsed % (3600 * 1000));

        m = (remaining / (60 * 1000));
        remaining = (remaining % (60 * 1000));

        s = (remaining / 1000);
        ms = (remaining % (1000));
    }

    public String getFormattedTime(){
        String text = "";
        text += df.format(h) + ":";
        text += df.format(m) + ":";
        text += df.format(s) + ":";
        text += msf.format(ms);
        return text;
    }

    public String getFormattedShortTime(){
        String text = "";
        text += df.format(h) + ":";
        text += df.format(m) + ":";
        text += df.format(s);
        return text;
    }

    public long getMilliseconds(){
        long time = ms;
        time += (s * 1000);
        time += (m * 60 * 1000);
        time += (h * 3600 * 1000);
        return time;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!Time.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final Time other = (Time) obj;
        if(other.h != h || other.m != m || other.s != s)
            return false;
        return true;
    }
}
