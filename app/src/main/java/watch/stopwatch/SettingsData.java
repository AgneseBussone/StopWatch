package watch.stopwatch;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to map the group header with the respective children
 */

public class SettingsData {

    public static Map<String, List<String[]>> getData(Context context) {

        /* Structure of the map:
         * key (string) = text to display as a group element (the category)
         * value: string[0] = text to display as a subitem
         *        string[1] = tag of the textview (preference key)
         */
        Map<String, List<String[]>> expandableListDetail = new LinkedHashMap<>();

        List<String[]> sound_alarm = new ArrayList<>();
        sound_alarm.add(new String[]{"Sound | Vibrate | None", context.getResources().getString(R.string.KEY_SOUND)});
        sound_alarm.add(new String[]{"Ringtone", context.getResources().getString(R.string.KEY_RINGTONE)});

        List<String[]> haptics_input = new ArrayList<>();
        haptics_input.add(new String[]{"Start Mode", context.getResources().getString(R.string.KEY_START)});
        haptics_input.add(new String[]{"Stop Mode", context.getResources().getString(R.string.KEY_STOP)});
        haptics_input.add(new String[]{"Lap Mode", context.getResources().getString(R.string.KEY_LAP)});
        haptics_input.add(new String[]{"Touch Button Feedback", context.getResources().getString(R.string.KEY_TOUCHBTN)});

        List<String[]> screen_display = new ArrayList<>();
        screen_display.add(new String[]{"Always ON screen", context.getResources().getString(R.string.KEY_SCREEN)});
        screen_display.add(new String[]{"Night Mode", context.getResources().getString(R.string.KEY_NIGHT)});

        List <String[]> about = new ArrayList<>();
        about.add(new String[]{"Enjoying our App?", ""});
        about.add(new String[]{"Who we are", ""});
        about.add(new String[]{"App version", ""});

        expandableListDetail.put("SOUND / ALARM", sound_alarm);
        expandableListDetail.put("HAPTICS / INPUT", haptics_input);
        expandableListDetail.put("SCREEN / DISPLAY", screen_display);
        expandableListDetail.put("ABOUT APP", about);

        return expandableListDetail;
    }
}
