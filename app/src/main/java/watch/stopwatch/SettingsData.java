package watch.stopwatch;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import watch.stopwatch.Item.DATA_ID;

/**
 * Class to map the group header with the respective children
 */

public class SettingsData {
    private static final int N_SETTINGS = 4;

    public static Map<GroupInfo, List<Item>> getData() {

        /* Structure of the map:
         * key: GroupInfo object with title and images resources
         * value: Item object with text and numeric id
         */
        Map<GroupInfo, List<Item>> expandableListDetail = new LinkedHashMap<>(N_SETTINGS);

        List<Item> sound_alarm = new ArrayList<>(2);
        sound_alarm.add(new Item("Timer expired", DATA_ID.ID_SOUND));
        sound_alarm.add(new Item("Ringtone", DATA_ID.ID_RINGTONE));

        List<Item> haptics_input = new ArrayList<>(4);
        haptics_input.add(new Item("Touch Button Feedback", DATA_ID.ID_TOUCHBTN));
        haptics_input.add(new Item("Start Mode", DATA_ID.ID_START));
        haptics_input.add(new Item("Stop Mode", DATA_ID.ID_STOP));
        haptics_input.add(new Item("Lap Mode", DATA_ID.ID_LAP));

        List<Item> screen_display = new ArrayList<>(2);
        screen_display.add(new Item("Night Mode", DATA_ID.ID_NIGHT));
        screen_display.add(new Item("Always ON screen", DATA_ID.ID_SCREEN));

        List <Item> about = new ArrayList<>(3);
        about.add(new Item("Enjoying our App?", DATA_ID.ID_RATE));
        about.add(new Item("Who we are", DATA_ID.ID_DEV));
        about.add(new Item("App version", DATA_ID.ID_VERSION));

        expandableListDetail.put(new GroupInfo("SOUND / ALARM", DATA_ID.ID_SOUND,
                R.drawable.ic_sound_on, R.drawable.ic_sound_off), sound_alarm);
        expandableListDetail.put(new GroupInfo("HAPTICS / INPUT", DATA_ID.ID_TOUCHBTN,
                R.drawable.ic_vibrate, R.drawable.ic_no_haptic), haptics_input);
        expandableListDetail.put(new GroupInfo("SCREEN / DISPLAY", DATA_ID.ID_NIGHT,
                R.drawable.ic_night, R.drawable.ic_day), screen_display);
        expandableListDetail.put(new GroupInfo("ABOUT APP", DATA_ID.ID_RATE,
                R.drawable.ic_smile, R.drawable.ic_smile_2), about);

        return expandableListDetail;
    }
}
