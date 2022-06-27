package tv.shout.tools.scratch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.util.Arrays;

import com.lambdaworks.crypto.SCrypt;
import com.meinc.commons.encryption.HexUtils;

public class PasswordResetter
{
    private static final char[] encode = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/".toCharArray();
    private static final int[]  decode = new int[128];
    private static final char   pad    = '=';

    static {
        Arrays.fill(decode, -1);
        for (int i = 0; i < encode.length; i++) {
            decode[encode[i]] = i;
        }
        decode[pad] = 0;
    }

    public String getEncryptedPassword(String plainTextPassword)
    throws Exception
    {
        byte[] salt = new byte[16];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(salt);

        String sha256OfPassword = HexUtils.stringToSha256HexString(plainTextPassword, true);

        int N = 4096;
        int r = 8;
        int p = 1;

        byte[] derived = SCrypt.scrypt(sha256OfPassword.getBytes("UTF-8"), salt, N, r, p, 32);

        String params = Long.toString(log2(N) << 16L | r << 8 | p, 16);

        StringBuilder sb = new StringBuilder((salt.length + derived.length) * 2);
        sb.append("$s0$").append(params).append('$');
        sb.append(encode(salt, encode, pad)).append('$');
        sb.append(encode(derived, encode, pad));

        return sb.toString();
    }

    private static int log2(int n) {
        int log = 0;
        if ((n & 0xffff0000 ) != 0) { n >>>= 16; log = 16; }
        if (n >= 256) { n >>>= 8; log += 8; }
        if (n >= 16 ) { n >>>= 4; log += 4; }
        if (n >= 4  ) { n >>>= 2; log += 2; }
        return log + (n >>> 1);
    }

    private char[] encode(byte[] src, char[] table, char pad) {
        int len = src.length;

        if (len == 0) return new char[0];

        int blocks = (len / 3) * 3;
        int chars  = ((len - 1) / 3 + 1) << 2;
        int tail   = len - blocks;
        if (pad == 0 && tail > 0) chars -= 3 - tail;

        char[] dst = new char[chars];
        int si = 0, di = 0;

        while (si < blocks) {
            int n = (src[si++] & 0xff) << 16 | (src[si++] & 0xff) << 8 | (src[si++] & 0xff);
            dst[di++] = table[(n >>> 18) & 0x3f];
            dst[di++] = table[(n >>> 12) & 0x3f];
            dst[di++] = table[(n >>>  6) & 0x3f];
            dst[di++] = table[n          & 0x3f];
        }

        if (tail > 0) {
            int n = (src[si] & 0xff) << 10;
            if (tail == 2) n |= (src[++si] & 0xff) << 2;

            dst[di++] = table[(n >>> 12) & 0x3f];
            dst[di++] = table[(n >>> 6)  & 0x3f];
            if (tail == 2) dst[di++] = table[n & 0x3f];

            if (pad != 0) {
                if (tail == 1) dst[di++] = pad;
                dst[di] = pad;
            }
        }

        return dst;
    }
    public static String getConsoleInput(String message)
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
        String plainTextPassword = getConsoleInput("password: ");
        String encryptedPassword = new PasswordResetter().getEncryptedPassword(plainTextPassword);
        System.out.println(MessageFormat.format("{0} -> {1}", plainTextPassword, encryptedPassword));
    }

}
