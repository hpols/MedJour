package com.example.android.medjour.ui;

import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.ui.journaling.MeditationFragment;
import com.example.android.medjour.ui.journaling.PreparationFragment;
import com.example.android.medjour.ui.journaling.ReviewFragment;

import timber.log.Timber;

public class NewEntryActivity extends AppCompatActivity implements PreparationFragment.toMeditationCallback,
        MeditationFragment.ToReviewCallback {

    FragmentManager fragMan;

    PreparationFragment prepFrag;
    MeditationFragment medFrag;
    ReviewFragment revFrag;

    //info gathered for DB
    long preparationTime;
    long meditationTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_entry);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        fragMan = getSupportFragmentManager();

        prepFrag = new PreparationFragment();
        medFrag = new MeditationFragment();
        revFrag = new ReviewFragment();

        //start off with the PreparationFragment
        fragMan.beginTransaction().add(R.id.new_entry_fragment_container, prepFrag).commit();
    }

    @Override
    public void toMeditation(long preparationTime) {
        this.preparationTime = preparationTime;
        Timber.v("preparationTime recorded: " + preparationTime);
        fragMan.beginTransaction().replace(R.id.new_entry_fragment_container, medFrag).commit();
    }

    @Override
    public void toReview(long meditationTime) {
       this.meditationTime = meditationTime;
       Timber.v("meditation time recorded: " + meditationTime);
       fragMan.beginTransaction().replace(R.id.new_entry_fragment_container, revFrag).commit();
    }
}
