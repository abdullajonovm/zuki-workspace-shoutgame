package com.shawker.test.enums;

import com.shawker.test.enums.NetworkManager.ERROR_TYPE;
import com.shawker.test.enums.NetworkManager.TYPE;

public class Client
implements DataReceiver<NetworkManager.TYPE, NetworkManager.ERROR_TYPE>
{
    private NetworkManager _net = new NetworkManager();

    @Override
    public void onSuccess(TYPE type, Object payload)
    {
        System.out.println("SUCCESS, type: " + type + ", payload: " + payload);
    }

    @Override
    public void onFailure(TYPE type, ERROR_TYPE errorType, int httpCode)
    {
        System.out.println("ERROR, type: " + type);

        switch (errorType)
        {
            case IOEXCEPTION:
                System.out.println("IOEXCEPTION: " + httpCode);
                break;

            case JSONPARSEEXCEPTION:
                System.out.println("JSONERROR: " + httpCode);
                break;

            default:
                System.out.println("UNKNOWN: " + httpCode);
                break;
        }
    }

    public void getDetails()
    {
        _net.doGet(this, NetworkManager.TYPE.GET_DETAILS, 0);
    }

    public static void main(String[] args)
    {
        Client client = new Client();
        client.getDetails();
    }

}
