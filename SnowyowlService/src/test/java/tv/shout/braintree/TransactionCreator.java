package tv.shout.braintree;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import com.braintreegateway.BraintreeGateway;
import com.braintreegateway.CreditCard;
import com.braintreegateway.CreditCardRequest;
import com.braintreegateway.Customer;
import com.braintreegateway.CustomerRequest;
import com.braintreegateway.Environment;
import com.braintreegateway.Result;
import com.braintreegateway.Transaction;
import com.braintreegateway.TransactionRequest;
import com.braintreegateway.ValidationError;
import com.braintreegateway.ValidationErrors;
import com.braintreegateway.exceptions.NotFoundException;
import com.meinc.store.domain.CreditCardInfo;
import com.meinc.store.domain.Item;
import com.meinc.store.exception.StoreException;

public class TransactionCreator
{
    private BraintreeGateway _gatewaySandbox;
    private CreditCardInfo _ccInfoSandbox;

    private BraintreeGateway _gatewayProduction;
    private CreditCardInfo _ccInfoProduction;

    public TransactionCreator()
    {
        _gatewaySandbox = new BraintreeGateway(
            Environment.SANDBOX,
            "9kgbhrrvk7rgytpr",
            "cvc74qps2qfpz8pb",
            "0f93cd573ce824d3fccf0036001e084e"
        );

        _ccInfoSandbox = new CreditCardInfo();
        _ccInfoSandbox.setName("John Doe");
        _ccInfoSandbox.setNumber("4111111111111111");
        _ccInfoSandbox.setExpDate("12/2020");
        _ccInfoSandbox.setCvv("123");
        _ccInfoSandbox.setStreetAddress("123 Cherry Lane");
        _ccInfoSandbox.setLocality("Springfield");
        _ccInfoSandbox.setRegion("UT");
        _ccInfoSandbox.setCountry("US");
        _ccInfoSandbox.setPostalCode("84602");

        _gatewayProduction = new BraintreeGateway(
            Environment.PRODUCTION,
            "wsd844ptx99y22jf",
            "6br5tvp3gx7wnndq",
            "1f89183e5ec1886564d6875e36564a71"
        );

        _ccInfoProduction = new CreditCardInfo();
        _ccInfoProduction.setName("Bruce K. Grant, Jr");
        _ccInfoProduction.setNumber("4120466010352938");
        _ccInfoProduction.setExpDate("07/2019");
        _ccInfoProduction.setCvv("194");
        _ccInfoProduction.setStreetAddress("798 W. 2410 N.");
        _ccInfoProduction.setLocality("Pleasant Grove");
        _ccInfoProduction.setRegion("UT");
        _ccInfoProduction.setCountry("US");
        _ccInfoProduction.setPostalCode("84062");
    }

    public void purchaseItem(BraintreeGateway gateway, String customerId, CreditCardInfo ccInfo, Item item)
    throws StoreException
    {
        String ccToken = getCreditCardToken(gateway, customerId, ccInfo);
        System.out.println("token: " + ccToken);
        String orderId = UUID.randomUUID().toString();

        //create a transaction
        TransactionRequest transactionRequest = new TransactionRequest()
            .amount(new BigDecimal(item.getPrice()))
            .orderId(orderId)
            .customerId(customerId)
            .customField("product_name", item.getTitle())
            .paymentMethodToken(ccToken)
            .options()
                .submitForSettlement(true)
                .storeInVaultOnSuccess(true)
                .done();

        Result<Transaction> transactionResult = gateway.transaction().sale(transactionRequest);
        Transaction transaction = transactionResult.getTarget();
        if (!transactionResult.isSuccess() || transaction == null)
            throwPurchaseException(transactionResult);
    }

