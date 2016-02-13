package example.http;

import javax.net.ssl.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.cert.X509Certificate;

/**
 * Created by ko-aoki on 2016/01/19.
 */
public class UrlConnectionInsecureExample {

    public static void main(String[] args) {

        System.out.println(System.getProperty("java.version"));
        System.out.println(System.getProperty("https.protocols"));
        System.setProperty("javax.net.debug","all");

        class LooseTrustManager implements X509TrustManager {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }
        }
        class LooseHostnameVerifier implements HostnameVerifier {
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        }
        URL url;
        HttpsURLConnection con = null;
        try {
            url = new URL("https://192.168.56.101/test.html");
            con = (HttpsURLConnection) url.openConnection();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 証明書の検証をしない
        // sun.security.validator.ValidatorException: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
        SSLContext sslContext;
        try {
//            sslContext = SSLContext.getInstance("SSL");
            sslContext = SSLContext.getInstance("tls");
            sslContext.init(null,
                    new X509TrustManager[]{new LooseTrustManager()},
                    new SecureRandom());
            con.setSSLSocketFactory(sslContext.getSocketFactory());

            con.setHostnameVerifier(new LooseHostnameVerifier());

            con.setRequestMethod("GET");
            con.connect();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "utf8"));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            con.disconnect();

            System.out.println(sb.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
