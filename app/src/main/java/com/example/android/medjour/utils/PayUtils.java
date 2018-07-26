package com.example.android.medjour.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.example.android.medjour.R;
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
import com.google.android.gms.wallet.ShippingAddressRequirements;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

/**
 * {@link PayUtils} is a class supporting the payment procedure.
 * See: https://developers.google.com/pay/api/android/guides/tutorial
 * and: https://github.com/google-pay/android-quickstart
 */
public class PayUtils {

    private static final BigDecimal MICROS = new BigDecimal(1000000d);

    // This file contains several constants you must edit before proceeding.
    //
    // Please take a look at PaymentsUtil.java to see where the constants are used and to
    // potentially remove ones not relevant to your integration.
    // Required changes:
    // 1.  Update SUPPORTED_NETWORKS and SUPPORTED_METHODS if required (consult your processor if
    //     unsure).
    // 2.  Update CURRENCY_CODE to the currency you use.
    // 3.  Update SHIPPING_SUPPORTED_COUNTRIES to list the countries where you currently ship. If
    //     this is not applicable to your app, remove the relevant bits from PaymentsUtil.java.
    // 4.  If you're integrating with your processor / gateway directly, update
    //     GATEWAY_TOKENIZATION_NAME and GATEWAY_TOKENIZATION_PARAMETERS per the instructions they
    //     provided. You don't need to update DIRECT_TOKENIZATION_PUBLIC_KEY.
    // 5.  If you're using direct integration, please consult the documentation to learn about
    //     next steps.

    // Changing this to ENVIRONMENT_PRODUCTION will make the API return real card information.
    // Please refer to the documentation to read about the required steps needed to enable
    // ENVIRONMENT_PRODUCTION.
    public static final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_TEST;

    // The allowed networks to be requested from the API. If the user has cards from networks not
    // specified here in their account, these will not be offered for them to choose in the popup.
    public static final List<Integer> SUPPORTED_NETWORKS = Arrays.asList(
            WalletConstants.CARD_NETWORK_AMEX,
            WalletConstants.CARD_NETWORK_DISCOVER,
            WalletConstants.CARD_NETWORK_VISA,
            WalletConstants.CARD_NETWORK_MASTERCARD
    );

    public static final List<Integer> SUPPORTED_METHODS = Arrays.asList(
            // PAYMENT_METHOD_CARD returns to any card the user has stored in their Google Account.
            WalletConstants.PAYMENT_METHOD_CARD,

            // PAYMENT_METHOD_TOKENIZED_CARD refers to EMV tokenized credentials stored in the
            // Google Pay app, assuming it's installed.
            // Please keep in mind tokenized cards may exist in the Google Pay app without being
            // added to the user's Google Account.
            WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
    );

    // Required by the API, but not visible to the user.
    public static final String CURRENCY_CODE = "USD";

    // Supported countries for shipping (use ISO 3166-1 alpha-2 country codes).
    // Relevant only when requesting a shipping address.
    public static final List<String> SHIPPING_SUPPORTED_COUNTRIES = Arrays.asList(
            "US",
            "GB"
    );

    // The name of your payment processor / gateway. Please refer to their documentation for
    // more information.
    public static final String GATEWAY_TOKENIZATION_NAME = "example";

    // Custom parameters required by the processor / gateway.
    // In many cases, your processor / gateway will only require a gatewayMerchantId.
    // Please refer to your processor's documentation for more information. The number of parameters
    // required and their names vary depending on the processor.
    public static final List<Pair<String, String>> GATEWAY_TOKENIZATION_PARAMETERS = Arrays.asList(
            Pair.create("gatewayMerchantId", "exampleGatewayMerchantId")

            // Your processor may require additional parameters.
    );

    // Only used for DIRECT tokenization. Can be removed when using GATEWAY tokenization.
    public static final String DIRECT_TOKENIZATION_PUBLIC_KEY = "REPLACE_ME";
    public static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 666;

