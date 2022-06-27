package tv.shout.test.scrypt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import com.lambdaworks.crypto.SCrypt;

public class ScryptChecker
{
    private static final char[] CA = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[] IA = new int[256];
    static {
        Arrays.fill(IA, -1);
        for (int i = 0, iS = CA.length; i < iS; i++)
            IA[CA[i]] = i;
        IA['='] = 0;
    }

    private final static byte[] decodeFast(String s)
    {
        // Check special case
        int sLen = s.length();
        if (sLen == 0)
            return new byte[0];

        int sIx = 0, eIx = sLen - 1;    // Start and end index after trimming.

        // Trim illegal chars from start
        while (sIx < eIx && IA[s.charAt(sIx) & 0xff] < 0)
            sIx++;

        // Trim illegal chars from end
        while (eIx > 0 && IA[s.charAt(eIx) & 0xff] < 0)
            eIx--;

        // get the padding count (=) (0, 1 or 2)
        int pad = s.charAt(eIx) == '=' ? (s.charAt(eIx - 1) == '=' ? 2 : 1) : 0;  // Count '=' at end.
        int cCnt = eIx - sIx + 1;   // Content count including possible separators
        int sepCnt = sLen > 76 ? (s.charAt(76) == '\r' ? cCnt / 78 : 0) << 1 : 0;

        int len = ((cCnt - sepCnt) * 6 >> 3) - pad; // The number of decoded bytes
        byte[] dArr = new byte[len];       // Preallocate byte[] of exact length

        // Decode all but the last 0 - 2 bytes.
        int d = 0;
        for (int cc = 0, eLen = (len / 3) * 3; d < eLen;) {
            // Assemble three bytes into an int from four "valid" characters.
            int i = IA[s.charAt(sIx++)] << 18 | IA[s.charAt(sIx++)] << 12 | IA[s.charAt(sIx++)] << 6 | IA[s.charAt(sIx++)];

            // Add the bytes
            dArr[d++] = (byte) (i >> 16);
            dArr[d++] = (byte) (i >> 8);
            dArr[d++] = (byte) i;

            // If line separator, jump over it.
            if (sepCnt > 0 && ++cc == 19) {
                sIx += 2;
                cc = 0;
            }
        }

        if (d < len) {
            // Decode last 1-3 bytes (incl '=') into 1-3 bytes
            int i = 0;
            for (int j = 0; sIx <= eIx - pad; j++)
                i |= IA[s.charAt(sIx++)] << (18 - j * 6);

            for (int r = 16; d < len; r -= 8)
                dArr[d++] = (byte) (i >> r);
        }

        return dArr;
    }

    //this is a copy of the method in the encryption service
    private static byte[] base64StringToBytes(String value)
    {
        return decodeFast(value);
    }

    //this is a copy of the method in the encryption service
    private boolean scryptCheck(String password, String scryptHash)
    {
        try {
            String[] parts = scryptHash.split("\\$");

            if (parts.length != 5 || !parts[1].equals("s0")) {
                throw new IllegalArgumentException("Invalid hashed value");
            }

            long params = Long.parseLong(parts[2], 16);
            byte[] salt = base64StringToBytes(parts[3]);
            byte[] scryptHashBytes = base64StringToBytes(parts[4]);

            int N = (int) params >> 16 & 0xffff;
            int r = (int) params >>  8 & 0xff;
            int p = (int) params       & 0xff;

            byte[] scryptRehashBytes = SCrypt.scrypt(password.getBytes("UTF-8"), salt, 1<<N, r, p, scryptHashBytes.length);

            return Arrays.equals(scryptHashBytes, scryptRehashBytes);
        } catch (UnsupportedEncodingException | GeneralSecurityException e) {
            throw new IllegalStateException();
        }
    }

