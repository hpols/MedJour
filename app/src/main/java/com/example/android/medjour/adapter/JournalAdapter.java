package com.example.android.medjour.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.android.medjour.R;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.utils.JournalUtils;

import java.text.DateFormat;
import java.util.List;

import butterknife.BindView;

public class JournalAdapter extends RecyclerView.Adapter<JournalAdapter.JournalViewHolder> {

    Context ctxt;
    private List<JournalEntry> journalEntries;

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
    public void onBindViewHolder(@NonNull JournalAdapter.JournalViewHolder holder, int position) {
        final JournalEntry currentEntry = journalEntries.get(position);

        long prepTime = currentEntry.getPrepTime();
        long medTime = currentEntry.getMedTime();
        long reviewTime = currentEntry.getRevTime();

        holder.prepTv.setText(JournalUtils.toMinutes(prepTime));
        holder.medTv.setText(JournalUtils.toMinutes(medTime));
        holder.reviewTv.setText(JournalUtils.toMinutes(reviewTime));
        holder.totalTv.setText(JournalUtils.toMinutes(prepTime + medTime
                + reviewTime));
        holder.assessmentTv.setText(currentEntry.getAssessment());
        holder.dateTv.setText(DateFormat.getDateInstance().format(currentEntry.getDate()));
        holder.editFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: go to reviewFragment to enable edits
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
        @BindView(R.id.rv_assessment_tv)
        TextView assessmentTv;
        @BindView(R.id.rv_edit_fab)
        FloatingActionButton editFab;

        public JournalViewHolder(View itemView) {
            super(itemView);
        }
    }
}
