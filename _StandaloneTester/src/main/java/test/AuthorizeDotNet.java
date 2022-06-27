package test;

import java.math.BigDecimal;
import java.text.MessageFormat;

import net.authorize.api.contract.v1.CreateTransactionRequest;
import net.authorize.api.contract.v1.CreateTransactionResponse;
import net.authorize.api.contract.v1.MerchantAuthenticationType;
import net.authorize.api.contract.v1.MessageTypeEnum;
import net.authorize.api.contract.v1.OpaqueDataType;
import net.authorize.api.contract.v1.PaymentType;
import net.authorize.api.contract.v1.TransactionRequestType;
import net.authorize.api.contract.v1.TransactionResponse;
import net.authorize.api.contract.v1.TransactionTypeEnum;
import net.authorize.api.controller.CreateTransactionController;
import net.authorize.api.controller.base.ApiOperationBase;

public class AuthorizeDotNet
{
    private static final String _apiLoginId = "2bX5V6pXaV";
    private static final String _transactionKey = "4r236f99pR9FE8MH";

    public void purchase(String nonce, String price)
    {
        //merchant information
        MerchantAuthenticationType merchantAuthenticationType  = new MerchantAuthenticationType();
        merchantAuthenticationType.setName(_apiLoginId);
        merchantAuthenticationType.setTransactionKey(_transactionKey);
        ApiOperationBase.setMerchantAuthentication(merchantAuthenticationType);

        //payment information (using a nonce in place of credit card data)
        OpaqueDataType op = new OpaqueDataType();
        op.setDataDescriptor("COMMON.ACCEPT.INAPP.PAYMENT");
        op.setDataValue(nonce);
        PaymentType paymentOne = new PaymentType();
        paymentOne.setOpaqueData(op);

        //payment transaction
        TransactionRequestType transactionRequest = new TransactionRequestType();
        transactionRequest.setAmount(new BigDecimal(price));
        transactionRequest.setTransactionType(TransactionTypeEnum.AUTH_CAPTURE_TRANSACTION.value());
        transactionRequest.setPayment(paymentOne);

        //make the request
        CreateTransactionRequest apiRequest = new CreateTransactionRequest();
        apiRequest.setTransactionRequest(transactionRequest);
        CreateTransactionController controller = new CreateTransactionController(apiRequest);
        controller.execute();

        //read the response
        CreateTransactionResponse response = controller.getApiResponse();
        if (response == null) {
            System.err.println("null response from Authorize.Net request");
        }

        if (response.getMessages().getResultCode() == MessageTypeEnum.OK) {
            TransactionResponse result = response.getTransactionResponse();
            if (result.getMessages() != null) {
                String transactionId = result.getTransId();
                String responseCode = result.getResponseCode();
                String messageCode = result.getMessages().getMessage().get(0).getCode();
                String description = result.getMessages().getMessage().get(0).getDescription();
                String authCode = result.getAuthCode();

//                if (_logger.isDebugEnabled()) {
                String msg = MessageFormat.format(
                    "Authorize.Net transaction successful. transactionId: {0}, responseCode: {1}, messageCode: {2}, description: {3}, authCode: {4}",
                    transactionId, responseCode, messageCode, description, authCode);
//                    _logger.debug(msg);
//                }
                System.out.println(msg);

            } else {
                String errorCode, errorMessage;

                if (response.getTransactionResponse().getErrors() != null) {
                    errorCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
                    errorMessage = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();

                } else {
                    errorCode = "UNKNOWN";
                    errorMessage = "UNKNOWN";
                }

                String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
                System.err.println(errorMsg);
            }

        } else {
            String errorCode, errorMessage;

            if (response.getTransactionResponse() != null && response.getTransactionResponse().getErrors() != null) {
                errorCode = response.getTransactionResponse().getErrors().getError().get(0).getErrorCode();
                errorMessage = response.getTransactionResponse().getErrors().getError().get(0).getErrorText();

            } else {
                errorCode = response.getMessages().getMessage().get(0).getCode();
                errorMessage = response.getMessages().getMessage().get(0).getText();
            }

            String errorMsg = MessageFormat.format("error response from Authorize.net. errorCode: {0}, errorMessage: {1}", errorCode, errorMessage);
            System.err.println(errorMsg);
        }

    }

    public static void main(String[] args)
    {
        AuthorizeDotNet processor = new AuthorizeDotNet();

        processor.purchase("eyJjb2RlIjoiNTBfMl8wNjAwMDUzMzUzNkI5MUU2OUI5MTZFQ0NFNDg0RkI1MjYxN0ZFMjFBRkJCRUZDQzY0Q0MyOUE0NkYyMEVFNzMxMjIzRjEyN0YzOEI0NzM0QjEyRjFGQzkyOUZDNkJEMUJENTE5MUNDIiwidG9rZW4iOiI5NTA3ODM3Mjk5OTQzOTIxNTA0NjA0IiwidiI6IjEuMSJ9", "1.00");
    }

}