    private void checkPassword(String password, String passwordSha256Hash, List<String> scryptHashes)
    {
        System.out.println(MessageFormat.format("password: {0}, passwordSha256: {1}", password, passwordSha256Hash));
        scryptHashes.forEach(scryptHash -> {
            boolean valid = scryptCheck(passwordSha256Hash, scryptHash);
            System.out.println(MessageFormat.format("\tvalid: {0}, scryptHash: {1}", valid, scryptHash));
        });
    }

    private static String getConsoleInput(String message)
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        System.out.print(message);
        try {
            return br.readLine();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        ScryptChecker scryptChecker = new ScryptChecker();

        String password = getConsoleInput("password: ");
        String passwordSha256Hash = getConsoleInput("password sha256: ");
        String scryptHash = getConsoleInput("scrypt hash: ");

        scryptChecker.checkPassword(password, passwordSha256Hash, Arrays.asList(scryptHash));


//        //generated by Aiden's code
//        scryptChecker.checkPassword("foobars", "33698e11fcf2895d5ea3c3c3cc04abefc3a9595e7f9d2ee0f4d0bee3a04d6b2d",
//            Arrays.asList(
//                "$s0$c0801$anpjLmxmPXJmdm5ucHl1amZ8MXcpNHQuOy9ndSkjbDByKCtAICcibWAtM2NlenZpLTVkZn51Wz5xdDEydDg6bQ==$uB3oW6KsdIZSmOaPV5G4bSEa1ugMOv5vbRFr9DjDZWQ=",
//                "$s0$c0801$bix5bGAkayU8Il0hIi92eW09J3Q3fjgxbTZncnZ6YnVcQCYlamJhY3VyYz9pZ3k3aXM8dW40KnhfZylwcTF3Nw==$x2FbY6mbczFdzDM20km6XkGskKmpiGpFjtZHSIkNVUI=",
//                "$s0$c0801$ZHFjZHRvXCsud3Jbeno7dTUiZGxwYThqeidbOXl2LStmenN2ans1YW1le2woems1LDllYC9uLyolOzo0W2xqKQ==$KGthK4skNfE5Zjeq/uHwq79RDsKXcYELQ/CW+cbw8zE=",
//                "$s0$c0801$Z2cyIm5hfGIqaWdyJH58bGNtdzs4Pi9dfnRjX3YnMXQ6I3AqYTF4KjR0ZHI9IF0xd2Nmdmk0am91PGgzIiYodQ==$jdm3PwHzHLYRtjj/O7xTEo5ZrLRuoz/jhhKz4uCSi9M=",
//                "$s0$c0801$OSZ7cX4jdC9wai14JW5lYj87eXgiW2twYzYubj50ZG1mOWwrYjx7fW9qKi42aXswNnl1djx1b2luOGNeYXlkPg==$G6Rpb30ygSD8B7OHrNrw8GkZuCOqLXuGsEPUjYjNGag=",
//                "$s0$c0801$ZyZ5JzM8dmp1NXJsM3V9IzFyKWxbLnp8fnM7dWZvIyxuMjdofj54b19rN24xeHo7cHIqY3Zqfms1P1xsNnYjdQ==$go6Z6gSrUG0d09+9flYsiwikRiwsJK6mGpCGFlxmTDU=",
//                "$s0$c0801$Pn5fcXN6JDZcdGV6aXE1eGxzYVwtc28iZzlkLCB0P3d1by01ZWlsZWNgJSdzb3Btcz5bLDZmJTx8ciMzZmtjfg==$HX59Wd0706mvwuLFvHjXgOvnWyocAT6jj8GVN5lWfNY=",
//                "$s0$c0801$aGR3K3l2ZDl3a302IzNjJyRgY2cvXy91Ij56ND0zY2Itfmhjdjs0ayImOEBuY352ZXhAPXd1JXlyaXZ2X3cxPg==$6qWX5RBRGYG+v/Itgr3P1uv3vrvOtiylytpMuK4IPd8=",
//                "$s0$c0801$ZnVzaGc5MDV0fnJ7OSJsYzszbit2XDh2entlb219eHQlNmMrO3tzJTc1NnBjOmM+aSN5fms5JUB6dnV9K3klZQ==$Xjbg5GEH7n1oNacHhhXeU9b+oa6UjUs/d84CQld0Oeg=",
//                "$s0$c0801$ZCVtcl5kaHJ1QG1ye2JAbnBrZmZwdixtYGJ0MFsiLzJleWddPHBqc14tXVtjPHc3cGlmemtiem42IGlub2g6OA==$LbpAZIPkl5bJPSNYHUxaEgKAiN14+yjBh+1ioK4aOOk=",
//                "$s0$c0801$Z24iOHI6eX0/aWlveG1oOnM+KSY9NmdgMmdjbDN6ZGIlM3dyPS90dT00ZDpzZ2s7KDdbL2VfMG4+cTJpMHEhag==$0GpzSJUeREt1v9YEM7nkibaSkSBdEvc6iF3g++AM2Bc=",
//                "$s0$c0801$czkvbydzaz1sQFxzcSZ3aCduc3JmOGBjKDleMGZyOXUmYjN3a2d2LHViYylvdnopeHQ2YTlqQHl5XmNsMUA1fQ==$K/Bio0EQlzNDOljFeMa4flQH/ArClzWlftGfTqzGa7c=",
//                "$s0$c0801$JF11NF56cGlwdDQ8MmJfNStpJisiY194eHBbIjIgdXJkeGhlJGE+ci9sLG01aWxucVsvKSY8aGUhfXM2fjx3KQ==$i2h1w5ilwlnnqVBKGkvMHKav2kmzoA3Sn4HSATMxYhk=",
//                "$s0$c0801$cTJsbnM+YngzdWFbczhcbnQoOm07NG9hZ3A9d29nIWUoLFtuZVt1dCg0Mm44aXhyW2I5cCxkW0BtJ3liJW8iaQ==$2A1yGF3c1a8QxBFHDxYoM8P4pSAWtq8HE7gAMCJFJ3c=",
//                "$s0$c0801$dGclPCFrZWxvJ254Z3BmL31qKytfeChnIGhwaXk6a2xpdH5nLW9yemlcOmlvPjF3fXdyNyptcT18bmtvJnc6fA==$pfigWWmznXDrzWjsfnT4S85oEE8+INGKhVHSDO+MdnY="
//            )
//        );
//
//        scryptChecker.checkPassword("bxgrant", "b3010d2b68fb5d323a74d0ae131b0f7f4f08b47f456f177909d49ae2a30642f8",
//            Arrays.asList(
//                "$s0$c0801$c344Zm1qKzdudlthaV5wamZuISEuZ2QsLiZocWl+JTEvXXJidGV8ZGsqcCkqZD91YXQ9eDJnaGxveWkoYHZ4Mw==$eb70IB9+75iNkzgoRDo/xuD+c6AWly1wXTl908HW4Sc=",
//                "$s0$c0801$cFthIzpsPDlwdGc/M2Bhcn1rY2srW1x1LHF2bi1wZmcvXF9kLWxvIiAxNl5tQDh0eyQgPT8ueG5tbit7eTRqag==$obvDV1rk/fRlxPRpbRqDdgwrN0+CsOPXxFlwrR2Bt/c=",
//                "$s0$c0801$bWVkaicjYCVyeXR3JHtueTo/dnskLylxPTEqXi18bSt+fSB5NSQuPCRlfiBzOC1hLDxpYmVddHt5fSYgezFncQ==$KjlMNK9qbGIW2LHg5zkh8qZNqa0GVZ4piXspHalK8bk=",
//                "$s0$c0801$YjtpdD96PD1edV9hMXVhYzxAX21samFAY2FlaTpleD5jNV9rZTN+YmxxL348bDNweDAoNCltay09eXFucnA4NA==$d5XVbL0CpP75ae4DRrzeviTCcZ1IWtMv8tCz8ff8uCs=",
//                "$s0$c0801$XmdybmB3MnFpMGxwMUA7ZHQtdiZecm1fXnJ+OGgmdm9jXWluNjkiZ2BuZis0Y0AvIiJ4LGt1Zl89MzApcG1oKg==$jjLMBxHGBWvxC1kNa1FaM/6+Bi2AhP1cnLT6COU1KU8=",
//                "$s0$c0801$cW5jbmRpcyswJWtsM3AhKWZhbndieiNlKXFyLV1kIWopZFx9XmRva3FtYXh1az1dZHRwW2liLCJkemJcc2t+OQ==$HgpzNZqmqUWPn5lu0WRTRCCaOPXeYIpxYnl0OU769Gg=",
//                "$s0$c0801$PTF7N3AqNDxfZFxpJiFgI3F5aWB7JD5hNnNocTchO3tqZD1sNTYjKV5iKnZmfWxsb2p2aDBcZz8ldGdwXn0lIQ==$x8iRk7KDjfZIYlFiqFITW/AcWUFiL8FVuDLQ+V42/tw=",
//                "$s0$c0801$XGVjXGhebSJkIHdpem4xaGohe2lydD1veSIiai15JnRvcDBlZDVqdnM2ZnUnLHZpeV5teWN5bCxpXjNmKnNyfg==$HMfCzjlJSHzs/RT3uADxk16/aCoGQJvH422WrYdoNFM=",
//                "$s0$c0801$dW8/I2xcd2luOFx5YnhffHdifiNzYGl3IHZgdGNhdiEvMHcwMXl+LC1sYyxbaXlvZTA0cX0pOl9AXnxkYXhcYw==$7HoN9AB03Ds5tzWfnaxgJQ2sUk4KxHCABrnWVGNOFzg=",
//                "$s0$c0801$d350dX1Adzdqd3pxbHZ2PHEzeGt9IXV2bnZdXmhdYzN1eiY0ZDI/eTJmdnwjcEB7fWx+Z30iXH1sd3dhdTtxQA==$ByCUjwtd0CTvwNLwP4XkTAYaBn7xbevyXkMh9jgS/tk=",
//                "$s0$c0801$X3Y9MD1yZyBmdGl3OXhreVw1Ym01enFkPSJ4fXZmJ3c3azsiN3k4fXt7MHRuKj9maT5fajtcNWJuIG4hKCR2bA==$EpqHdF9rQyjD6EUFyDAb1FPGjx0fw6qZYHHTn++ChlU=",
//                "$s0$c0801$Y2ZrZ295em5sNkBjXGh5dmZyJH14aChlJWtuJmlcd2YqXCRAYS4hdCQ8ZG18JiIkcWMxeC1kMnd4KHdueTFkIA==$Xxoh/MssSrJFuo0heqmvXLrtnXCGUlg/04iXZMQDVMk=",
//                "$s0$c0801$cy94MCR8andndDk+ajxjbGhiXXFlYncobihubCc9Yl5xZylsYSo7ZHo7ez1jJiJldDxxdyNucSJzaGduaDV0Xg==$/E2pJlcqYRmG9dFJGAdA9ReHOsy6JHxXCovzvq24KrQ=",
//                "$s0$c0801$dWJ5c3ojaWlfcztfZGFnQDVoIzsufmxxPS5hdzFzciNkP2M3M2puZV1ucmFzXnlgW25zaGlhZTg7JGgwcjk5Zg==$CAV7/7OKDEc6HZJ/jyEDCpt82NVpyDehsRDesa/fMxE=",
//                "$s0$c0801$dS9zdSZ4cnVlQCZzcHsgJ2Uod242Ki86cm9+d3giYWtvOnh+Nm8sYmlrOmk1eXVrO2dzaTNlW2J4eXpmMW98bA==$4V6t6hgAWtBw7MfBHSGK6fFOpfZ5XF4p3+hYiS30gPM="
//            )
//        );
//
//        scryptChecker.checkPassword("a1d3nGr@nt!", "f2f35c35774b967fe615345e46ea122f12ca7024729e02c409caed91971d3803",
//            Arrays.asList(
//                "$s0$c0801$N3oteDB5djV2YG83KHIybCxgei18e2Qje3YoMXtbfXUyImc7Y209cTkxfCcvN3dhOzsmeGgiO3c+X2djZ2QrYQ==$8/30erg1cdhQg09YC0Af4LuId5iM4Q7vNJVScGtSpZY=",
//                "$s0$c0801$a3J0MnQlZSM5IGlzeyBkYW1sKF04bWoxc2snLid8a2x3fV5xYyxreTF8Ly1wcmd4MGF6XStheFx7bGRmZSJyZA==$vffda/tuqUEM8bJYWofaawIIisRsCY4TvfwP0YO7f4g=",
//                "$s0$c0801$Z31tQGYkcWp5YiNvbG9yaXNlOjdoN2t6IydpdkA/ayBeNmh6NW4tcmdqYGEqYkBqYntoPDgnYSF5YG9xQGRwLA==$OWIvB58H1T0ChzPaD/8GyYdYD2AszR50qeq29TjJXrc=",
//                "$s0$c0801$W2N0em00IikofHhsYiY1a3E1al48MDZkeSIlc2ZyOy1oZjx0cHBwJyVjXGckPTRqezUwOzhpbWsqe3VAYVtmOw==$EX+3s0/n4pxvGJYfY3dYa8MrLdTnTenjKV/GL5QCLDk=",
//                "$s0$c0801$M20rNTpqbnZpKHhdeThmKC9ibyhrbHxgPSVrbS5tZiloYnxxbnJ0aXkwLFwqLGkua3cpQGtueCdwOjcvXG4rQA==$+SPoFPCuILZCiuIUAY0A+4zfhTf3F/TjENb/Hm5dmDI=",
//                "$s0$c0801$cS96YGo6ejcvdnplYXJgOmNhXGNpI2h2ZGciIV4rI2R4XmE3czwrL2Jvc2EuZ2goYHJ3anVpdCF2OF46ZjQ8cg==$+JZ0rqXIgC+qRLsS3TdlUVViHzO9Um6dHQ3bzTnUx3E=",
//                "$s0$c0801$LT5rJWshaW5eLXMnanEibm1ocmc0I2giIXpwemYpXXs6ZGcncHM4MD1iIVxiJnk8NC8hP3tqOHJyKXRrc3pqdw==$GOUbKV4/8lebn7q+EcpTuQ05HsHdqWKHLN9ba2zPfK0=",
//                "$s0$c0801$fiVrdjchfSl3YiogL2psJSZ6anknKSBrbmIhdDJceDVqPT8sM3p4PnEudWhsZmghayJ1Ll01OWF1YHIgJj90LA==$fSqAMfpUnFZ4UmRgCdzdKZ+NpXua4iUhHWtWUd2l4Qk=",
//                "$s0$c0801$NHV5ZipwZCNiYWg9fWEibl93ZW9cbHknfGAwJmI3dGtoZyR2Y2Qzc2xldjloYTc6MDpvezBvJ3trPTwtdjkuZg==$Y1UhzFpVdiXCWjuCtTSPEHSwQD+/xg4kOYLDIMMWEMY=",
//                "$s0$c0801$bzJpcHV6Njk8M3JvITIqP3RibGE5fTV4MWwgP3Z0ZGhxIltzfDReK200e3Fjb2kxb11xY15yKTwpZGNrbSRxQA==$bu4lX4p/MHSP4YhD38yh8V+DIWcuii8mRpwUmYjv0HY=",
//                "$s0$c0801$JXJ8aTRwKHVwbm8gNGU3Nz4haD09O3hyayx5NGp2aGRmamo7bWZ7ez18Xn5xb2ZuOm1hZzZhc34jZlx3Zn59eg==$DOO7/rC2Pp+tnbEfEgSRfOpUEopkBUUN18oldeLzHx4=",
//                "$s0$c0801$KGZ1fXlvcHI9cHNcbltwcyBmZ2x6ZGh7d3hrb3g0dmtAYT4/YHNtX25vdl1pOTskaj8jbm1vYGtyJV8ifkB1cA==$JGSZBMi3UM3eX5FEdZnZZzz/1XfGHqx/6UOGIxcPsxU=",
//                "$s0$c0801$enk9MDsmeHRdYSw9bnorM1tnKi1sPWdmamphOmJldmw6W2EyQDZ2eCJvenBdYipbay9hJSxjZ3x+JiFxX2lzOA==$fpkyVvtIjFPpZPr0ipXDUPCss1qRfHDiI7VHwjnt0i0=",
//                "$s0$c0801$eGltY2l1LXh5K2cjbGR1Z20tZWZ1LmIncTVgb3V5eiVfJSY7amBtd2ppPHt2ZTVsfXp2Lz1cIDpqaSRkPjwgdA==$xQ2ddTr7APIk2JDgqRko8PLN5TKUIpJrNdcphOZ+H4g=",
//                "$s0$c0801$IWk8XSQwJWFxbnoxdmJ9d2VmJ2E2YWwyNXZ5YHBkaj4gPj8lc2EvPXpsPjQja3ZtIFtqMzU/bGt9YiZlcTFtXg==$I4vWADhaHfnkUFqmrmzQnQnUtglFH4xA5eaqax7z1x4="
//            )
//        );
//
//        scryptChecker.checkPassword("bxbxgrant", "945861af4b3d032131f484c59dfd74963d639c23f0795cc33b8320d3acebf2e1",
//            Arrays.asList(
//                "$s0$c0801$XWMwK2MyaysjY2tgXXF1ZC5sN2lsZF8wfSlqIHskd2I9eGp4Mn5pbWxkaHZrI2JubD5sIDs3eiJvdmpkJ3RuXQ==$OjwOztCHFYO4UMITAh whioils8x/gC8hkKLIRf1LLI=",
//                "$s0$c0801$NXp4eWR+JiBhanRoO294bT5oYHNlPF53PXkyLndwamplZDA4XjVAdWFnaTE0fXlkYDAhJnBbOmM1Zmt5Y2VhfQ==$g0Q6/JwtAp9m15dno9c+v5DDZxykhJM9d5OnbYr+gH8=",
//                "$s0$c0801$O29vKzw3cC5tN2Z4PHBddyY9ZS1saHtjWyt2c2ZiJzdnKnpzdCg9NzhzKnBpI2cmMml0cn1cJ2dtZmNrdGQ+IA==$FiPFy4AaJ7y1QqKiUqhDRnLeuhM4Y5U1QgHo1RV7nH0=",
//                "$s0$c0801$NjgkaGpldWlwZ3NvcnJ2Y3E5c212ZmtcMyl5eHJiO3J5ZSU0b2RxbzE+YF4iaSAhbnVrX3ggdGFoenFrImNvbw==$ratYgQzB43j6i7MyA5JR0w+Py5/vEG9qxCEld6+LplM=",
//                "$s0$c0801$LnJ2YWdqeWohNy89bGR5cnI9Z3QyPm1rcHRicTcmbHhqa19tO250aGMnfTA6dHwxcGZzajFsdylAZzZifW4tLQ==$AASS23Gy3mLalgDlDf427LlySpKeePOL4gbuMv6xV4Q=",
//                "$s0$c0801$anpfdi05cyJoXGlkeH5kaSx2aTooQGM3XmNrO3VydGIkajZfeHs8IHRrZSJmcWZnL2R0IS06OihkK2F2Z3o5ZQ==$LA1+8fP5dvRKCqhxokeFE4/WPedaZPnP+D7nKDOGGHg=",
//                "$s0$c0801$OWkpbnhqJTJqJSk6a3BzKXo/NTFvcjBjIVtsKWdwZz51cCgzKDkjZmNqY3olOHl2fW80ajZwc2ZveWlyaD94PQ==$hZJ+ahnS02fBGjphPpA5vW2rXAEm8BKUsP8Pc9odcKs=",
//                "$s0$c0801$dW52ImBxJXUkZHIpci98YyViPzV+cWF+LjZnMnB0d15qb3goWzxidmstdDt4a2BrNiYzcidvYj0wcWsmPWJuWw==$XCxpXva9MfsAMfSqD2+1n00uTT1Ou+BoB43zMqirius=",
//                "$s0$c0801$MG4xa3lxZS0mezxwIXNjLG94Yz52enh0b34mclwwYTJ1eCg7d15qYjh0J2BccjowL3VbNDtvXjEvbTBuc3o7Mw==$Zfz57HRV1lml7lxuwyYd5gQCnFE1anbtXjFU1d6eMNA=",
//                "$s0$c0801$bmtzbyAybDhqZGphLXQmYW93LWQhdiR+LSd3YnlibywgY2wiKWFAd2pucmB2NXJcPy5rMWFlYmI+YnhbfGw0aA==$AceBqOHidvlBooSNhPxRA2x9Txq5wCGto5YqaShAtOM=",
//                "$s0$c0801$O2JAayVsNW9mI3dyaHV+andsfipnKi16L3B6a3pnaShyMDwiXFw2Yy1jZT4uQHtiIzN6YXRoeWRhcCtydXQrZA==$JPMz8DyhfIQcsbnScD625jfp4k6A00y9tcx/GNn08aE=",
//                "$s0$c0801$M2gxeW1da2dyMTclOjt0bWlsOHBhXGA3eHI+bGVccnBgPCUvKHx1aCI4Il8qaGZoeHwpJzZwPHh9ZW5hZmpbfQ==$WxPCNsuPMQNeJ7SzVnTy28cX8rZdOq21ITvKwnJwZkU=",
//                "$s0$c0801$dyJzYml1dyBzaW0uLDxqc2Y6JnVldjtdZSw7dXJ3Ll1kcmtfenRkajIzd3skdHZ6bWprdX07b2NqMXQieCQnKA==$aQ/I1gVTa59H2y8oi8/9kxJw4j9Ll7zyZhWD2jom/w8=",
//                "$s0$c0801$YGVddyhybmZ1aDVxLmt0NjcwO2QmJXQ2O2wob29lLy0oZTxnMTF4eGZkfV92cGxhYioqMn5mZ341XHhuWyElaA==$dQRw+PdmbbH42rxCKuyNUpgEDoTJiutn668VuSo2iKw=",
//                "$s0$c0801$MWMqYT8/Yl44KCp1X2sjei88I2ZlaWxtcXZbeCFybjVddCB4MmhiNm4reXVnZnU7Zm9xemFyJ19wa2che3AyLg==$IFenmKXwxc1M++wf7LEeK2yd1s10xioQSuUYbmUZWfk=",
//                "$s0$c0801$LjZzd0B5MmNyLTsnckBqcWFoMypyZSY9ZTV3ZzJmcGlzI3VkI3hjfmctZiMoWy5yZHV3KEBdO30ldXh1cyZyZQ==$QdbGT6ybcP06Oh4598XHkXZzxzoWALUv5QwnNKRT73M="
//            )
//        );


//        scryptChecker.checkPassword("", "",
//            Arrays.asList(
//            )
//        );


    }

}
