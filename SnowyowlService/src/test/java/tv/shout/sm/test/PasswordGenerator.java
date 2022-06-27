package tv.shout.sm.test;

import com.meinc.commons.encryption.EncryptionService;
import com.meinc.commons.encryption.HexUtils;

import tv.shout.sm.db.BaseDbSupport;

public class PasswordGenerator
{

    public static void main(String[] args)
    {
        String password = BaseDbSupport.getConsoleInput("password: ");
        String sha256OfPassword = HexUtils.stringToSha256HexString(password, true);
        String scryptOfSha256OfPassword = new EncryptionService().scryptEncode(sha256OfPassword);

        System.out.println("password: " + password);
        System.out.println("sha256 of password: " + sha256OfPassword);
        System.out.println("scrypt of sha256 of password: " + scryptOfSha256OfPassword);
    }

}
