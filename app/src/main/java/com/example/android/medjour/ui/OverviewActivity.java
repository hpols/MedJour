package com.example.android.medjour.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.medjour.BuildConfig;
import com.example.android.medjour.R;
import com.example.android.medjour.databinding.ActivityOverviewBinding;
import com.example.android.medjour.model.data.UpgradeItem;
import com.example.android.medjour.settings.SettingsActivity;
import com.example.android.medjour.utils.JournalUtils;
import com.example.android.medjour.utils.NotificationUtils;
import com.example.android.medjour.utils.PayUtils;
import com.example.android.medjour.utils.SettingsUtils;
import com.facebook.stetho.Stetho;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * {@link OverviewActivity} is the central activity offering a quick overview of total meditation
 * and providing buttons to add new entries to the journal or view the latter once at least one
 * entry has been added.
 */
public class OverviewActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private ActivityOverviewBinding overviewBinder;
    private SettingsUtils utils;
    private PaymentsClient paymentsClient;

    private String totalTimeText;
    private final String TOTAL_TIME = "total time key";

    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 666;
    private ArrayList<UpgradeItem> upgradeItemArrayList;
    private boolean[] upgradeChoice;
    private View dialogPay;
    private ImageButton googlepayBt;
    private TextView googlepayTotal;
    private boolean googlePayAvailable;
    private String priceString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overviewBinder = DataBindingUtil.setContentView(this, R.layout.activity_overview);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(TOTAL_TIME)) {
                totalTimeText = savedInstanceState.getString(TOTAL_TIME);
            }
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);


        //integrating stetho for debugging
        Stetho.initializeWithDefaults(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        if (!JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_REPEAT)) {
            showActivationDialog();
            JournalUtils.setSharedPrefBoo(this, true, JournalUtils.BOO_REPEAT);
        }

        overviewBinder.mainJournalBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent journalIntent = new Intent(OverviewActivity.this,
                        JournalActivity.class);
                startActivity(journalIntent);
            }
        });

        if (JournalUtils.hasMeditatedToday(this)) {
            overviewBinder.mainEntryBt.setEnabled(false);
        } else {
            overviewBinder.mainEntryBt.setEnabled(true);
        }


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

        //ready payments in case the user wants to upgrade
        //see : https://developers.google.com/pay/api/android/guides/tutorial
        if (!JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_STUDENT)
                || !JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_FULLY_UPGRADED)) {
            paymentsClient = PayUtils.createPaymentsClient(this);
            checkIsReadyToPay();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(TOTAL_TIME, totalTimeText);
    }

    /**
     * show ad to non-student users
     */
    private void setupAdBanner() {
        if (!JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_STUDENT)) {
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
                        if (codeField.getText().toString().equals
                                (BuildConfig.STUDENT_ACTIVATION_KEY)) {
                            JournalUtils.setSharedPrefBoo(OverviewActivity.this,
                                    true, JournalUtils.BOO_STUDENT);
                            setupAdBanner();
                            Toast.makeText(OverviewActivity.this,
                                    R.string.toast_successful_student_activation,
                                    Toast.LENGTH_SHORT).show();
                            invalidateOptionsMenu();//reflow the menu, so as to remove the student activation option
                        } else {
                            JournalUtils.setSharedPrefBoo(OverviewActivity.this,
                                    false, JournalUtils.BOO_STUDENT);
                            Toast.makeText(OverviewActivity.this,
                                    R.string.toast_faulty_student_authentification,
                                    Toast.LENGTH_SHORT).show();
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
        if (totalTime == JournalUtils.NO_TOT_TIME) {
            totalTimeText = getString(R.string.overview_no_entries);
            overviewBinder.mainCumulativeTv.setText(totalTimeText);
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

        MenuItem activation = menu.findItem(R.id.menu_activation);
        MenuItem upgrade = menu.findItem(R.id.menu_activation);
        MenuItem info = menu.findItem(R.id.menu_guidelines);

        if (JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_STUDENT)
                || JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_FULLY_UPGRADED)) {
            activation.setVisible(false);
            upgrade.setVisible(false);
        } else {
            activation.setVisible(true);
            upgrade.setVisible(true);
        }

        if (JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_STUDENT)) {
            info.setTitle(R.string.menu_guidelines);
        } else {
            info.setTitle(R.string.menu_information);
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
                startActivity(new Intent(this, InfoActivity.class));
                break;
            case R.id.menu_upgrade:
                showUpgradeDialog();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Display a dialog offering upgrade option which then takes the user on to pay via google pay.
     */
    private void showUpgradeDialog() {
        priceString = getString(R.string.initial_price); //instantiate/reset from previous use

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this,
                R.style.DialogTheme);
        alertBuilder.setTitle("Purchase video access and/or remove ads");

        if (googlePayAvailable) {

            //remove items which have already been bought
            if (JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_ADS_REMOVED)) {
                upgradeItemArrayList.remove(1);
            }
            if (JournalUtils.getsharedPrefBoo(this, JournalUtils.BOO_VIDEOS_UNLOCKED)) {
                upgradeItemArrayList.remove(0);
            }

            final String[] choices = new String[upgradeItemArrayList.size()];
            upgradeChoice = new boolean[upgradeItemArrayList.size()];
            for (int i = 0; i < upgradeItemArrayList.size(); i++) {
                UpgradeItem currentUpgradeItem = upgradeItemArrayList.get(i);
                choices[i] = currentUpgradeItem.getName() + " ($"
                        + PayUtils.microsToString(currentUpgradeItem.getPriceMicros()) + ")";
            }

            googlepayBt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!priceString.equals(getString(R.string.initial_price))) {
                        requestPayment(priceString);
                    } else {
                        Toast.makeText(OverviewActivity.this,
                                R.string.toast_no_upgrade_selected, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            alertBuilder.setMultiChoiceItems(choices, null,
                    new DialogInterface.OnMultiChoiceClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                            for (int i = 0; i < choices.length; i++) {
                                if (i == which) {
                                    upgradeChoice[i] = isChecked;
                                }
                            }
                            priceString = getUpgradePrice(upgradeChoice);
                            googlepayTotal.setText(priceString);
                        }
                    })
                    .setView(dialogPay)
                    .setNegativeButton("cancel", null);
        } else {
            alertBuilder.setMessage("Unfortunately, Google Pay does not seem to be activated on this device.");
            alertBuilder.setNeutralButton("ok", null);
        }

        AlertDialog alertDialog = alertBuilder.create(); //create and show the dialog
        alertDialog.show();
    }

    /**
     * The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
     * OnCompleteListener to be triggered when the result of the call is known.
     */
    private void checkIsReadyToPay() {
        PayUtils.isReadyToPay(paymentsClient).addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(@NonNull Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            setGooglePayAvailable(result);
                        } catch (ApiException exception) {
                            // Process error
                            Log.w("isReadyToPay failed", exception);
                        }
                    }
                });
    }

    /**
     * If isReadyToPay returned true, set up the, upgradeItems array, button and set
     * googlePayAvailable to true.
     *
     * @param available is a boolean detailing whether google pay is available or not
     */
    private void setGooglePayAvailable(boolean available) {
        if (available) {
            //the list of upgrade options
            upgradeItemArrayList = new ArrayList<>();
            upgradeItemArrayList.add(new UpgradeItem(getString(R.string.upgradeItem_videos), 5 * 1000000));
            upgradeItemArrayList.add(new UpgradeItem(getString(R.string.upgradeItem_ads), 5 * 1000000));

            dialogPay = LayoutInflater.from(this).inflate(R.layout.dialog_googlepay, null);
            googlepayBt = dialogPay.findViewById(R.id.googlepay_bt);
            googlepayTotal = dialogPay.findViewById(R.id.dialog_total_tv);
            googlepayTotal.setText(getString(R.string.initial_price)
            );
            googlePayAvailable = true;
        } else {
            googlePayAvailable = false;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOAD_PAYMENT_DATA_REQUEST_CODE:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        assert paymentData != null;
                        handlePaymentSuccess(paymentData);
                        break;
                    case Activity.RESULT_CANCELED:
                        // Nothing to here normally - the user simply cancelled without selecting a
                        // payment method.
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        assert status != null;
                        handleError(status.getStatusCode());
                        break;
                }

                // Re-enables the Pay with Google button.
                googlepayBt.setClickable(true);
                break;
        }
    }

    private void handlePaymentSuccess(PaymentData paymentData) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest.
        if (token != null) {
            // If the gateway is set to example, no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (token.getToken().equals("examplePaymentMethodToken")) {
                Snackbar.make(null, "This is but a test implementation of google pay.",
                        Snackbar.LENGTH_LONG);
//                android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this)
//                        .setMessage("This is but a test implementation of google pay.")
//                        .setPositiveButton(getString(R.string.ok), null)
//                        .create();
//                alertDialog.show();

                for (int i = 0; i < upgradeChoice.length; i++) {
                    if (upgradeChoice[i]) {
                        String boughtUpgrade = upgradeItemArrayList.get(i).getName();
                        if (boughtUpgrade.equals(getString(R.string.upgradeItem_videos))) {
                            JournalUtils.setSharedPrefBoo(this, true,
                                    JournalUtils.BOO_VIDEOS_UNLOCKED);
                        }
                        if (boughtUpgrade.equals(getString(R.string.upgradeItem_ads))) {
                            JournalUtils.setSharedPrefBoo(this, true,
                                    JournalUtils.BOO_ADS_REMOVED);
                        }
                    }
                }

            }

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "PaymentMethodToken received");
        }

    }

    /**
     * At this stage, the user has already seen a popup informing them an error occurred.
     * Normally, only logging is required.
     * statusCode will hold the value of any constant from CommonStatusCode or one of the
     * WalletConstants.ERROR_CODE_* constants.
     *
     * @param statusCode is the error code  passed from the ActivityResult
     */
    private void handleError(int statusCode) {
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    /* This method is called when the Pay with Google button is clicked.*/
    private void requestPayment(String price) {
        // Disables the button to prevent multiple clicks.
        googlepayBt.setClickable(false);

        TransactionInfo transaction = PayUtils.createTransaction(price);
        PaymentDataRequest request = PayUtils.createPaymentDataRequest(transaction);
        Task<PaymentData> futurePaymentData = paymentsClient.loadPaymentData(request);

        // AutoResolveHelper awaits the user's chocie and then calls onActivityResult with the result.
        AutoResolveHelper.resolveTask(futurePaymentData, this, LOAD_PAYMENT_DATA_REQUEST_CODE);
    }

    private String getUpgradePrice(boolean[] upgradeItems) {
        long totalPrice = 0;
        for (int i = 0; i < upgradeItems.length; i++) {
            if (upgradeItems[i]) { //get checked item(s) and from those the according price(s)
                long itemPrice = upgradeItemArrayList.get(i).getPriceMicros();
                totalPrice += itemPrice;
            }
        }
        // calculate the total price
        return PayUtils.microsToString(totalPrice);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_med_reminder_key))) {
            SettingsUtils.reminderIsUpdated = true;
            setupNotification();
        }
    }
}