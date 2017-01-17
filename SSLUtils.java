package com.letv.jr.lepay.common.util;

import com.letv.jr.lepay.PayCenter;
import com.letv.jr.lepay.R;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * @author: huajie
 * @version: 1.0
 * @date: 2016/11/1
 * @email: huajie@le.com
 * @Copyright (c) 2016. le.com Inc. All rights reserved.
 */
public class SSLUtils {
    public static SSLSocketFactory getSSLSocketFactory() {
        InputStream[] ins = new InputStream[]{PayCenter.get().getApplication().getResources().openRawResource(R.raw.le2016),
                PayCenter.get().getApplication().getResources().openRawResource(R.raw.le2017)};
        try {
            KeyStore keystore = KeyStore.getInstance(KeyStore.getDefaultType());
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            InputStream is = null;
            keystore.load(null, null);
            for (int index = 0; index < ins.length; index++) {
                is = ins[index];
                keystore.setCertificateEntry(String.valueOf(index), certificateFactory.generateCertificate(is));
                if (is != null) {
                    is.close();
                }
            }

            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keystore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
            return sslSocketFactory;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
