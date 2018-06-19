package com.example.android.medjour.ui.journaling;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.utils.UiUtils;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A simple {@link MeditationFragment} subclass.
 */
public class MeditationFragment extends Fragment {

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

        UiUtils.getTimeStamp(medStartTime);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            root.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.indigo));
        }

        //for debugging/testing purposes:
        // this button goes direct to the next fragment of the new-entry flow
        TestFb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long clickTime = Calendar.getInstance().getTimeInMillis();
                reviewCallback.toReview(clickTime - medStartTime);
            }
        });
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //TODO: when media player is needed (check settings)
        youTubePlayerFragment = (YouTubePlayerSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.youtube_player_fragment);

        youTubePlayerFragment.initialize(BuildConfig.API_KEY,
                new YouTubePlayer.OnInitializedListener() {
                    @Override
                    public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                        YouTubePlayer player, boolean b) {
                        if (!b) {
                            youTubePlayer = player;
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
                                    long clickTime = Calendar.getInstance().getTimeInMillis();
                                    reviewCallback.toReview(clickTime - medStartTime);
                                }

                                @Override
                                public void onError(YouTubePlayer.ErrorReason errorReason) {

                                }
                            });
                            youTubePlayer.cueVideo(String.valueOf(R.string.meditation_test));
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



    //TODO: Get length of video or set meditation time (dependent on settings) and move to next
    // fragment once meditation is completeld.
}