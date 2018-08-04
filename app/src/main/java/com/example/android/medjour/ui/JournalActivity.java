package com.example.android.medjour.ui;

import android.Manifest;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

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
import com.example.android.medjour.utils.PdfUtils;
import com.example.android.medjour.widget.WidgetService;
import com.itextpdf.text.DocumentException;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import timber.log.Timber;

public class JournalActivity extends AppCompatActivity implements JournalAdapter.DialogClicks {

    private ActivityJournalBinding journalBinder;
    private static JournalDb dB;
    private JournalAdapter journalAdapter;
    private JournalViewHolder journalViewHolder;

    private JournalEntry currentEntry;
    private List<JournalEntry> journalEntries;
    private int selectedEntryId;

    private boolean showEditOptions;
    private boolean showSave;
    private boolean entryDeleted;
    private static final String WRITE_PERMIT = "external writing permission";
    private static final String READ_PERMIT = "external reading permission";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        journalBinder = DataBindingUtil.setContentView(this, R.layout.activity_journal);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dB = JournalDb.getInstance(getApplicationContext());

        //setup adapter and recyclerView
        journalBinder.journalRv.setLayoutManager(new LinearLayoutManager(this));
        journalAdapter = new JournalAdapter(this, this);
        journalBinder.journalRv.setAdapter(journalAdapter);
        Timber.v("adapter and recyclerview setup");

        //setup viewModel
        JournalViewModel viewModel = ViewModelProviders.of(this).get(JournalViewModel.class);
        viewModel.getJournalEntries().observe(this, new Observer<List<JournalEntry>>() {
            @Override
            public void onChanged(@Nullable List<JournalEntry> journalEntries) {
                Timber.d("Updating entries from LiveData in ViewModel");
                journalAdapter.setJournalEntries(journalEntries);
                JournalActivity.this.journalEntries = journalEntries;
            }
        });

        //setup the total time Ui.
        setTotalTime();

        //when clicked set the pdf-writing action in motion
        journalBinder.exportJournalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final PdfUtils pdfUtils = new PdfUtils(JournalActivity.this);
                if (isStoragePermissionGranted(WRITE_PERMIT)) {
                    Date date = new Date();
                    final String fileName = "MeditationJournal"
                            + new SimpleDateFormat("yyyyMMdd_HHmmss").format(date) + ".pdf";

                    try {
                        pdfUtils.writePdf(journalEntries, fileName);
                    } catch (FileNotFoundException | DocumentException e) {
                        e.printStackTrace();
                    }
                    Snackbar snackbar = Snackbar.make(journalBinder.journalFooter,
                            "Pdf was successfully written", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("View", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            pdfUtils.readPdf(fileName);
                        }
                    });
                    snackbar.show();
                } else {
                    Toast.makeText(JournalActivity.this,
                            R.string.storage_permission_request, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * ensure external sotrage permission is granted. See: https://stackoverflow.com/a/33162451/7601437
     *
     * @param permit what kind of permit are we looking for?
     * @return boolean indicating whehter permission is granted or denied.
     */
    private boolean isStoragePermissionGranted(String permit) {

        String androidPermission = null;
        switch (permit) {
            case READ_PERMIT:
                androidPermission = android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
                break;
            case WRITE_PERMIT:
                androidPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
                break;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(androidPermission) == PackageManager.PERMISSION_GRANTED) {
                Timber.v("External storage permission is granted");
                return true;
            } else {

                Timber.v("External storage permission is revoked");
                ActivityCompat.requestPermissions(this,
                        new String[]{androidPermission}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Timber.v("External storage permission is granted");
            return true;
        }
    }

    /**
     * In case of there being no external storage permission granted, make a request to the user.
     *
     * @param requestCode  is the code detailing the request
     * @param permissions  is the permission being requested
     * @param grantResults is the result of the request
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Timber.v("Permission: " + permissions[0] + "was " + grantResults[0]);
            //resume tasks needing this permission
        }
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