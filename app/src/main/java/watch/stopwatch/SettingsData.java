package watch.stopwatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to map the group header with the respective children
 */

public class SettingsData {

    public static Map<String, List<String>> getData() {

        Map<String, List<String>> expandableListDetail = new LinkedHashMap<String, List<String>>();

        List<String> sound_alarm = new ArrayList<String>();
        sound_alarm.add("Item1");
        sound_alarm.add("Item2");
        sound_alarm.add("Item3");
        sound_alarm.add("Item4");
        sound_alarm.add("Item5");

        List<String> haptics_input = new ArrayList<String>();
        haptics_input.add("Item1");
        haptics_input.add("Item2");
        haptics_input.add("Item3");
        haptics_input.add("Item4");
        haptics_input.add("Item5");

        List<String> screen_display = new ArrayList<String>();
        screen_display.add("Item1");
        screen_display.add("Item2");
        screen_display.add("Item3");
        screen_display.add("Item4");
        screen_display.add("Item5");

        expandableListDetail.put("SOUND / ALARM", sound_alarm);
        expandableListDetail.put("HAPTICS / INPUT", haptics_input);
        expandableListDetail.put("SCREEN / DISPLAY", screen_display);
        expandableListDetail.put("ABOUT APP", new ArrayList<String>());

        return expandableListDetail;
    }
}
