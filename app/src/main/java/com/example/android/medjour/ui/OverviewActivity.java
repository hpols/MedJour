package com.example.android.medjour.ui;

import android.app.Activity;
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
import com.example.android.medjour.utils.PayUtils;
import com.example.android.medjour.utils.SettingsUtils;
import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.util.ArrayList;

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
    private PaymentsClient paymentsClient;


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
            JournalUtils.setRepeatedAccess(true, this);
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

        overviewBinder.mainEntryBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent newEntryIntent = new Intent(OverviewActivity.this,
                        NewEntryActivity.class);
                startActivity(newEntryIntent);
            }
        });

        //setup up Ui, notifications and adBanner.
        setupNotification();
        setupCountAndButtons();
        setupAdBanner();

        //ready payments incase the user wants to upgrade
        //see : https://developers.google.com/pay/api/android/guides/tutorial
        if (!JournalUtils.isStudent || !JournalUtils.isFullyUpgraded)
            paymentsClient = Wallet.getPaymentsClient(this, new Wallet.WalletOptions.Builder()
                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                    .build());

    }

    /**
     * show ad to non-student users
     */
    private void setupAdBanner() {
        if (!JournalUtils.isStudent) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                    .build();
            overviewBinder.mainAdView.setVisibility(View.VISIBLE);
            overviewBinder.mainAdView.loadAd(adRequest);
        } else {
            overviewBinder.mainAdView.setVisibility(View.GONE);
        }
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
                        if (codeField.getText().toString().equals(BuildConfig.STUDENT_ACTIVATION_KEY)) {
                            JournalUtils.setIsStudent(true);
                            setupAdBanner();
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
        if (JournalUtils.isStudent) {
            menu.findItem(R.id.menu_activation).setVisible(false);
        } else {
            menu.findItem(R.id.menu_activation).setVisible(true);
        }
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
                break;
            case R.id.menu_upgrade:
                offerUpgrade();
        }
        return super.onOptionsItemSelected(item);
    }

    private void offerUpgrade() {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,
                R.style.DialogTheme);
        alertBuilder.setTitle("Purchase video access and/or remove ads");

        PayUtils.checkIsReadyToPay(paymentsClient);
        if (PayUtils.googlePayAvailable) {
            final String[] choices = new String[]{"Buy video access", "Remove ads"};
            final ArrayList<String> selected = new ArrayList<String>();

            alertBuilder.setMessage("The videos are part of the certification program. However, they can be unlocked for a charge of $5.\n\nThe ads help maintain this app. However a charge of $5 removes them, which goes direclty to future work on the app.")
                    .setMultiChoiceItems(choices, null,
                            new DialogInterface.OnMultiChoiceClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                    for (int i = 0; i < choices.length; i ++) {
                                        if (i == which) {
                                            selected.add(choices[i]);
                                        }
                                    }
                                }
                            })
                    .setPositiveButton("buy", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPayment(selected);
                        }
                    })
                    .setNegativeButton("cancel", null);
        } else {
            alertBuilder.setMessage("Unfortunately, Google Pay does not seem to be activated on this device.");
            alertBuilder.setNeutralButton("ok", null);
        }

        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();
    }

    // This method is called when the Pay with Google button is clicked.
    public void requestPayment(ArrayList<String> selected) {

        // The price provided to the API should include taxes and shipping.
        // This price is not displayed to the user.
        //get price from selection. Currently this is simple … until the pricing gets complexer.
        String price = "0.00";
        switch (selected.size()) {
            case 1:
                price = "5.00";
                break;
            case 2:
                price = "10.00";
                break;
        }
        TransactionInfo transaction = PayUtils.createTransaction(price);
        PaymentDataRequest request = PayUtils.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(request);

        // Since loadPaymentData may show the UI asking the user to select a payment method, we use
        // AutoResolveHelper to wait for the user interacting with it. Once completed,
        // onActivityResult will be called with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, this, PayUtils.LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PayUtils.LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        PayUtils.handlePaymentSuccess(paymentData, this);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        PayUtils.handleError(status.getStatusCode());
                        break;
                }
                break;
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_med_reminder_key))) {
            SettingsUtils.reminderIsUpdated = true;
            setupNotification();
        }
    }
}