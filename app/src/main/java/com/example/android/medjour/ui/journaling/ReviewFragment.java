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
import com.example.android.medjour.model.data.JournalDao;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.ui.NewEntryActivity;
import com.example.android.medjour.ui.OverviewActivity;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.widget.WidgetService;

import java.text.DateFormat;
import java.util.Date;

/**
 * A simple {@link Fragment} subclass.
 */
public class ReviewFragment extends Fragment {

    FragmentReviewBinding reviewBinding;
    Date date;
    JournalEntry journalEntry;
    JournalDao journalDao;
    JournalDb dB;

    long startReviewTime;
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

        startReviewTime = System.currentTimeMillis();

        dB = JournalDb.getInstance(getActivity().getApplicationContext());

        //gradually change background color from indigo back to background color,
        // as a slow "release" from the meditation state
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int endColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);

            int startColor = ContextCompat.getColor(getActivity(), R.color.indigo);
            JournalUtils.changeBackground(root, startColor, endColor, JournalUtils.REVIEW_FLAG);
        }

        date = new Date();
        String dateDisplay= DateFormat.getDateInstance().format(date);
        reviewBinding.reviewDateTv.setText(dateDisplay);

        reviewBinding.reviewPrepTv.setText(JournalUtils.toMinutes(NewEntryActivity.getPreparationTime()));
        reviewBinding.reviewMedTv.setText(JournalUtils.toMinutes(NewEntryActivity.getMeditationTime()));

        reviewBinding.reviewSaveFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToDb();
                Intent returnToOverview = new Intent(getActivity(), OverviewActivity.class);
                startActivity(returnToOverview);
            }
        });

        JournalUtils.saveLastDate(getActivity(), dateDisplay);
        JournalUtils.saveTotalTime(getActivity(),
                NewEntryActivity.getPreparationTime()
                        + NewEntryActivity.getMeditationTime() + reviewTime);
        WidgetService.startHandleActionUpdateWidget(getActivity());

        return root;
    }

    private void writeToDb() {
        String assessment = reviewBinding.reviewAssessmentEt.getText().toString().trim();
        reviewTime = System.currentTimeMillis() - startReviewTime;
        journalEntry = new JournalEntry(date, NewEntryActivity.getPreparationTime(),
                NewEntryActivity.getMeditationTime(), reviewTime, assessment);

        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                //insert new entry
                dB.journalDao().createEntry(journalEntry);
            }
        });

    }
}
