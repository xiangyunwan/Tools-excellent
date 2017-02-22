package com.allen.jr.testjni;

/**
 * 功能说明： </br>
 *
 * @author: zhangzhenzhong
 * @version: 1.0
 * @date: 2017/2/22
 * @Copyright (c) 2017. zhangzhenzhong Inc. All rights reserved.
 */
import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/*      调用方式
        String key = "12345678";
        String text = "12345678678768";
        String result1 = null;
        String result2=null;
        try {
        result1 = DES.encryptDES(text,key);
        result2 = DES.decryptDES(result1, key);
        System.out.println(result1);//加密文
        System.out.println(result2);//解密文
        } catch (Exception e) {
        e.printStackTrace();
        }*/




public class DES {
    //初始化向量，随意填充
    private static byte[] iv = {1,2,3,4,5,6,7,8};
    /**
     * DES加密
     * @param encryptString 为原文
     * @param encryptKey 为密钥
     * @return
     * @throws Exception
     */
    public static String encryptDES(String encryptString, String encryptKey) throws Exception {
        //实例化IvParameterSpec对象，使用指定的初始化向量
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        //实例化SecretKeySpec类，根据字节数组来构造SecretKey
        SecretKeySpec key = new SecretKeySpec(encryptKey.getBytes(), "DES");
        //创建密码器
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        //用密钥初始化Cipher对象
        cipher.init(Cipher.ENCRYPT_MODE, key, zeroIv);
        //执行加密操作
        byte[] encryptedData = cipher.doFinal(encryptString.getBytes());
        return JrBase64.encode(encryptedData);
    }

    /**
     * DES解密
     * @param decryptString 为原文
     * @param decryptKey 为密钥
     * @return
     * @throws Exception
     */
    public static String decryptDES(String decryptString, String decryptKey) throws Exception {
        //先使用Base64解密
        byte[] byteMi = new JrBase64().decode(decryptString);
        //实例化IvParameterSpec对象，使其指向初始化向量
        IvParameterSpec zeroIv = new IvParameterSpec(iv);
        //实例化SecretKeySpec类，根据字节数组来构造SecretKey
        SecretKeySpec key = new SecretKeySpec(decryptKey.getBytes(), "DES");
        //创建密码器
        Cipher cipher = Cipher.getInstance("DES/CBC/PKCS5Padding");
        //用密钥初始化Cipher对象
        cipher.init(Cipher.DECRYPT_MODE, key, zeroIv);
        //获取解密的数据
        byte decryptedData[] = cipher.doFinal(byteMi);
        //解密数据转化成字符串输出
        return new String(decryptedData);
    }
}