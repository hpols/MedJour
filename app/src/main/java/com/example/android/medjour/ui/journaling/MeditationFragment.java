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
 * A simple {@link MeditationFragment} subclass.
 */
public class MeditationFragment extends Fragment {
    private String MED_TIME = "meditation time";

    public MeditationFragment() {
        // Required empty public constructor
    }

    SettingsUtils utils;

    @BindView(R.id.meditation_next_test_bt)
    FloatingActionButton TestFb; //for testing purposes only

    @BindView(R.id.meditation_counter_tv)
    TextView counterTv;

    //all sorts of time related info we want to keep track of …
    long medStartTime, medLength, medLengthInMillis, timeRemaining;

    private CountDownTimer countDownTimer;
    private boolean timerIsRunning = false;

    ToReviewCallback reviewCallback;

    boolean isVideo, videoError;

    public interface ToReviewCallback {
        void toReview(long meditationTime);
    }

    // YoutubePlayer implementation:
    // https://stackoverflow.com/questions/26458919/integrating-youtube-to-fragment#comment77123807_26459181
    // http://www.androhub.com/implement-youtube-player-fragment-android-app/

    // Counter implementation: http://fedepaol.github.io/blog/2016/06/20/how-to-a-timer/

    private YouTubePlayerSupportFragment youTubePlayerFragment; //the player fragment
    private YouTubePlayer youTubePlayer; //the player

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // This ensures that the host activity has implemented the callback interface
        // If not, it throws an exception
        try {
            reviewCallback = (MeditationFragment.ToReviewCallback) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement toReviewCallback");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isVideo) {
            initTimer();
            removeAlarm();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (timerIsRunning && !isVideo) {
            countDownTimer.cancel();
            setAlarm();
        }
    }

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

        //for debugging/testing purposes:
        // this button goes direct to the next fragment of the new-entry flow.
        // Keep it hidden when not debugging
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
     * setup the countedown timer ready to use
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(MED_TIME, getTimeRemaining());
    }

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

    private void startTimer() {
        countDownTimer.start();

        //fade out the timer. The user does not need it and should be encouraged not to repeatedly
        // look at the screen, but to sink into the meditation until the callback is provided.
        // TODO: add http://techdocs.zebra.com/emdk-for-android/3-1/tutorial/tutMxDisplayManager/
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

    private long getTimeRemaining() {
        return medLengthInMillis - (JournalUtils.getNow() - utils.getStartedTime());
    }

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

    private void updateTimeUi(long millisUntilFinished) {

        String timeDisplay = String.format("%02d",
                TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millisUntilFinished)))
                + ":" + String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished)));

        counterTv.setText(String.valueOf(timeDisplay));
    }

    //ensure the device wakes up so as to play the notification to end the meditation time
    public void setAlarm() {
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

    //cancel the device-wake-up when no longer needed
    public void removeAlarm() {
        Intent intent = new Intent(getActivity(), TimerExpiredReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(getActivity(), 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager am = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
        am.cancel(sender);
    }

    //setup and play the chosen video
    private void playVideo() {

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
                                    //for whatever reason … the video is not working today, but the
                                    // meditator's eyes might well already be shut. So plan B: revert
                                    // to sound-callback
                                    if (!timerIsRunning) {
                                        utils.setStartedTime(JournalUtils.getNow());
                                        startTimer();
                                        timerIsRunning = true;
                                    }
                                    Toast.makeText(getActivity(), R.string.video_not_available,
                                            Toast.LENGTH_SHORT).show();
                                    videoError = true;
                                }
                            });

                            //TODO: whenever the user interacts with the playback, this equals the end of
                            //their meditation as they are no longer in focus.

                            youTubePlayer.loadVideo(utils.getVideofromPrefSetting(getActivity()));

                        } else {
                            //seek to remainder of time, so as to stay close to the users chosen
                            // meditation time.
                            while (youTubePlayer.getCurrentTimeMillis() < timeRemaining) {
                                youTubePlayer.pause();
                            }
                            youTubePlayer.seekToMillis((int) timeRemaining);
                            youTubePlayer.play();
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                        YouTubeInitializationResult
                                                                youTubeInitializationResult) {
                        Timber.e("Youtube Player could not be initialized.");
                        //for whatever reason … the video is not working today, but the
                        // meditator's eyes might well already be shut. So plan B: revert
                        // to sound-callback
                        if (!timerIsRunning) {
                            utils.setStartedTime(JournalUtils.getNow());
                            startTimer();
                            timerIsRunning = true;
                        }
                        Toast.makeText(getActivity(), R.string.video_not_available,
                                Toast.LENGTH_SHORT).show();
                        videoError = true;
                    }
                });
    }

    //go to the next fragment of the new-entry-flow
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

    /**
     * {@link TimerExpiredReceiver} is a {@link BroadcastReceiver} providing the end of meditation
     * signal if the device has gone to sleep.
     */
    public class TimerExpiredReceiver extends BroadcastReceiver {
        private String NOT_CALLBACK = "notification_callback";

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
}