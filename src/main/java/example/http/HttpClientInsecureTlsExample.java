package example.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

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
public class HttpClientInsecureTlsExample {

    public static void main(String[] args) throws KeyManagementException {


//        System.setProperty("javax.net.ssl.trustStore","/Users/ko-aoki/.keystore");
//        System.setProperty("javax.net.debug","all");
//
        HttpClientBuilder builder = HttpClientBuilder.create();

        // setup a Trust Strategy that allows all certificates.
        //
        SSLContext sslContext = null;

        try {
            sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                    return true;
                }
            }).build();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        builder.setSSLContext(sslContext);

        // don't check Hostnames, either.
        //      -- use SSLConnectionSocketFactory.getDefaultHostnameVerifier(), if you don't want to weaken
        HostnameVerifier hostnameVerifier = new NoopHostnameVerifier();

        // here's the special part:
        //      -- need to create an SSL Socket Factory, to use our weakened "trust strategy";
        //      -- and create a Registry, to register it.
        //
//        SSLConnectionSocketFactory sslSocketFactory
//                = new SSLConnectionSocketFactory(sslContext, hostnameVerifier);
        SSLConnectionSocketFactory sslSocketFactory
                = new SSLConnectionSocketFactory(
                    sslContext,
                    new String[] {"TLSv1"},
                    null,
                    hostnameVerifier) {
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

            };

        };
        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslSocketFactory)
                .build();

        // now, we create connection-manager using our Registry.
        //      -- allows multi-threaded use
        PoolingHttpClientConnectionManager connMgr = new PoolingHttpClientConnectionManager( socketFactoryRegistry);
        builder.setConnectionManager(connMgr);

        // finally, build the HttpClient;
        //      -- done!
        HttpClient httpClient = builder.build();

        //        HttpPost method = new HttpPost("http://192.168.56.101");
        HttpGet method = new HttpGet("https://192.168.56.101/test.html");

        try {
            HttpResponse response = httpClient.execute(method);
            System.out.println(response.getStatusLine());
            InputStream content = response.getEntity().getContent();
            BufferedReader reader = new BufferedReader(new InputStreamReader(content, "utf8"));

            String line;
            StringBuilder sb = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }

            reader.close();
            System.out.println(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
