package com.example.android.medjour.ui;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.adapter.JournalAdapter;
import com.example.android.medjour.adapter.JournalAdapter.JournalViewHolder;
import com.example.android.medjour.databinding.ActivityJournalBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.model.data.JournalEntry;
import com.example.android.medjour.model.viewModels.JournalViewModel;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.widget.WidgetService;

import java.text.DateFormat;
import java.util.List;

import timber.log.Timber;

public class JournalActivity extends AppCompatActivity implements JournalAdapter.DialogClicks {

    private ActivityJournalBinding journalBinder;
    private static JournalDb dB;
    private JournalAdapter journalAdapter;
    private JournalViewHolder journalViewHolder;

    private JournalEntry currentEntry;
    private int selectedEntryId;

    private boolean showEditOptions;
    private boolean showSave;
    private boolean entryDeleted;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        journalBinder = DataBindingUtil.setContentView(this, R.layout.activity_journal);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        setSupportActionBar(journalBinder.journalAppbar.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.app_name);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        dB = JournalDb.getInstance(getApplicationContext());

        //setup adapter and recyclerView
        journalBinder.journalRv.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter = new JournalAdapter(this, this);
        journalBinder.journalRv.setAdapter(journalAdapter);
        Timber.v("adapter and recyclerView setup");

        //setup viewModel
        JournalViewModel viewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        viewModel.getJournalEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Timber.d("Updating entries from LiveData in ViewModel");
                journalAdapter.setJournalEntries(journalEntries);
            }
        });

        //setup the total time Ui.
        setTotalTime();

    }

    /**
     * display the total time of logged meditation across all entries.
     */
    private void setTotalTime() {
        String totalTime = JournalUtils.toMinutes(JournalUtils.retrieveTotalTimeFromPref(this));
        journalBinder.journalAccTimeTv.setText(totalTime);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.setGroupVisible(R.id.pref_save_group, showSave);
        menu.setGroupVisible(R.id.pref_edit_del_group, showEditOptions);
        menu.findItem(R.id.pref_leave_menu).setVisible(showSave || showEditOptions);
        if (showSave || showEditOptions) {
            journalBinder.journalFooter.setVisibility(View.GONE);
        } else {
            journalBinder.journalFooter.setVisibility(View.VISIBLE);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_journal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        journalViewHolder = (JournalViewHolder) journalBinder.journalRv
                .findViewHolderForAdapterPosition(selectedEntryId);
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                onBackPressed();
                if (journalAdapter.entryHasChanged) {
                    unsavedDialog();
                }
                break;
            case R.id.pref_edit:
                showEditOptions = false;
                showSave = true;
                invalidateOptionsMenu();
                journalViewHolder.setTextToEdit();
                break;
            case R.id.pref_delete:
                confirmDeleteAction();
                showEditOptions = false;
                invalidateOptionsMenu();
                break;
            case R.id.pref_save:
                String updatedAssessment = journalViewHolder.getAssessmentUpdate();
                updateEntry(updatedAssessment);
                showSave = false;
                invalidateOptionsMenu();
                journalViewHolder.setEditToText();
            case R.id.pref_leave_menu:
                showSave = false;
                showEditOptions = false;
                invalidateOptionsMenu();
        }
        return true;
    }

    //if any changes were made save these to the db. Otherwise just leave the edit mode.
    private void updateEntry(final String updatedAssessment) {
        if (journalAdapter.entryHasChanged) {
            EntryExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    currentEntry.setAssessment(updatedAssessment);
                    dB.journalDao().updateEntry(currentEntry);
                }
            });
            journalAdapter.notifyItemChanged(selectedEntryId);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (journalAdapter.entryHasChanged) {
            unsavedDialog();
        }
        if (entryDeleted) { //ensure the latest date is saved for the widget to retrieve
            EntryExecutor.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    String date = dB.journalDao().getLastEntryDate();
                    String dateDisplay = DateFormat.getDateInstance().format(date);
                    JournalUtils.saveLastDate(JournalActivity.this, dateDisplay);
                    WidgetService.startHandleActionUpdateWidget(JournalActivity.this);
                }
            });
            entryDeleted = false;
        }
    }

    private void unsavedDialog() {
        DialogInterface.OnClickListener discardClick = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NavUtils.navigateUpFromSameTask(JournalActivity.this);
            }
        };
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,
                R.style.DialogTheme);
        alertBuilder.setMessage(R.string.unsaved_alert);
        alertBuilder.setPositiveButton(R.string.discard_button, discardClick);
        alertBuilder.setNegativeButton(R.string.keep_editing_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (dialog != null) {
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();
    }

    private void confirmDeleteAction() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(JournalActivity.this,
                R.style.DialogTheme);
        alertBuilder.setMessage(R.string.delete_query);
        alertBuilder.setPositiveButton(R.string.delete_button,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteEntry(); //go to delete method
                    }
                });
        alertBuilder.setNegativeButton(R.string.cancel_button, null);
        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();

        setTotalTime(); //update display of accumulative time
    }

    private void deleteEntry() {
        long totalTime = currentEntry.getMedTime() + currentEntry.getMedTime() + currentEntry.getRevTime();
        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                dB.journalDao().deleteEntry(currentEntry);
            }
        });
        journalBinder.journalRv.removeViewAt(selectedEntryId);
        journalAdapter.notifyItemRemoved(selectedEntryId);
        entryDeleted = true;
        JournalUtils.updateTotalTimeFromPref(this, totalTime, JournalUtils.DELETE);
    }

    @Override
    public void getEntryId(int entryId) {
        if (entryId == -1) {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        } else {
            selectedEntryId = entryId;
            currentEntry = journalAdapter.getJournalEntries().get(entryId);
            showEditOptions = true;
            invalidateOptionsMenu();
        }
    }
}