    /**
     * Creates an instance of {@link PaymentsClient} for use in an {@link Activity}.
     *
     * @param activity is the caller's activity.
     */
    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions walletOptions = new Wallet.WalletOptions.Builder()
                .setEnvironment(PAYMENTS_ENVIRONMENT)
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
                        .addParameter("gateway", GATEWAY_TOKENIZATION_NAME);
        for (Pair<String, String> param : GATEWAY_TOKENIZATION_PARAMETERS) {
            paramsBuilder.addParameter(param.first, param.second);
        }

        return createPaymentDataRequest(transactionInfo, paramsBuilder.build());
    }

    /**
     * Builds {@link PaymentDataRequest} for use with DIRECT integration to be consumed by
     * {@link PaymentsClient#loadPaymentData}.
     * <p>
     * Please refer to the documentation for more information about DIRECT integration. The type of
     * integration you use depends on your payment processor.
     *
     * @param transactionInfo contains the price for this transaction.
     */
    public static PaymentDataRequest createPaymentDataRequestDirect(TransactionInfo transactionInfo) {
        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)

                        // Omitting the publicKey will result in a request for unencrypted data.
                        // Please refer to the documentation for more information on unencrypted
                        // requests.
                        .addParameter("publicKey", DIRECT_TOKENIZATION_PUBLIC_KEY)
                        .build();

        return createPaymentDataRequest(transactionInfo, params);
    }

    private static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo, PaymentMethodTokenizationParameters params) {
        PaymentDataRequest request =
                PaymentDataRequest.newBuilder()
                        .setPhoneNumberRequired(false)
                        .setEmailRequired(true)
                        .setShippingAddressRequired(true)

                        // Omitting ShippingAddressRequirements all together means all countries are
                        // supported.
                        .setShippingAddressRequirements(
                                ShippingAddressRequirements.newBuilder()
                                        .addAllowedCountryCodes(SHIPPING_SUPPORTED_COUNTRIES)
                                        .build())

                        .setTransactionInfo(transactionInfo)
                        .addAllowedPaymentMethods(SUPPORTED_METHODS)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(SUPPORTED_NETWORKS)
                                        .setAllowPrepaidCards(true)
                                        .setBillingAddressRequired(true)

                                        // Omitting this parameter will result in the API returning
                                        // only a "minimal" billing address (post code only).
                                        .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                                        .build())
                        .setPaymentMethodTokenizationParameters(params)

                        // If the UI is not required, a returning user will not be asked to select
                        // a card. Instead, the card they previously used will be returned
                        // automatically (if still available).
                        // Prior whitelisting is required to use this feature.
                        .setUiRequired(true)
                        .build();

        return request;
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
    public static Task<Boolean> isReadyToPay(PaymentsClient client) {
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
     * {@link PayUtils#microsToString} can be used to format the string.
     *
     * @param price total of the transaction.
     */
    public static TransactionInfo createTransaction(String price) {
        return TransactionInfo.newBuilder()
                .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                .setTotalPrice(price)
                .setCurrencyCode(CURRENCY_CODE)
                .build();
    }

    /**
     * Converts micros to a string format accepted by {@link PayUtils#createTransaction}.
     *
     * @param micros value of the price.
     */
    public static String microsToString(long micros) {
        return new BigDecimal(micros).divide(MICROS).setScale(2, RoundingMode.HALF_EVEN).toString();
    }

    public static void handlePaymentSuccess(PaymentData paymentData, Context ctxt) {
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
                AlertDialog alertDialog = new AlertDialog.Builder(ctxt)
                        .setTitle("Warning")
                        .setMessage("Gateway name set to \"example\" - please modify " +
                                "Constants.java and replace it with your own gateway.")
                        .setPositiveButton("OK", null)
                        .create();
                alertDialog.show();
            }

            String billingName = paymentData.getCardInfo().getBillingAddress().getName();
            Toast.makeText(ctxt, ctxt.getString(R.string.payments_show_name, billingName), Toast.LENGTH_LONG).show();

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
                    public void onComplete(Task<Boolean> task) {
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
}
