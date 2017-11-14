package watch.stopwatch;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.support.annotation.NonNull;
import android.util.Log;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.onsets.OnsetHandler;
import be.tarsos.dsp.onsets.PercussionOnsetDetector;

/**
 * Class that manages all the settings required for the clap detection.
 */

public class ClapHandler {

    private static final String TAG = ClapHandler.class.getSimpleName();

    // Audio recording rates, from the highest to the lowest
    private final int[] mSampleRates = new int[] {44100, 22050, 11025, 16000, 8000};
    private final short[] mAudioFormat = new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT };
    private final short[] mChannelConfig = new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO };
    private final double threshold = 15;
    private final double sensitivity = 65;

    private int sampleRate = -1;
    private int bufferSize= -1;
    private AudioRecord recorder = null;
    private AudioDispatcher dispatcher = null;
    private PercussionOnsetDetector percussionDetector = null;

    private ClapListener listener = null;

    ClapHandler() {
        // initialize audio settings
        recorder = findAudioRecord();
        if (recorder != null) {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize * 2, 0);
            percussionDetector = new PercussionOnsetDetector(sampleRate, bufferSize * 2,
                    new OnsetHandler() {

                        @Override
                        public void handleOnset(double time, double salience) {
                            Log.d(TAG, "Clap detected!");
//                            if(listener != null)
//                                listener.clapDetected();
                        }
                    }, sensitivity, threshold);
        }
    }

    public boolean isReady(){ return (recorder != null); }

    public void setListener(ClapListener clapListener){ listener = clapListener; }

    public void startDetectingClaps(){
        if(dispatcher != null && percussionDetector != null) {
            dispatcher.addAudioProcessor(percussionDetector);
            new Thread(dispatcher).start();
        }
    }

    public void stopDetectingClaps(){
        if(dispatcher != null && percussionDetector != null) {
            dispatcher.stop();
            dispatcher.removeAudioProcessor(percussionDetector);
        }
    }

    private AudioRecord findAudioRecord() {
        for (int rate : mSampleRates) {
            for (short audioFormat : mAudioFormat) {
                for (short channelConfig : mChannelConfig) {
                    try {
                        Log.d(TAG, "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
                                + channelConfig);
                        int buffer_size = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

                        if (buffer_size != AudioRecord.ERROR_BAD_VALUE) {
                            // check if we can instantiate and have a success
                            AudioRecord rec = new AudioRecord(MediaRecorder.AudioSource.DEFAULT, rate, channelConfig, audioFormat, buffer_size);

                            if (rec.getState() == AudioRecord.STATE_INITIALIZED){
                                // memorize the data
                                sampleRate = rate;
                                bufferSize = buffer_size;
                                return rec;
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, rate + "Exception, keep trying.",e);
                    }
                }
            }
        }
        return null;
    }
}
