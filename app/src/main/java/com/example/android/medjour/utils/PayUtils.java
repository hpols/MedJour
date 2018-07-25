package com.example.android.medjour.utils;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;

/**
 * {@link PayUtils} is a class supporting the payment procedure.
 * See: https://developers.google.com/pay/api/android/guides/tutorial
 */
public class PayUtils {

    //keep track of the request made
    public static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 666;

    /**
     * ensure everything is ready to start payment
     *
     * @param paymentsClient is the client through which payment will take place
     */
    private void isReadyToPay(PaymentsClient paymentsClient) {
        IsReadyToPayRequest request = IsReadyToPayRequest.newBuilder()
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .build();
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(new OnCompleteListener<Boolean>() {
            public void onComplete(Task<Boolean> task) {
                try {
                    boolean result = task.getResult(ApiException.class);
                    if (result == true) {
                        // Show Google as payment option.
                    } else {
                        // Hide Google as payment option.
                    }
                } catch (ApiException exception) {
                }
            }
        });
    }

    /**
     * Setup the data request for the payment. Currently it is set to "example" as the app is not
     * live
     *
     * @return the built request ready for the user to fill in
     */
    public static PaymentDataRequest createPaymentDataRequest() {
        PaymentDataRequest.Builder request = PaymentDataRequest.newBuilder().setTransactionInfo(
                TransactionInfo.newBuilder()
                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                        .setTotalPrice("10.00")
                        .setCurrencyCode("USD")
                        .build())
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                .setCardRequirements(CardRequirements.newBuilder().addAllowedCardNetworks(
                        Arrays.asList(WalletConstants.CARD_NETWORK_AMEX,
                                WalletConstants.CARD_NETWORK_DISCOVER,
                                WalletConstants.CARD_NETWORK_VISA,
                                WalletConstants.CARD_NETWORK_MASTERCARD))
                        .build());

        PaymentMethodTokenizationParameters params = PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(
                        WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                .addParameter("gateway", "example")
                .addParameter("gatewayMerchantId", "exampleGatewayMerchantId")
                .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
    }
}
