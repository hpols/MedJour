package com.example.android.medjour.ui;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.ui.journaling.MeditationFragment;
import com.example.android.medjour.ui.journaling.PreparationFragment;
import com.example.android.medjour.ui.journaling.ReviewFragment;

import timber.log.Timber;

public class NewEntryActivity extends AppCompatActivity implements
        PreparationFragment.toMeditationCallback, MeditationFragment.ToReviewCallback {

    private FragmentManager fragMan;

    private PreparationFragment prepFrag;
    private MeditationFragment medFrag;
    private ReviewFragment revFrag;

    //info gathered for DB
    private static long preparationTime;
    private static long meditationTime;

    public static boolean prepTimeLimitedReached;
    private final String PREP_TIME = "preparation time";
    private final String MED_TIME = "meditation time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        fragMan = getSupportFragmentManager();

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(PREP_TIME)) {
                preparationTime = savedInstanceState.getLong(PREP_TIME);
            }
            if (savedInstanceState.containsKey(MED_TIME)) {
                preparationTime = savedInstanceState.getLong(MED_TIME);
            }

        } else {

            prepFrag = new PreparationFragment();

            //start off with the PreparationFragment
            fragMan.beginTransaction().add(R.id.new_entry_fragment_container, prepFrag).commit();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Save the fragment's instance
        outState.putLong(PREP_TIME, preparationTime);
        outState.putLong(MED_TIME, meditationTime);
    }

    @Override
    public void toMeditation(long preparationTime, boolean prepTimeLimitReached) {
        medFrag = new MeditationFragment();
        NewEntryActivity.preparationTime = preparationTime;
        NewEntryActivity.prepTimeLimitedReached = prepTimeLimitReached;
        Timber.v("preparationTime recorded: " + preparationTime);
        fragMan.beginTransaction().replace(R.id.new_entry_fragment_container, medFrag).commit();
    }

    @Override
    public void toReview(long meditationTime) {
        revFrag = new ReviewFragment();
        NewEntryActivity.meditationTime = meditationTime;
        Timber.v("meditation time recorded: " + meditationTime);
        fragMan.beginTransaction().replace(R.id.new_entry_fragment_container, revFrag).commit();
    }

    public static long getPreparationTime() {
        return preparationTime;
    }

    public static long getMeditationTime() {
        return meditationTime;
    }
}
