package com.meinc.identity.helper;

public class Util
{
    public static String makeNicknameValid(String nickname) 
    {
        return nickname.replaceAll("[^A-Za-z0-9_\\-.]", "_");
    }

}
