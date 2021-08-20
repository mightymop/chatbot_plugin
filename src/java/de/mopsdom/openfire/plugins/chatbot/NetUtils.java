package de.mopsdom.openfire.plugins.chatbot;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;

public class NetUtils {
    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                @Override
                public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                }

                @Override
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new java.security.cert.X509Certificate[]{};
                }
            }
    };

    private static SSLSocketFactory getSSLFactory() throws NoSuchAlgorithmException, KeyManagementException {

        final SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
        // Create an ssl socket factory with our all-trusting manager
        final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String arg0, SSLSession arg1) {
                return true;
            }
        });

        return sslSocketFactory;
    }

    public static OkHttpClient getHttpClient(String  url)
    {
        OkHttpClient.Builder okBuilder = new OkHttpClient.Builder();

        if (url.startsWith("https://")) {
            try {
                okBuilder.sslSocketFactory(getSSLFactory(), (X509TrustManager) trustAllCerts[0]);
            } catch (Exception e) {
            }
            okBuilder.followSslRedirects(true);
            okBuilder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });
        }

        //okBuilder.authenticator(new NTLMAuthenticator(merkeuser, merkepw/*,domain,""*/));
        okBuilder.connectTimeout(10000, TimeUnit.MILLISECONDS);
        okBuilder.readTimeout(10000, TimeUnit.MILLISECONDS);
        okBuilder.followRedirects(true);

        return okBuilder.build();
    }
}
