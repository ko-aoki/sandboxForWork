package example.http;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ko-aoki on 2016/01/19.
 */
public class HttpClientExample {

    public static void main(String[] args) {

        System.setProperty("javax.net.ssl.trustStore","/Users/ko-aoki/.keystore");
        System.setProperty("javax.net.debug","all");

        HttpClientBuilder b = HttpClientBuilder.create();
        HttpClient httpClient = b.build();
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
