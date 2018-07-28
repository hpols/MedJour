package com.example.android.medjour.ui.journaling;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.FragmentReviewBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.ui.NewEntryActivity;
import com.example.android.medjour.ui.OverviewActivity;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.widget.WidgetService;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    FragmentReviewBinding reviewBinding;

    JournalEntry journalEntry;
    JournalDb dB;

    long startReviewTime, reviewTime;

    String currentAssessment;
    String dateDisplay;
    private String CURRENT_ASSESSMENT = "current assessment key";
    private String REVIEW_TIME = "review time key";
    private String DATE_KEY = "date key";


    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState != null) {
            if(savedInstanceState.containsKey(CURRENT_ASSESSMENT)) {
                currentAssessment = savedInstanceState.getString(CURRENT_ASSESSMENT);
            }
            if (savedInstanceState.containsKey(REVIEW_TIME)) {
                startReviewTime = savedInstanceState.getLong(REVIEW_TIME);
            }
            if (savedInstanceState.containsKey(DATE_KEY)) {
                dateDisplay = savedInstanceState.getString(DATE_KEY);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_review, container,
                false);
        View root = reviewBinding.getRoot();

        if(savedInstanceState == null) {
            startReviewTime = System.currentTimeMillis();
        }

        dB = JournalDb.getInstance(getActivity().getApplicationContext());

        //gradually change background color from indigo back to background color,
        // as a slow "release" from the meditation state
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

            int startColor = ContextCompat.getColor(getActivity(), R.color.indigo);
            JournalUtils.changeBackground(root, startColor, endColor, JournalUtils.REVIEW_FLAG);
        }

        Date date = new Date();
        dateDisplay = DateFormat.getDateInstance().format(date);
        reviewBinding.reviewDateTv.setText(dateDisplay);

        reviewBinding.reviewPrepTv.setText(JournalUtils.toMinutes(NewEntryActivity.getPreparationTime()));
        reviewBinding.reviewMedTv.setText(JournalUtils.toMinutes(NewEntryActivity.getMeditationTime()));

        reviewBinding.reviewSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveEntry(dateDisplay);
                Intent returnToOverview = new Intent(getActivity(), OverviewActivity.class);
                startActivity(returnToOverview);
            }
        });

        if (NewEntryActivity.prepTimeLimitedReached) {
            Toast.makeText(getActivity(), getString(R.string.review_toast_part_I)
                            + String.valueOf(NewEntryActivity.getPreparationTime())
                            + getString(R.string.review_toast_part_II_prep),
                    Toast.LENGTH_SHORT).show();
            NewEntryActivity.prepTimeLimitedReached = false;
        }

        return root;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        currentAssessment = reviewBinding.reviewAssessmentEt.getText().toString().trim();
        outState.putString(CURRENT_ASSESSMENT, currentAssessment);
        outState.putLong(REVIEW_TIME, startReviewTime);
        outState.putString(DATE_KEY, dateDisplay);
    }

    private void saveEntry(String dateDisplay) {
        String assessment = reviewBinding.reviewAssessmentEt.getText().toString().trim();
        reviewTime = System.currentTimeMillis() - startReviewTime;

        boolean reviewTimeLimitReached = false;

        //we can only log a certain amount of minutes for review, as per the C.MI. regulations
        if (reviewTime > JournalUtils.MAX_REVIEW_TIME) {
            reviewTime = JournalUtils.MAX_REVIEW_TIME;
            reviewTimeLimitReached = true;
        }
        journalEntry = new JournalEntry(dateDisplay, NewEntryActivity.getPreparationTime(),
                NewEntryActivity.getMeditationTime(), reviewTime, assessment);

        long totalTimeFromEntry =  NewEntryActivity.getPreparationTime() +
                NewEntryActivity.getMeditationTime() + reviewTime;

        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //insert new entry
                dB.journalDao().createEntry(journalEntry);
            }
        });

        JournalUtils.saveLastDate(getActivity(), dateDisplay);
        JournalUtils.updateTotalTimeFromPref(getActivity(), totalTimeFromEntry, JournalUtils.CREATE);
        WidgetService.startHandleActionUpdateWidget(getActivity());

        JournalUtils.setRingerMode(getActivity(), JournalUtils.NOT_NORMAL);

        if (reviewTimeLimitReached) {
            Toast.makeText(getActivity(), getString(R.string.review_toast_part_I)
                            + String.valueOf(TimeUnit.MILLISECONDS.toMinutes(reviewTime))
                            + getString(R.string.review_toast_part_II_review), Toast.LENGTH_SHORT).show();
        }

        Intent returnToOverview = new Intent(getActivity(), OverviewActivity.class);
        startActivity(returnToOverview);
    }
}