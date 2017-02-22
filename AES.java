package com.allen.jr.testjni;

import android.os.Bundle;
import android.util.Log;

import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * 功能说明： </br>
 *
 * @author: zhangzhenzhong
 * @version: 1.0
 * @date: 2017/2/22
 * @Copyright (c) 2017. zhangzhenzhong Inc. All rights reserved.
 */


/*      调用方式
        String masterPassword = "a";
        String originalText = "0123456789";
        try {
        String encryptingCode = AES.encrypt(masterPassword,originalText);
        System.out.println("加密结果为 " + encryptingCode);
        Log.i("加密结果为 ",encryptingCode);
        String decryptingCode = AES.decrypt(masterPassword, encryptingCode);
        System.out.println("解密结果为 " + decryptingCode);
        Log.i("解密结果",decryptingCode);
        } catch (Exception e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        }*/
public class AES {
    /**
     * AES加密
     * @param seed 为密钥
     * @param cleartext 需要加密的内容
     * @return
     * @throws Exception
     */
    public static String encrypt(String seed, String cleartext) throws Exception {
        //对密钥进行编码
        byte[] rawKey = getRawKey(seed.getBytes());
        //加密数据
        byte[] result = encrypt(rawKey, cleartext.getBytes());
        return toHex(result);
    }

    /**
     * AES解密
     * @param seed 为密钥
     * @param encrypted 需要解密的内容
     * @return
     * @throws Exception
     */
    public static String decrypt(String seed, String encrypted) throws Exception {
        //对密钥进行编码
        byte[] rawKey = getRawKey(seed.getBytes());
        byte[] enc = toByte(encrypted);
        byte[] result = decrypt(rawKey, enc);
        return new String(result);
    }

    /**
     * 对密钥进行编码
     * @param seed
     * @return
     * @throws Exception
     */
    private static byte[] getRawKey(byte[] seed) throws Exception {
        //获取密匙生成器
        KeyGenerator kgen = KeyGenerator.getInstance("AES");
        SecureRandom sr = SecureRandom.getInstance("SHA1PRNG", "Crypto");
        sr.setSeed(seed);
        //生成128位的AES密码生成器
        kgen.init(128, sr); // 192 and 256 bits may not be available
        //生成密匙
        SecretKey skey = kgen.generateKey();
        //编码格式
        byte[] raw = skey.getEncoded();
        return raw;
    }
    /**
     * 加密
     * @param raw
     * @param clear
     * @return
     * @throws Exception
     */
    private static byte[] encrypt(byte[] raw, byte[] clear) throws Exception {
        //生成一组扩展密钥，并放入一组数组之中
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //用ENCRYPT_MODE模式，用skeySpec密码组，生成AES解密方法
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        //得到解密数据
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }
    /**
     * 解密
     * @param raw
     * @param encrypted
     * @return
     * @throws Exception
     */
    private static byte[] decrypt(byte[] raw, byte[] encrypted) throws Exception {
        //生成一组扩展密钥，并放入数组之中
        SecretKeySpec skeySpec = new SecretKeySpec(raw, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        //用ENCRYPT_MODE模式，用skeySpec密码组，生成AES解密方法
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        //得到解密数据
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    /**
     * 将十进制数转化为十六进制
     * @param txt
     * @return
     */
    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }
    /**
     * 将十六进制字符串转化为十进制字符串
     * @param hex
     * @return
     */
    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }
    /**
     * 将十六进制字符串转化为十进制字符数组
     * @param hexString
     * @return
     */
    public static byte[] toByte(String hexString) {
        int len = hexString.length()/2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2*i, 2*i+2), 16).byteValue();
        return result;
    }
    /**
     * 将十进制字节数组转化为十六进制字符串
     * @param buf
     * @return
     */
    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2*buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }
    private final static String HEX = "0123456789ABCDEF";
    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(HEX.charAt((b>>4)&0x0f)).append(HEX.charAt(b&0x0f));
    }
}
