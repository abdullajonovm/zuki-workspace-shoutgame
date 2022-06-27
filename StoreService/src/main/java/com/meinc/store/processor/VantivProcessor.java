package com.meinc.store.processor;

import java.util.List;

import com.meinc.store.domain.Receipt.ReceiptType;

public class VantivProcessor
extends PaymentProcessor
{
    @Override
    List<ReceiptType> getTypes()
    {
        // TODO Auto-generated method stub
        return null;
    }

    //The Express API looks to be what will be needed to do account information (store/retrieve card info)
    //https://developer.vantiv.com/docs/DOC-1353



    //examples for various things (the only one which seems useful to our usecase is for purchasing:
    //https://developer.vantiv.com/docs/DOC-1223
    //https://developer.vantiv.com/docs/DOC-1232#jive_content_id_Java_example

    //top level starting point for docs:
    //https://developer.vantiv.com/community/ecommerce

    //using the java sdk
    //https://github.com/Vantiv/cnp-sdk-for-java
    //DEPRECATED?: https://github.com/Vantiv/litle-sdk-for-java
    //DEPRECATED?: save/purchase via token: https://github.com/Vantiv/litle-sdk-for-java/tree/11.0/samples/token/src/main/java

    //sandbox testing information
    //https://developer.vantiv.com/docs/DOC-1347

    //eprotect/vault/storing credit cards and using tokens
    //https://developer.vantiv.com/docs/DOC-1203

    /*
DM-54: Allow for the fulfillment of a CC transaction that saves the CC as a payment method at the same time via Vantiv.

NOTE: if this is the first payment method entered, it becomes the 'default'
NOTE: This must be called AFTER the client has contacted Vantiv and converted the credit card information into a card token

    URL:
        <collector>/store/vAddPaymentMethod

    PARAMS:
        cardToken (per Vantiv docs, the last 4 characters of this match the last 4 digits of the credit card number)
        cardType (string, VISA, Mastercard, Discover, etc)
        expMonth (2 character)
        expYear (2 character)

    RETURN:
        {"success":boolean}

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>
        duplicateCardToken

-----

DM-55: Allow for retrieving all payment methods where Vantiv CC's is among them (return card type and last four of card) from Vantiv.

    URL:
        <collector>/store/vGetPaymentMethods

    PARAMS:
        <none>

    RETURN:
        {
            "success": boolaen,
            "paymentMethods": [
                {
                    "cardToken": string[last 4 characters are the last 4 digits of the credit card],
                    "cardType": string,
                    "expMonth": string,
                    "expYear": string,
                    "default": boolean,
                    "valid": boolean
                }, ...
            ]
        }

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>

-----

DM-56: Allow to specify default payment method, the first payment method becomes the default.

    URL:
        <collector>/store/vSetDefaultPaymentMethod

    PARAMS:
        cardToken

    RETURN:
        {"success":boolean}

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>
        (in this case, invalidParam would mean the cardToken wasn't found)

-----

DM-58: Allow users to remove a payment method that is a Vantiv CC.

NOTE: presumably the client would have previously made a call to Vantiv to remove the payment method from their servers

    URL:
        <collector>/store/vRemovePaymentMethod

    PARAMS:
        cardToken

    RETURN:
        {"success":boolean}

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>
        (in this case, invalidParam would mean the cardToken wasn't found)

-----

DM-59: Allow users to update the expiration date of a Vantiv CC.

NOTE: presumably the client would have previously made a call to Vantiv to update the epiration date (and whatever else) on their servers

    URL:
        <collector>/store/vUpdatePaymentMethod

    PARAMS:
        cardToken
        expMonth
        expYear

    RETURN:
        {"success":boolean}

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>
        (in this case, invalidParam would mean the cardToken wasn't found)
        invalidExpMonth
        invalidExpYear

-----

DM-16: Server side implement Vantiv integration.

    URL:
        <collecltor>/store/vPurchaseItem

    PARAMS:
        cardToken
        itemUuid

    RETURN:
        {"success":boolean}

    ERRORS:
        <the usual invalidParam, missingRequiredParam, unexpectedError, etc>
        (in this case, invalidParam would mean the cardToken wasn't found OR the itemUuid wasn't found (check the 'message' param for additional details)
        (probably more failures that will be passed along from the Vantiv call, but i won't know those until i get into it)

-----

DATABASE:
    store.vantiv_cc
        subscriber_id
        card_token
        card_type
        exp_month
        exp_year
        is_default
        is_valid
     */
}
