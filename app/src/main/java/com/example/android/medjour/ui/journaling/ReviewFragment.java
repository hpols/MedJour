package com.example.android.medjour.ui.journaling;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.android.medjour.R;
import com.example.android.medjour.databinding.FragmentReviewBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.JournalDao;
import com.example.android.medjour.model.JournalDb;
import com.example.android.medjour.model.JournalEntry;
import com.example.android.medjour.ui.NewEntryActivity;
import com.example.android.medjour.ui.OverviewActivity;
import com.example.android.medjour.utils.UiUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    FragmentReviewBinding reviewBinding;
    Date date;
    JournalEntry journalEntry;
    JournalDao journalDao;
    JournalDb dB;

    long prepTime;
    long medTime;
    long reviewTime;


    public ReviewFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        reviewBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_review, container,
                false);
        View root = reviewBinding.getRoot();

        dB = JournalDb.getInstance(getActivity().getApplicationContext());

        //gradually change background color from indigo back to background color,
        // as a slow "release" from the meditation state
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

            int startColor = ContextCompat.getColor(getActivity(), R.color.indigo);
            UiUtils.changeBackground(root, startColor, endColor);
        }

        date = new Date();
        String dateDisplay= DateFormat.getDateInstance().format(date);
        reviewBinding.reviewDateTv.setText(dateDisplay);

        reviewBinding.reviewPrepTv.setText(toMinutes(NewEntryActivity.getPreparationTime()));
        reviewBinding.reviewMedTv.setText(toMinutes(NewEntryActivity.getMeditationTime()));

        reviewBinding.reviewSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToDb();
                Intent returnToOverview = new Intent(getActivity(), OverviewActivity.class);
                startActivity(returnToOverview);
            }
        });

        return root;
    }

    private void writeToDb() {
        String assessment = reviewBinding.reviewAssessmentEt.getText().toString().trim();
        journalEntry = new JournalEntry(date, NewEntryActivity.getPreparationTime(),
                NewEntryActivity.getMeditationTime(), reviewTime, assessment);

        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //insert new entry
                dB.journalDao().createEntry(journalEntry);

                //TODO: add logic to update entry
            }
        });

    }

    private String toMinutes(long timeInMillis) {
        return String.valueOf(TimeUnit.MILLISECONDS.toMinutes(timeInMillis)) + " min";
    }
}
