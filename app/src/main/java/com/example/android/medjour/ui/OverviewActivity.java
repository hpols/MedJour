package com.example.android.medjour.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.databinding.ActivityOverviewBinding;
import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.settings.SettingsActivity;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.utils.NotificationUtils;
import com.example.android.medjour.utils.SettingsUtils;
import com.facebook.stetho.Stetho;

import timber.log.Timber;

/**
 * {@link OverviewActivity} is the central activity offering a quick overview of total meditation
 * and providing buttons to add new entries to the journal or view the latter once at least one
 * entry has been added.
 */
public class OverviewActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    ActivityOverviewBinding overviewBinder;
    JournalDb dB;
    SettingsUtils utils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overviewBinder = DataBindingUtil.setContentView(this, R.layout.activity_overview);

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


        //integrating stetho for debugging
        Stetho.initializeWithDefaults(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        dB = JournalDb.getInstance(getApplicationContext());

        if (!JournalUtils.isRepeatedAccess(this)) {
            showActivationDialog();
            JournalUtils.setRepeatedAccess(false, this);
        }

        overviewBinder.mainJournalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent journalIntent = new Intent(OverviewActivity.this,
                        JournalActivity.class);
                startActivity(journalIntent);
            }
        });

        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                if (JournalUtils.hasMeditatedToday(dB)) {
                    overviewBinder.mainEntryBt.setEnabled(false);
                } else {
                    overviewBinder.mainEntryBt.setEnabled(true);
                }
            }
        });

        //if the user has enabled notification schedule the next one
        setupNotification();

        overviewBinder.mainEntryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEntryIntent = new Intent(OverviewActivity.this,
                        NewEntryActivity.class);
                startActivity(newEntryIntent);
            }
        });

        setupCountAndButtons();
    }

    /**
     * A dialog enabling the user to switch to student-use of the app
     */
    private void showActivationDialog() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,
                R.style.DialogTheme);
        alertBuilder.setMessage(R.string.activation_text);
        // Create an input field to receive the activation code
        final EditText codeField = new EditText(this);
        codeField.setInputType(InputType.TYPE_CLASS_TEXT);
        alertBuilder.setView(codeField);

        alertBuilder.setPositiveButton(R.string.activation_student_bt,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (codeField.getText().equals(BuildConfig.STUDENT_ACTIVATION_KEY)) {
                            //TODO: reflow app to display student flavor
                            JournalUtils.setIsStudent(true);
                        } else {
                            JournalUtils.setIsStudent(false);
                        }

                    }
                });
        alertBuilder.setNegativeButton(R.string.activation_free_bt, null);
        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();

    }

    /**
     * setup the accumulative count fo the meditation as well as the (non-)activation of the
     * journal button.
     */
    private void setupCountAndButtons() {
        long totalTime = JournalUtils.retrieveTotalTimeFromPref(this);
        String totalTimeText = null;
        if (totalTime == JournalUtils.NO_TOT_TIME) {
            overviewBinder.mainCumulativeTv.setText(R.string.overview_no_entries);
        } else {
            totalTimeText = getString(R.string.total_time_label) + JournalUtils.toMinutes(totalTime);
            overviewBinder.mainJournalBt.setVisibility(View.VISIBLE);
        }
        overviewBinder.mainCumulativeTv.setText(totalTimeText);
    }

    /**
     * setup the notification to either fire as soon as the allotted time has passed or ready for
     * future reminders
     */
    private void setupNotification() {
        utils = new SettingsUtils(this);
        if (utils.reminderIsEnabled(this)) {
            NotificationUtils.scheduleNotification(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupCountAndButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.menu_settings:
                startActivity(new Intent(this, SettingsActivity.class));
                break;
            case R.id.menu_activation:
                showActivationDialog();
                break;
            case R.id.menu_guidelines:
                //TODO: go to guidelines
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_med_reminder_key))) {
            SettingsUtils.reminderIsUpdated = true;
            setupNotification();
        }
    }
}