package watch.stopwatch;

import android.app.Activity;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
    private final int tarsos_channelConfig = android.media.AudioFormat.CHANNEL_IN_MONO;
    private final int tarsos_audioFormat = android.media.AudioFormat.ENCODING_PCM_16BIT;
    private final double threshold = 7;
    private final double sensitivity = 25;

    private int sampleRate = -1;
    private int bufferSize= -1;
    private AudioDispatcher dispatcher = null;
    private PercussionOnsetDetector percussionDetector = null;
    private Activity mainActivity;
    private ClapListener listener = null;

    ClapHandler(final Activity mainActivity) {
        this.mainActivity = mainActivity;
        getValidSampleRates();
        if (sampleRate != -1 && bufferSize != -1) {
            dispatcher = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0);
            percussionDetector = new PercussionOnsetDetector(sampleRate, bufferSize,
                new OnsetHandler() {

                    @Override
                    public void handleOnset(double time, double salience) {
                        if(listener != null)
                            mainActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    listener.clapDetected();
                                }
                            });
                    }
                },
                sensitivity,
                threshold);
        }
    }

    public void setListener(ClapListener clapListener){ listener = clapListener; }

    public void startDetectingClaps(){
        if(dispatcher != null && percussionDetector != null) {
            dispatcher.addAudioProcessor(percussionDetector);
            // note: be careful not to call this multiple times or there'll be conflicts among threads
            new Thread(dispatcher).start();
        }
    }

    public void stopDetectingClaps(){
        if(dispatcher != null && percussionDetector != null) {
            dispatcher.stop();
            dispatcher.removeAudioProcessor(percussionDetector);
        }
    }

    private void getValidSampleRates() {
        for (int rate : mSampleRates) {
            try {
                int buffer_size = AudioRecord.getMinBufferSize(rate, tarsos_channelConfig, tarsos_audioFormat);
                if (buffer_size != AudioRecord.ERROR_BAD_VALUE) {
                    // check if we can instantiate and have a success
                    AudioRecord rec = new AudioRecord(MediaRecorder.AudioSource.MIC, rate, tarsos_channelConfig, tarsos_audioFormat, buffer_size);

                    if (rec.getState() == AudioRecord.STATE_INITIALIZED){
                        Log.d(TAG, "sampleRate= " + rate + " buffer=" + buffer_size);
                        // memorize the data
                        sampleRate = rate;
                        bufferSize = buffer_size;

                        // release the resource
                        rec.release();
                        rec = null;

                        break;
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, rate + "Exception, keep trying.",e);
            }
        }
    }
}
