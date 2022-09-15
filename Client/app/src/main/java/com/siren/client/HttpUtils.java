package com.siren.client;

import android.util.Log;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

/**
 * okHttp请求工具
 * Created by Siren on 2022/9/7.
 */
public class HttpUtils {

    private final static String TAG = "HttpUtils";
    //安全传输层协议
    private static final String PROTOCOL = "TLS";
    // JKS/PKCS12
    private static final String KEY_KEYSTORE_TYPE = "PKCS12";

    /**
     * 忽略证书
     */
    public static OkHttpClient getClientWithoutCert() {
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .sslSocketFactory(getSocketFactory(), getX509TrustManager())
                .hostnameVerifier(getHostnameVerifier())
                .build();
    }

    /**
     * 校验证书
     */
    public static OkHttpClient getClientWithCert() {
        SSLSocketFactory socketFactory = getSocketFactory(AppContext.get().getResources().openRawResource(R.raw.server));
        return new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .sslSocketFactory(socketFactory, getX509TrustManager())
                .hostnameVerifier(getHostnameVerifier())
                .build();
    }

    /**
     * 获取证书信息，默认返回true
     */
    public static HostnameVerifier getHostnameVerifier() {
        return (hostname, sslSession) -> {
            Certificate[] localCertificates = new Certificate[0];
            try {
                //获取证书链中的所有证书
                localCertificates = sslSession.getPeerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }

            //打印所有证书内容
            for (Certificate c : localCertificates) {
                Log.d(TAG, c.toString());
            }
            return true;
        };
    }


    public static X509TrustManager getX509TrustManager() {
        X509TrustManager trustManager = null;
        try {
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init((KeyStore) null);
            TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
            if (trustManagers.length != 1 || !(trustManagers[0] instanceof X509TrustManager)) {
                throw new IllegalStateException("Unexpected default trust managers:" + Arrays.toString(trustManagers));
            }
            trustManager = (X509TrustManager) trustManagers[0];
        } catch (Exception e) {
            e.printStackTrace();
        }

        return trustManager;
    }

    public static SSLSocketFactory getSocketFactory() {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, getTrustManager(), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static SSLSocketFactory getSocketFactory(InputStream inputStream) {
        try {
            SSLContext sslContext = SSLContext.getInstance(PROTOCOL);
            sslContext.init(null, getTrustManagers(inputStream), new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 通过公钥证书获取TrustManager
     */
    private static TrustManager[] getTrustManagers(InputStream inputStream) throws Exception {
        TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore keyStore = KeyStore.getInstance(KEY_KEYSTORE_TYPE);
        //加载证书
        CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        Certificate ca = certificateFactory.generateCertificate(inputStream);
        keyStore.load(null, null);
        //设置公钥
        keyStore.setCertificateEntry("server", ca);
        trustManagerFactory.init(keyStore);
        TrustManager[] trustManagers = trustManagerFactory.getTrustManagers();
        return trustManagers;
    }

    /**
     * 创建TrustManager，信任所有服务器
     */
    private static TrustManager[] getTrustManager() {
        return new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[]{};
                    }
                }
        };
    }
} 