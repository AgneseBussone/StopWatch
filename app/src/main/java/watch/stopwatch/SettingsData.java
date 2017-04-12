package watch.stopwatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to map the group header with the respective children
 */

public class SettingsData {

    public static Map<String, List<String[]>> getData() {

        /* Structure of the map:
         * key (string) = text to display as a group element (the category)
         * value: string[0] = text to display as a subitem
         *        string[1] = tag of the textview (preference key)
         */
        Map<String, List<String[]>> expandableListDetail = new LinkedHashMap<String, List<String[]>>();

        List<String[]> sound_alarm = new ArrayList<String[]>();
        sound_alarm.add(new String[]{"Item1", "key_pref_1"});
        sound_alarm.add(new String[]{"Item2", "key_pref_2"});
        sound_alarm.add(new String[]{"Item3", "key_pref_3"});
        sound_alarm.add(new String[]{"Item4", "key_pref_4"});
        sound_alarm.add(new String[]{"Item5", "key_pref_5"});

        List<String[]> haptics_input = new ArrayList<String[]>();
        haptics_input.add(new String[]{"Item1", "key_pref_1"});
        haptics_input.add(new String[]{"Item2", "key_pref_2"});
        haptics_input.add(new String[]{"Item3", "key_pref_3"});
        haptics_input.add(new String[]{"Item4", "key_pref_4"});
        haptics_input.add(new String[]{"Item5", "key_pref_5"});

        List<String[]> screen_display = new ArrayList<String[]>();
        screen_display.add(new String[]{"Item1", "key_pref_1"});
        screen_display.add(new String[]{"Item2", "key_pref_2"});
        screen_display.add(new String[]{"Item3", "key_pref_3"});
        screen_display.add(new String[]{"Item4", "key_pref_4"});
        screen_display.add(new String[]{"Item5", "key_pref_5"});

        expandableListDetail.put("SOUND / ALARM", sound_alarm);
        expandableListDetail.put("HAPTICS / INPUT", haptics_input);
        expandableListDetail.put("SCREEN / DISPLAY", screen_display);
        expandableListDetail.put("ABOUT APP", new ArrayList<String[]>());

        return expandableListDetail;
    }
}
