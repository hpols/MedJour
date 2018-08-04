package com.example.android.medjour.ui.journaling;

import android.animation.Animator;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.utils.SettingsUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * {@link MeditationFragment} is the core of the journal flow. No input is expected from the user,
 * who is expected to be meditating throughout the time of this fragment being active. Therefor the
 * fragment has an inbuilt backup system in case the user has chosen to use the video callback, but
 * the video does not load or there is no internet.
 */
public class MeditationFragment extends Fragment {
    private final String MED_TIME = "meditation time";

    public MeditationFragment() {
        // Required empty public constructor
    }

    private SettingsUtils utils;

    @BindView(R.id.meditation_next_test_bt)
    FloatingActionButton TestFb; //for testing purposes only

    @BindView(R.id.meditation_counter_tv)
    TextView counterTv;

    //all sorts of time related info we want to keep track of …
    private long medStartTime, medLength, medLengthInMillis, timeRemaining;

    // Counter implementation: http://fedepaol.github.io/blog/2016/06/20/how-to-a-timer/
    private CountDownTimer countDownTimer;
    private boolean timerIsRunning = false;

    //track whether the user wants a video callback and if there is an error in its use
    private boolean isVideo, videoError;

    private ToReviewCallback reviewCallback;

    public interface ToReviewCallback {
        void toReview(long meditationTime);
    }

    // YoutubePlayer implementation:
    // https://stackoverflow.com/questions/26458919/integrating-youtube-to-fragment#comment77123807_26459181
    // http://www.androhub.com/implement-youtube-player-fragment-android-app/
    private YouTubePlayerSupportFragment youTubePlayerFragment; //the player fragment
    private YouTubePlayer youTubePlayer; //the player

    /**
     * ensure the host activity has the callback is in place
     *
     * @param ctxt is the context
     */
    @Override
    public void onAttach(Context ctxt) {
        super.onAttach(ctxt);
        try {
            reviewCallback = (MeditationFragment.ToReviewCallback) ctxt;
        } catch (ClassCastException e) {
            throw new ClassCastException(ctxt.toString() + " must implement toReviewCallback");
        }
    }

    /**
     * if the activity resumes: remove the Alarm and reapply the timer.
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!isVideo) {
            initTimer();
            removeAlarm();
        }
    }

    /**
     * as the activity pauses remove the timer and setup the alarm
     */
    @Override
    public void onPause() {
        super.onPause();
        if (timerIsRunning) {
            countDownTimer.cancel();
            setAlarm();
        }
    }

