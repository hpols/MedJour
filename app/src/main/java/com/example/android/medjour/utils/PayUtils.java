package com.example.android.medjour.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Pair;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodToken;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * {@link PayUtils} is a class supporting the payment procedure.
 * See: https://developers.google.com/pay/api/android/guides/tutorial
 * and: https://github.com/google-pay/android-quickstart
 */
public class PayUtils {

    // The allowed networks to be requested from the API.
    private static final List<Integer> SUPPORTED_NETWORKS = Arrays.asList(
            WalletConstants.CARD_NETWORK_AMEX,
            WalletConstants.CARD_NETWORK_DISCOVER,
            WalletConstants.CARD_NETWORK_VISA,
            WalletConstants.CARD_NETWORK_MASTERCARD);

    private static final List<Integer> SUPPORTED_METHODS = Arrays.asList(
            WalletConstants.PAYMENT_METHOD_CARD, //returns any card stored in user's Google Account.
            WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD); // if active: EMV token credentials.

    // Custom parameters required by the processor / gateway.
    private static final List<Pair<String, String>> GATEWAY_TOKENIZATION_PARAMETERS
            = Collections.singletonList(Pair.create("gatewayMerchantId",
            "exampleGatewayMerchantId"));

    public static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 666;

    /**
     * Creates an instance of {@link PaymentsClient} for use in an {@link Activity}.
     *
     * @param activity is the caller's activity.
     */
    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .build();
        return Wallet.getPaymentsClient(activity, walletOptions);
    }

    /**
     * Builds {@link PaymentDataRequest} to be consumed by {@link PaymentsClient#loadPaymentData}.
     *
     * @param transactionInfo contains the price for this transaction.
     */
    public static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo) {
        PaymentMethodTokenizationParameters.Builder paramsBuilder =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "example");
        for (Pair<String, String> param : GATEWAY_TOKENIZATION_PARAMETERS) {
            paramsBuilder.addParameter(param.first, param.second);
        }

        return createPaymentDataRequest(transactionInfo, paramsBuilder.build());
    }

    private static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo,
                                                               PaymentMethodTokenizationParameters
                                                                       params) {
        return PaymentDataRequest.newBuilder()
                .setPhoneNumberRequired(false)
                .setEmailRequired(true)
                .setTransactionInfo(transactionInfo)
                .addAllowedPaymentMethods(SUPPORTED_METHODS)
                .setCardRequirements(
                        CardRequirements.newBuilder()
                                .addAllowedCardNetworks(SUPPORTED_NETWORKS)
                                .setAllowPrepaidCards(true)
                                .setBillingAddressRequired(true)
                                .build())
                .setPaymentMethodTokenizationParameters(params)
                .setUiRequired(true)//enables user to select card
                .build();
    }

    /**
     * Determines if the user is eligible to Pay with Google by calling
     * {@link PaymentsClient#isReadyToPay}.
     * <p>
     * If {@link WalletConstants#PAYMENT_METHOD_CARD} is specified among supported methods, this
     * function will return true even if the user has no cards stored. Please refer to the
     * documentation for more information on how the check is performed.
     *
     * @param client used to send the request.
     */
    private static Task<Boolean> isReadyToPay(PaymentsClient client) {
        IsReadyToPayRequest.Builder request = IsReadyToPayRequest.newBuilder();
        for (Integer allowedMethod : SUPPORTED_METHODS) {
            request.addAllowedPaymentMethod(allowedMethod);
        }
        return client.isReadyToPay(request.build());
    }

    /**
     * Builds {@link TransactionInfo} for use with {@link PayUtils#createPaymentDataRequest}.
     * <p>
     * The price is not displayed to the user and must be in the following format: "12.34".
     *
     * @param price total of the transaction.
     */
    public static TransactionInfo createTransaction(String price) {
        return TransactionInfo.newBuilder()
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .setTotalPrice(price)
                .setCurrencyCode("USD")
                .build();
    }

    public static void handlePaymentSuccess(PaymentData paymentData, Context ctxt) {
        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        //
        // Refer to your processor's documentation on how to proceed from here.
        PaymentMethodToken token = paymentData.getPaymentMethodToken();

        if (token != null) {
            // If the gateway is set to example, no payment information is returned - instead, the
            // token will only consist of "examplePaymentMethodToken".
            if (token.getToken().equals("examplePaymentMethodToken")) {
                AlertDialog alertDialog = new AlertDialog.Builder(ctxt)
                        .setTitle("Warning")
                        .setMessage("Gateway set to \"example\". Change to provider before launching.")
                        .setPositiveButton("OK", null)
                        .create();
                alertDialog.show();
            }

            // Use token.getToken() to get the token string.
            Log.d("PaymentData", "PaymentMethodToken received");
        }
    }

    public static void handleError(int statusCode) {
        // At this stage, the user has already seen a popup informing them an error occurred.
        // Normally, only logging is required.
        // statusCode will hold the value of any constant from CommonStatusCode or one of the
        // WalletConstants.ERROR_CODE_* constants.
        Log.w("loadPaymentData failed", String.format("Error code: %d", statusCode));
    }

    public static void checkIsReadyToPay(PaymentsClient paymentsClient) {
        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        isReadyToPay(paymentsClient).addOnCompleteListener(
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

    public static boolean googlePayAvailable;

    private static void setGooglePayAvailable(boolean available) {
        // If isReadyToPay returned true, show the button and hide the "checking" text. Otherwise,
        // notify the user that Pay with Google is not available.
        // Please adjust to fit in with your current user flow. You are not required to explicitly
        // let the user know if isReadyToPay returns false.
        googlePayAvailable = available;
    }

    class upgradeItems {

        private String item;
        private long priceInMicros;

        public upgradeItems(String item, long priceInMicros) {
            this.item = item;
            this.priceInMicros = priceInMicros;
        }

        public String getItem() {
            return item;
        }

        public long getPriceInMicros() {
            return priceInMicros;
        }

        private upgradeItems upgradeItems = new upgradeItems("Simple Bike", 5 * 1000000);

    }
}