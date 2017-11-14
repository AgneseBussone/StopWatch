package watch.stopwatch;

/**
 * Interface for the clap thread. The thread will send a message to the listener every time
 * a clap is detected and the listener (which lives in the MainActivity) will act accordingly.
 */

public interface ClapListener {

    void clapDetected();

}
