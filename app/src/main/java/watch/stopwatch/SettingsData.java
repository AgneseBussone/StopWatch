package watch.stopwatch;

import android.content.Context;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import watch.stopwatch.Item.DATA_ID;

/**
 * Class to map the group header with the respective children
 */

public class SettingsData {

    public static Map<String, List<Item>> getData(Context context) {

        /* Structure of the map:
         * key (string) = text to display as a group element (the category)
         * value: Item object with text and numeric id
         */
        Map<String, List<Item>> expandableListDetail = new LinkedHashMap<>();

        List<Item> sound_alarm = new ArrayList<>();
        sound_alarm.add(new Item("Sound | Vibrate | None", DATA_ID.ID_SOUND));
        sound_alarm.add(new Item("Ringtone", DATA_ID.ID_RINGTONE));

        List<Item> haptics_input = new ArrayList<>();
        haptics_input.add(new Item("Start Mode", DATA_ID.ID_START));
        haptics_input.add(new Item("Stop Mode", DATA_ID.ID_STOP));
        haptics_input.add(new Item("Lap Mode", DATA_ID.ID_LAP));
        haptics_input.add(new Item("Touch Button Feedback", DATA_ID.ID_TOUCHBTN));

        List<Item> screen_display = new ArrayList<>();
        screen_display.add(new Item("Always ON screen", DATA_ID.ID_SCREEN));
        screen_display.add(new Item("Night Mode", DATA_ID.ID_NIGHT));

        List <Item> about = new ArrayList<>();
        about.add(new Item("Enjoying our App?", DATA_ID.ID_RATE));
        about.add(new Item("Who we are", DATA_ID.ID_DEV));
        about.add(new Item("App version", DATA_ID.ID_VERSION));

        expandableListDetail.put("SOUND / ALARM", sound_alarm);
        expandableListDetail.put("HAPTICS / INPUT", haptics_input);
        expandableListDetail.put("SCREEN / DISPLAY", screen_display);
        expandableListDetail.put("ABOUT APP", about);

        return expandableListDetail;
    }
}
