package example.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ko-aoki on 2016/01/17.
 */
public class HttpClientInsecureTlsExample2 {

    public static void main(String[] args) throws KeyManagementException {


//        System.setProperty("javax.net.ssl.trustStore","/Users/ko-aoki/.keystore");
//        System.setProperty("javax.net.debug","all");
//
        // Trust own CA and all self-signed certs
        SSLContext sslContext = null;
        try {
            sslContext = SSLContexts.custom()
                    .loadTrustMaterial(new TrustSelfSignedStrategy())
                    .build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        // Allow TLSv1 protocol only
        SSLConnectionSocketFactory sslSocketFactory
                = new SSLConnectionSocketFactory(
                sslContext,
                new String[]{"TLSv1"},
                null,
                SSLConnectionSocketFactory.getDefaultHostnameVerifier()) {

            protected void prepareSocket(SSLSocket socket) throws IOException {

                // Workaround to use different order of CipherSuites used by Java6 in order
                // to avoid the the problem of java7 "Could not generate DH keypair"
                String[] enabledCipherSuites = socket.getEnabledCipherSuites();

                // but to avoid hardcoding a new list, we just remove the entries
                // which cause the exception (via TrialAndError)
                List<String> asList = new ArrayList(Arrays.asList(enabledCipherSuites));

                // we identified the following entries causing the problems
                // "Could not generate DH keypair"
                // and "Caused by: java.security.InvalidAlgorithmParameterException: Prime size must be multiple of 64, and can only range from 512 to 1024 (inclusive)"
                asList.remove("TLS_DHE_RSA_WITH_AES_128_CBC_SHA");
                asList.remove("SSL_DHE_RSA_WITH_3DES_EDE_CBC_SHA");
                asList.remove("TLS_DHE_RSA_WITH_AES_256_CBC_SHA");

                String[] array = asList.toArray(new String[0]);
                socket.setEnabledCipherSuites(array);

            }
        };

        CloseableHttpClient httpClient = HttpClients.custom()
                .setSSLSocketFactory(sslSocketFactory)
                .build();

        try {
            //        HttpPost method = new HttpPost("http://192.168.56.101");
            HttpGet method = new HttpGet("https://192.168.56.101/test.html");

            try {
                CloseableHttpResponse response = httpClient.execute(method);
                System.out.println(response.getStatusLine());
                InputStream content = response.getEntity().getContent();
                BufferedReader reader = new BufferedReader(new InputStreamReader(content, "utf8"));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }

                reader.close();
                response.close();
                System.out.println(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } finally {
            try {
                httpClient.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
