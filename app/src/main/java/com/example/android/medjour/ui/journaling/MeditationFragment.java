package com.example.android.medjour.ui.journaling;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerSupportFragment;

import butterknife.ButterKnife;
import timber.log.Timber;

/**
 * A simple {@link Fragment} subclass.
 */
public class MeditationFragment extends Fragment {

    public MeditationFragment() {
        // Required empty public constructor
    }

    // YoutubePlayer implementation:
    // https://stackoverflow.com/questions/26458919/integrating-youtube-to-fragment#comment77123807_26459181
    // http://www.androhub.com/implement-youtube-player-fragment-android-app/

    private YouTubePlayerSupportFragment youTubePlayerFragment; //the player fragment
    private YouTubePlayer youTubePlayer; //the player


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View root = inflater.inflate(R.layout.fragment_meditation, container, false);
        ButterKnife.bind(this, root);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            root.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.indigo));

            youTubePlayerFragment = (YouTubePlayerSupportFragment) getActivity().getSupportFragmentManager()
                    .findFragmentById(R.id.youtube_player_fragment);

//        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
//        transaction.add(R.id.youtube_player, youTubePlayerFragment).commit();

            youTubePlayerFragment.initialize(BuildConfig.API_KEY,
                    new YouTubePlayer.OnInitializedListener() {
                        @Override
                        public void onInitializationSuccess(YouTubePlayer.Provider provider,
                                                            YouTubePlayer player, boolean b) {
                            if (!b) {
                                youTubePlayer = player;
                                youTubePlayer.setFullscreen(true);
                                youTubePlayer.cueVideo(String.valueOf(R.string.meditation_test));
                                //youTubePlayer.play();
                            }

                        }

                        @Override
                        public void onInitializationFailure(YouTubePlayer.Provider provider,
                                                            YouTubeInitializationResult youTubeInitializationResult) {
                            Timber.e("Youtube Player could not be initialized.");
                            //TODO: handle failure
                        }
                    });
        }
        //TODO: when media player is needed (check settings)


        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);

    }

//    @Override
//    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean b) {
//        youTubePlayer.loadVideo(String.valueOf(R.string.meditation_test));
//        startPlaying();
//    }
//
//    private void startPlaying() {
//        youTubePlayer.initialize(BuildConfig.API_KEY, this);
//    }
//
//    @Override
//    public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
//
//    }
}
