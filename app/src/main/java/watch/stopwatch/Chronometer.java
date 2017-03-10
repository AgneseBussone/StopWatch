package watch.stopwatch;

/**
 * Credits:
 *      netplot/src/Chronometer.java
 *      Millisecond-Chronometer/src/gr/antoniom/chronometer/Chronometer.java
 */

public class Chronometer {
    private long startTime = 0;
    private long stopTime = 0;
    private boolean running = false;


    public void start() {
        startTime = System.currentTimeMillis();
        running = true;
    }


    public void stop() {
        stopTime = System.currentTimeMillis();
        running = false;
    }

    public void pause(){
        stop();
    }

    public void resume(){
        startTime += (System.currentTimeMillis() - stopTime);
        running = true;
    }


    // elapsed time in milliseconds
    public long getElapsedTime() {
        if (running) {
            return System.currentTimeMillis() - startTime;
        }
        return stopTime - startTime;
    }


    // elapsed time in seconds
    public long getElapsedTimeSecs() {
        if (running) {
            return ((System.currentTimeMillis() - startTime) / 1000);
        }
        return ((stopTime - startTime) / 1000);
    }

    public Time getTime(){
        long timeElapsed = getElapsedTime();

        int hours = (int)(timeElapsed / (3600 * 1000));
        int remaining = (int)(timeElapsed % (3600 * 1000));

        int minutes = (remaining / (60 * 1000));
        remaining = (remaining % (60 * 1000));

        int seconds = (remaining / 1000);
        remaining = (remaining % (1000));

        return new Time(hours, minutes, seconds, remaining);
    }

}