    private String getCreditCardToken(BraintreeGateway gateway, String customerId, CreditCardInfo ccInfo)
    throws StoreException
    {
        String ccToken;

        //see if a customer exists
        Customer customer = null;
        try {
            customer = gateway.customer().find(customerId);
            System.out.println(customer);
        } catch (NotFoundException ignored) {
        }

        //create the customer
        if (customer == null) {
            CustomerRequest customerRequest = new CustomerRequest()
                .id(customerId);

            Result<Customer> customerResult = gateway.customer().create(customerRequest);
            customer = customerResult.getTarget();
            if (!customerResult.isSuccess() || customer == null)
                throwPurchaseException(customerResult);

            //add the credit card / billing address
            CreditCardRequest creditCardRequest = new CreditCardRequest()
                .customerId(customerId)
                .cardholderName(ccInfo.getName())
                .number(ccInfo.getNumber())
                .expirationDate(ccInfo.getExpDate())
                .cvv(ccInfo.getCvv())
                .billingAddress()
                    .streetAddress(ccInfo.getStreetAddress())
                    //.extendedAddress(ccInfo.getExtAddress())
                    .locality(ccInfo.getLocality())
                    .region(ccInfo.getRegion())
                    .postalCode(ccInfo.getPostalCode())
                    .countryCodeAlpha2(ccInfo.getCountry())
                    .done()
                .options()
                    .makeDefault(true)
                    .done();

            Result<CreditCard> creditCardResult = gateway.creditCard().create(creditCardRequest);
            CreditCard customerCreditCard = creditCardResult.getTarget();
            if (!creditCardResult.isSuccess() || customerCreditCard == null)
                throwPurchaseException(creditCardResult);

            ccToken = customerCreditCard.getToken();

        } else {
            //get the default credit card
            ccToken = customer.getPaymentMethods().get(0).getToken();
        }

        return ccToken;
    }

    private void throwPurchaseException(Result<?> result)
    throws StoreException
    {
        StringBuilder errorMessage = new StringBuilder();

        ValidationErrors errors = result.getErrors();
        if (errors != null) {
            List<ValidationError> errorList = errors.getAllDeepValidationErrors();
            Iterator<ValidationError> errorListIt = errorList.iterator();
            while (errorListIt.hasNext()) {
                errorMessage.append(errorListIt.next().getMessage());
                if (errorListIt.hasNext())
                    errorMessage.append("; ");
            }
        }

        if (errorMessage.length() == 0) {
            String resultMessage = result.getMessage();
            if (resultMessage != null && !resultMessage.trim().isEmpty())
                throw new StoreException(resultMessage);
            else
                throw new StoreException("Credit card error");
        }
        throw new StoreException(errorMessage.toString());
    }

    public static void main(String[] args)
    throws StoreException
    {
        Item item1 = new Item(); item1.setPrice("1"); item1.setTitle("Daily Millionaire 1 Dollar");
        Item item5 = new Item(); item5.setPrice("5"); item5.setTitle("Daily Millionaire 5 Dollars");
        Item item10 = new Item(); item10.setPrice("10"); item10.setTitle("Daily Millionaire 10 Dollars");
        Item item20 = new Item(); item20.setPrice("20"); item20.setTitle("Daily Millionaire 20 Dollars");
        Item item50 = new Item(); item50.setPrice("50"); item50.setTitle("Daily Millionaire 50 Dollars");
        Item item100 = new Item(); item100.setPrice("100"); item100.setTitle("Daily Millionaire 100 Dollars");

        List<Item> items = Arrays.asList(item1, item5, item10, item20, item50, item100);

        String customerFeldon = "grimshaw_feldon_1972_05_24";
        String customerBB8 = "bb8_resistance_orange";
        String customerAndy = "grifith_andy_mayberry";
        String customerStarbuck = "starbuck_bsg_1977";
        String customerBruce = "bruce_k_grant_jr";

        List<String> customers = Arrays.asList(customerFeldon, customerBB8, customerAndy, customerStarbuck, customerBruce);

        TransactionCreator tc = new TransactionCreator();

        //tc.purchaseItem(tc._gatewaySandbox, customerStarbuck, tc._ccInfoSandbox, item1);
        tc.purchaseItem(tc._gatewayProduction, customerBruce, tc._ccInfoProduction, item10);

//        int totalTransactions = 5;
//        for (int i=0; i<totalTransactions; i++) {
//            String customer = customers.get(new Random().nextInt(customers.size()));
//            Item item = items.get(new Random().nextInt(items.size()));
//
//            System.out.println(MessageFormat.format("{0} -> {1}", customer, item.getTitle()));
//
//            tc.purchaseItem(tc._gatewaySandbox, customer, tc._ccInfoSandbox, item);
//        }
    }

}
