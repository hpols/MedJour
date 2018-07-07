package com.example.android.medjour.ui.journaling;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.utils.SettingsUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A simple {@link MeditationFragment} subclass.
 */
public class MeditationFragment extends Fragment {

    public void setMedLength(int medLength) {
        this.medLength = medLength;
    }

    public void setMediaType(int mediaType) {
        this.mediaType = mediaType;
    }

    int medLength;
    int mediaType;

    public MeditationFragment() {
        // Required empty public constructor
    }

    //for testing purposes only
    @BindView(R.id.meditation_next_test_bt)
    FloatingActionButton TestFb;

    long medStartTime;
    ToReviewCallback reviewCallback;

    public interface ToReviewCallback {
        void toReview(long meditationTime);
    }

    // YoutubePlayer implementation:
    // https://stackoverflow.com/questions/26458919/integrating-youtube-to-fragment#comment77123807_26459181
    // http://www.androhub.com/implement-youtube-player-fragment-android-app/

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meditation, container, false);
        ButterKnife.bind(this, root);

        medStartTime = System.currentTimeMillis();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            root.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.indigo));
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
        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (SettingsUtils.getMeditationCallback(getActivity()) == SettingsUtils.VIDEO_CB) {
            playVideo();
        } else {
            //TODO: set timer and play tone once completed & hide the youtubefragment
            SettingsUtils.getSound(getActivity());

           // Uri notification = RingtoneManager.getDefaultUri(Ri);
            //Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
            //r.play();
            Toast.makeText(getActivity(), "Sound Callback", Toast.LENGTH_SHORT).show();

        }


    }

    private void playVideo() {
        youTubePlayerFragment = YouTubePlayerSupportFragment.newInstance();

        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        transaction.add(R.id.youtube_player_fragment, youTubePlayerFragment).commit();

        youTubePlayerFragment.initialize(BuildConfig.API_KEY,
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                        YouTubePlayer player, boolean b) {
                        if (!b) {
                            youTubePlayer = player;

                            //when video has finished automatically move forward to next fragment
                            youTubePlayer.setPlayerStateChangeListener(new YouTubePlayer.PlayerStateChangeListener() {
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

                                }
                            });

                            //whenever the user interacts with the playback, this equals the end of
                            //their meditation as they are no longer in focus.

                            //TODO: replace hardcoded example with video from setting
                            youTubePlayer.loadVideo(getString(R.string.meditation_5min));
                        }
                    }

                    @Override
                    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                        YouTubeInitializationResult
                                                                youTubeInitializationResult) {
                        Timber.e("Youtube Player could not be initialized.");
                    }
                });
    }

    private void GoToReview() {
        long medTime = System.currentTimeMillis() - medStartTime;
        reviewCallback.toReview(medTime);
    }
}