    /**
     * once the activity is created retrieve any saved instances and – if the sound-callback is
     * enabled – set up and start the timer based on the retrieved data.
     *
     * @param savedInstanceState is the data stored before the activity was destroyed
     */
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        //retrieve remaining time if the activity is recreated
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(MED_TIME)) {
                timeRemaining = savedInstanceState.getLong(MED_TIME);
                if (!isVideo) {
                    utils.getStartedTime();
                    setupTimer(timeRemaining);
                    startTimer();
                }
            }
        }

        //for debugging/testing purposes: this button goes direct to the next fragment of the
        // new-entry flow. Keep it hidden when not debugging
        if (BuildConfig.DEBUG) {
            TestFb.setVisibility(View.VISIBLE);
            TestFb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GoToReview();
                }
            });
        }
    }

    /**
     * create the view based on the provided xml, along with the basic variables needed
     *
     * @param inflater           is the LayoutInflater for the fragment
     * @param container          is the ViewGroup within which the fragment is inflated
     * @param savedInstanceState is any data saved before the activity was destroyed
     * @return the view containing the Fragment.
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meditation, container, false);
        ButterKnife.bind(this, root);

        if (savedInstanceState == null) {
            medStartTime = JournalUtils.getNow();
        }

        utils = new SettingsUtils(getActivity());

        medLength = utils.getMeditationLength(getActivity());
        Timber.d("meditation time is set to: " + medLength);
        medLengthInMillis = TimeUnit.MINUTES.toMillis(medLength);
        timeRemaining = medLengthInMillis;
        isVideo = utils.getMeditationCallback(getActivity()) == SettingsUtils.VIDEO_CB;

        //keep the color animation going
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            root.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.indigo));
        }

        //prepare countDownTimer when a soundCallback is needed.
        if (!isVideo) {
            setupTimer(medLengthInMillis);
        }
        return root;
    }

    /**
     * save the remaining time before the activity gets destroyed
     *
     * @param outState is the bundle to which data is stored before the activity is destroyed
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MED_TIME, getTimeRemaining());
    }

    /**
     * Once the view is fully created determine whether the user has set a video or audio callback
     * and setup the chosen callback
     *
     * @param view               is the finished created view
     * @param savedInstanceState is any data stored before the activity was destroyed
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (isVideo) {
            counterTv.setVisibility(View.GONE); // no need for the counterTv when video is playing
            //ready the youtube fragment if this is the initial create
            youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.add(R.id.youtube_player_fragment, youTubePlayerFragment).commit();
            playVideo();

        } else {
            if (!timerIsRunning) {
                utils.setStartedTime(JournalUtils.getNow());
                startTimer();
                timerIsRunning = true;
            }
        }
    }

    // ––––––––––––––––––––––––––– //
    // –––––– TIMER METHODS –––––– //
    // ––––––––––––––––––––––––––– //

    /**
     * setup the countdown timer ready to use
     *
     * @param length the remaining time to be count down
     */
    private void setupTimer(long length) {
        countDownTimer = new CountDownTimer(length, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                getTimeRemaining();
                updateTimeUi(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                timerIsRunning = false;
                onTimerFinish();
                updateTimeUi(0);
            }
        };
    }

    /**
     * start the timer
     */
    private void startTimer() {
        countDownTimer.start();

        //fade out the timer. The user does not need it and should be encouraged not to repeatedly
        // look at the screen, but to sink into the meditation until the callback is provided.
        counterTv.setVisibility(View.VISIBLE);
        counterTv.setAlpha(1);
        counterTv.animate().setDuration(60000).alpha(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                counterTv.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });
    }

    /**
     * initialize the timer
     */
    private void initTimer() {
        long startTime = utils.getStartedTime();
        if (startTime > 0) {
            timeRemaining = getTimeRemaining();
            if (timeRemaining <= 0) { // TIMER EXPIRED
                timeRemaining = medLengthInMillis;
                timerIsRunning = false;
                onTimerFinish();
            } else {
                startTimer();
                timerIsRunning = true;
            }
        } else {
            timeRemaining = medLengthInMillis;
            timerIsRunning = false;
        }
        updateTimeUi(timeRemaining);
    }

    /**
     * get the remaining time based by comparing the first time the fragment was entered to the
     * current time
     *
     * @return the remaining time in miliseconds as a long
     */
    private long getTimeRemaining() {
        return medLengthInMillis - (JournalUtils.getNow() - utils.getStartedTime());
    }

    /**
     * When the timer has hit 0 play selected sound and move onto the next view.
     */
    private void onTimerFinish() {
        utils.setStartedTime(0);
        timeRemaining = medLengthInMillis;
        updateTimeUi(timeRemaining);
        try {
            Uri notification = utils.playCallbackSound(getActivity());
            Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
            r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        GoToReview();
    }

    /**
     * keep the timer Ui updated whilst it is displayed
     *
     * @param millisUntilFinished is the time still left on the clock
     */
    private void updateTimeUi(long millisUntilFinished) {

        String timeDisplay = String.format("%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)))
                + ":" + String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

        counterTv.setText(String.valueOf(timeDisplay));
    }

    /**
     * ensure the device wakes up so as to play the notification to end the meditation time
     */
    private void setAlarm() {
        long wakeUpTime = utils.getStartedTime() + medLengthInMillis;
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getActivity(), TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            assert am != null;
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, sender), sender);
        } else {
            am.set(AlarmManager.RTC_WAKEUP, wakeUpTime, sender);
        }
    }

    /**
     * cancel the device-wake-up when no longer needed
     */
    private void removeAlarm() {
        Intent intent = new Intent(getActivity(), TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    /**
     * {@link TimerExpiredReceiver} is a {@link BroadcastReceiver} providing the end of meditation
     * signal if the device has gone to sleep (which it most probably will have).
     */
    public class TimerExpiredReceiver extends BroadcastReceiver {
        private final String NOT_CALLBACK = "notification_callback";

        @Override
        public void onReceive(Context ctxt, Intent intent) {
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            Intent callbackReceiver = new Intent(ctxt, MeditationFragment.class);
            PendingIntent pIntent = PendingIntent.getActivity(ctxt, 0, callbackReceiver,
                    0);

            NotificationCompat.Builder notBuild = new NotificationCompat.Builder(ctxt, NOT_CALLBACK);
            Uri notTone = utils.playCallbackSound(getActivity());
            notBuild.setSound(notTone)
                    .setContentTitle("Meditation finished")
                    .setAutoCancel(true)
                    .setContentText("Meditation finished")
                    .setSmallIcon(android.R.drawable.ic_notification_clear_all)
                    .setContentIntent(pIntent);

            Notification not = notBuild.build();
            NotificationManager notMan =
                    (NotificationManager) ctxt.getSystemService(Context.NOTIFICATION_SERVICE);
            notMan.notify(0, not);

            GoToReview();
        }
    }

    // ––––––––––––––––––––––––– //
    //–––––– VIDEO METHODS ––––––//
    // ––––––––––––––––––––––––– //

    /**
     * setup and play the chosen video
     */
    private void playVideo() {

        //ensure there is internet connectivity. If not use sound callback
        ConnectivityManager connectMan = (ConnectivityManager)
                getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        assert connectMan != null;
        NetworkInfo netInfo = connectMan.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isConnectedOrConnecting()) {

            youTubePlayerFragment.initialize(BuildConfig.API_KEY,
                    new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                            YouTubePlayer player, boolean b) {
                            if (!b) {
                                youTubePlayer = player;

                                //when video has finished automatically move forward to next fragment
                                youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer
                                        .PlayerStateChangeListener() {
                                    @Override
                                    public void onLoading() {
                                    }

                                    @Override
                                    public void onLoaded(String s) {
                                    }

                                    @Override
                                    public void onAdStarted() {
                                    }

                                    @Override
                                    public void onVideoStarted() {
                                    }

                                    @Override
                                    public void onVideoEnded() {
                                        GoToReview();
                                    }

                                    @Override
                                    public void onError(YouTubePlayer.ErrorReason errorReason) {
                                        revertToTimer();
                                    }
                                });

                                youTubePlayer.loadVideo(utils.getVideofromPrefSetting(getActivity()));
                            }
                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                            YouTubeInitializationResult
                                                                    youTubeInitializationResult) {
                            Timber.e("Youtube Player could not be initialized.");

                            revertToTimer();
                        }
                    });
        } else {
            revertToTimer();
        }
    }

    /**
     * for whatever reason the video is not working today, but the meditator's eyes might well
     * already be shut. So plan B: revert to sound-callback
     */
    private void revertToTimer() {
        if (!timerIsRunning) {
            utils.setStartedTime(JournalUtils.getNow());
            startTimer();
            timerIsRunning = true;
        }
        Toast.makeText(getActivity(), R.string.video_not_available,
                Toast.LENGTH_SHORT).show();
        videoError = true;
    }

    /**
     * go to the next fragment of the new-entry-flow
     */
    private void GoToReview() {
        long medTime = System.currentTimeMillis() - medStartTime;
        reviewCallback.toReview(medTime);

        //add vibration if the user has selected this option in the settings.
        //based on: https://stackoverflow.com/a/13950364/7601437
        if (utils.vibrateEnabled(getActivity())) {
            Vibrator vib = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            // Vibrate for 500 milliseconds
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vib.vibrate(VibrationEffect.createOneShot(500,
                        VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                //deprecated in API 26
                vib.vibrate(500);
            }
        }
        if (videoError) {//repeat message at meditation end to ensure the user is not puzzled
            Toast.makeText(getActivity(), R.string.video_not_available,
                    Toast.LENGTH_SHORT).show();
            videoError = false;
        }
    }
}