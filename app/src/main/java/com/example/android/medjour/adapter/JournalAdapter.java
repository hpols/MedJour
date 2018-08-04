package com.example.android.medjour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.medjour.R;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.utils.JournalUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private final Context ctxt;
    private List<JournalEntry> journalEntries;
    public boolean entryHasChanged = false; //flag whether there has been a change

    private static ArrayList<CardView> cardViewArrayList = new ArrayList<>();


    private final DialogClicks dialogClicks;

    public interface DialogClicks {
        void getEntryId(int entryId);
    }

    //constructor
    public JournalAdapter(Context ctxt, DialogClicks dialogClicks) {
        this.ctxt = ctxt;
        this.dialogClicks = dialogClicks;
    }

    @NonNull
    @Override
    public JournalAdapter.JournalViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                               int viewType) {
        View root = LayoutInflater.from(ctxt).inflate(R.layout.rv_journal, parent,
                false);
        root.setFocusable(true);

        return new JournalViewHolder(root);
    }

    @Override
    public void onBindViewHolder(@NonNull final JournalViewHolder holder, final int position) {
        final JournalEntry currentEntry = journalEntries.get(position);

        long prepTime = currentEntry.getPrepTime();
        long medTime = currentEntry.getMedTime();
        long reviewTime = currentEntry.getRevTime();

        //display the stored times and date
        holder.prepTv.setText(JournalUtils.toMinutes(prepTime));
        holder.medTv.setText(JournalUtils.toMinutes(medTime));
        holder.reviewTv.setText(JournalUtils.toMinutes(reviewTime));
        holder.totalTv.setText(JournalUtils.toMinutes(prepTime + medTime
                + reviewTime));
        holder.dateTv.setText(currentEntry.getDate());

        //present assessment for review or editing
        holder.setAssessment(currentEntry.getAssessment());

        if (holder.assessment.length() <= 100) {
            holder.assessmentTv.setText(holder.assessment);
        } else {
            holder.extract = holder.assessment.substring(0, 120) + " (…)";
            holder.assessmentTv.setText(holder.extract);
        }

        //keep track of any changes made to the assessment text.
        holder.assessmentEt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // not needed
            }

            @Override
            public void afterTextChanged(Editable s) {
                entryHasChanged = true;
            }
        });

        addHolder(holder.rvHolder);
    }

    private static void addHolder(CardView rvHolder)
    {
        cardViewArrayList.add(rvHolder);
    }

    @Override
    public int getItemCount() {
        if (journalEntries == null) return 0;
        else return journalEntries.size();
    }

    public void setJournalEntries(List<JournalEntry> journalEntries) {
        this.journalEntries = journalEntries;
        notifyDataSetChanged();
    }

    public List<JournalEntry> getJournalEntries() {
        return journalEntries;
    }

    public class JournalViewHolder extends RecyclerView.ViewHolder {

        //header
        @BindView(R.id.rv_total_tv)
        TextView totalTv;
        @BindView(R.id.rv_date_tv)
        TextView dateTv;

        //left side
        @BindView(R.id.rv_prep_tv)
        TextView prepTv;
        @BindView(R.id.rv_med_tv)
        TextView medTv;
        @BindView(R.id.rv_review_tv)
        TextView reviewTv;

        //right side
        @BindView(R.id.rv_assessment_et)
        EditText assessmentEt;
        @BindView(R.id.rv_assessment_tv)
        TextView assessmentTv;

        @BindView(R.id.focus_grabber)
        TextView focusGrabber;

        //the recyclerViewHolder
        @BindView(R.id.rv_holder)
            CardView rvHolder;


        boolean isCollapsed = true;

        void setAssessment(String assessment) {
            this.assessment = assessment;
        }

        String assessment;
        String extract;

        JournalViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    Timber.v("Clicking so long!");
                    dialogClicks.getEntryId(getAdapterPosition());
                    return false;
                }
            });

            assessmentTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.v("Click!");
                    if (isCollapsed) {
                        assessmentTv.setText(assessment);
                        isCollapsed = false;
                    } else {
                        assessmentTv.setText(extract);
                        isCollapsed = true;
                    }
                }
            });
        }

        public String getAssessmentUpdate() {
            return assessmentEt.getText().toString().trim();
        }

        public void setTextToEdit() {
            assessmentTv.setVisibility(View.INVISIBLE);
            assessmentEt.setVisibility(View.VISIBLE);
            assessmentEt.setText(assessment);
        }

        public void setEditToText() {
            assessmentTv.setVisibility(View.VISIBLE);
            assessmentEt.setFocusable(false);
            assessmentEt.setEnabled(false);
            assessmentEt.setVisibility(View.GONE);
            assessmentTv.setText(assessment);
            focusGrabber.requestFocus();
            dialogClicks.getEntryId(-1);

        }
    }
}
