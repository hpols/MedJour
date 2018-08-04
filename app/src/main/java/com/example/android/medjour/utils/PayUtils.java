package com.example.android.medjour.utils;

import android.app.Activity;
import android.util.Pair;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * see: https://developers.google.com/pay/api/android/guides/tutorial
 * and: https://github.com/google-pay/android-quickstart
 * {@link PayUtils} enable google pay API functionality in the app.
 */
public class PayUtils {

    // The cards and thus networks available to the user.
    private static final List<Integer> SUPPORTED_NETWORKS = Arrays.asList(
            WalletConstants.CARD_NETWORK_AMEX,
            WalletConstants.CARD_NETWORK_DISCOVER,
            WalletConstants.CARD_NETWORK_VISA,
            WalletConstants.CARD_NETWORK_MASTERCARD);

    private static final List<Integer> SUPPORTED_METHODS = Arrays.asList(
            WalletConstants.PAYMENT_METHOD_CARD, // any card stored in Google Account.
            WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD); //EMV tokenized credentials

    // Custom parameters required by the processor / gateway.
    private static final List<Pair<String, String>> GATEWAY_TOKENIZATION_PARAMETERS
            = Collections.singletonList(Pair.create("gatewayMerchantId",
            "exampleGatewayMerchantId"));

    private static final BigDecimal MICROS = new BigDecimal(1000000d);

    /**
     * Creates an test instance of {@link PaymentsClient}
     *
     * @param activity is the calling activity.
     */
    public static PaymentsClient createPaymentsClient(Activity activity) {
        Wallet.WalletOptions setupWallet = new Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST).build();
        return Wallet.getPaymentsClient(activity, setupWallet);
    }

    /**
     * Builds {@link PaymentDataRequest} to be consumed by {@link PaymentsClient#loadPaymentData}.
     *
     * @param transactionInfo contains the price for this transaction.
     */
    public static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo) {
        PaymentMethodTokenizationParameters.Builder paramBuild =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "example");
        for (Pair<String, String> param : GATEWAY_TOKENIZATION_PARAMETERS) {
            paramBuild.addParameter(param.first, param.second);
        }

        return createPaymentDataRequest(transactionInfo, paramBuild.build());
    }

    private static PaymentDataRequest createPaymentDataRequest(TransactionInfo transactionInfo,
                                                               PaymentMethodTokenizationParameters params) {

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
    }

    /**
     * Determines whether user is eligible to Pay with Google by calling
     * {@link PaymentsClient#isReadyToPay}. This function returns true whether the user has already
     * stored card details or not.
     *
     * @param client used to send the request.
     */
    public static Task<Boolean> isReadyToPay(PaymentsClient client) {
        IsReadyToPayRequest.Builder requestBuilder = IsReadyToPayRequest.newBuilder();
        for (Integer allowedMethod : SUPPORTED_METHODS) {
            requestBuilder.addAllowedPaymentMethod(allowedMethod);
        }
        return client.isReadyToPay(requestBuilder.build());
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
                .setCurrencyCode("USD")
                .build();
    }

    /**
     * Converts micros to a string format accepted by {@link PayUtils#createTransaction}.
     *
     * @param micros value of the price.
     */
    public static String microsToString(long micros) {
        return new BigDecimal(micros).divide(MICROS).setScale(2,
                RoundingMode.HALF_EVEN).toString();
    }

}