package com.arvind.quark.util;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Encryption
{
    private String encryptionKey;

    private static final String ALGORITHM = "AES";

    public Encryption(String key)
    {
        this.encryptionKey = key+"0000";
    }

    public  String encrypt(String message) throws Exception
    {
        String salt = encryptionKey;
        SecretKeySpec key = new SecretKeySpec(salt.getBytes(), "AES");
        Cipher c = Cipher.getInstance("AES");
        c.init(Cipher.ENCRYPT_MODE, key);
        byte[] encVal = c.doFinal(message.getBytes());
        return android.util.Base64.encodeToString(encVal, android.util.Base64.DEFAULT);
    }

    public  String decrypt(String message) throws Exception
    {
        String salt = this.encryptionKey;
        Cipher c = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(salt.getBytes(), "AES");
        c.init(Cipher.DECRYPT_MODE, key);
        byte[] decordedValue = android.util.Base64.decode(message.getBytes(), android.util.Base64.DEFAULT);
        byte[] decValue = c.doFinal(decordedValue);
        return new String(decValue);
    }
}
