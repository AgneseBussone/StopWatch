package watch.stopwatch;

/**
 * Class that represent a specific setting inserted in the expandable list
 */

public class Item {

    public enum DATA_ID{
        ID_SOUND,
        ID_RINGTONE,
        ID_START_STOP,
        ID_LAP,
        ID_TOUCHBTN,
        ID_SCREEN,
        ID_NIGHT,
        ID_RATE,
        ID_DEV,
        ID_VERSION
    }

    public String text;     // text for the TextView
    public DATA_ID id;      // numeric id assigned as tag

    public Item(String text, DATA_ID id){
        this.text = text;
        this.id = id;
    }
}
