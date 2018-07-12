package com.example.android.medjour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.medjour.R;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.utils.JournalUtils;

import java.text.DateFormat;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    private Context ctxt;
    private List<JournalEntry> journalEntries;
    private JournalDb dB;

    //constructor
    public JournalAdapter(Context ctxt, JournalDb dB) {
        this.ctxt = ctxt;
        this.dB = dB;
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
    public void onBindViewHolder(@NonNull final JournalAdapter.JournalViewHolder holder, final int position) {
        final JournalEntry currentEntry = journalEntries.get(position);

        //Keep track of the assessment view and corresponding fab button
        final String COLLAPSED_ASSESSMENT = "collapsed";
        final String EXPANDED_ASSESSMENT = "expanded";
        final String EDIT_ASSESSMENT = "edit";
        final String[] assessmentState = {COLLAPSED_ASSESSMENT}; //initial state is collapsed

        long prepTime = currentEntry.getPrepTime();
        long medTime = currentEntry.getMedTime();
        long reviewTime = currentEntry.getRevTime();

        //display the stored times and date
        holder.prepTv.setText(JournalUtils.toMinutes(prepTime));
        holder.medTv.setText(JournalUtils.toMinutes(medTime));
        holder.reviewTv.setText(JournalUtils.toMinutes(reviewTime));
        holder.totalTv.setText(JournalUtils.toMinutes(prepTime + medTime
                + reviewTime));
        holder.dateTv.setText(DateFormat.getDateInstance().format(currentEntry.getDate()));

        //present assessment for review or editing
        holder.assessmentTv.setText(currentEntry.getAssessment());

        //expanding, editing and saving the assessment
        holder.editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (assessmentState[0]) {
                    case COLLAPSED_ASSESSMENT: //going to expanded state
                        holder.editFab.setImageResource(android.R.drawable.ic_menu_edit);
                        //TODO: expand to full text length
                        assessmentState[0] = EXPANDED_ASSESSMENT;
                        break;
                    case EXPANDED_ASSESSMENT: // going to edit state
                        holder.editFab.setImageResource(android.R.drawable.ic_menu_save);
                        holder.assessmentTv.setEnabled(true);
                        assessmentState[0] = EDIT_ASSESSMENT;
                        break;
                    case EDIT_ASSESSMENT: //saving and going back to collapsed state
                        holder.editFab.setImageResource(R.drawable.ic_expand_more);
                        String editedAssessment = holder.assessmentTv.getText().toString().trim();
                        updateEntry(currentEntry, editedAssessment);
                        holder.assessmentTv.setEnabled(false);
                        //TODO: collapse to first 5 lines of text
                        assessmentState[0] = COLLAPSED_ASSESSMENT;
                }
            }
        });

        holder.editFab.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //TODO: dialog with option to delete entry
                EntryExecutor.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        dB.journalDao().deleteEntry(currentEntry);
                    }
                });
                return false;
            }
        });
    }

    //TODO: debug whether this works … add dialog to confirm saving before doing so.
    private void updateEntry(final JournalEntry currentEntry, final String editedAssessment) {
        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                currentEntry.setAssessment(editedAssessment);
                dB.journalDao().updateEntry(currentEntry);

            }
        });
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
        EditText assessmentTv;
        @BindView(R.id.rv_edit_fab)
        FloatingActionButton editFab;

        public JournalViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
