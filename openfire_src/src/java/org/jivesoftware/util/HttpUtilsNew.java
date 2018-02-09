package org.jivesoftware.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author hcq
 * @create 2018-01-26 下午 6:04
 **/

public class HttpUtilsNew {
    private static final Logger Log = LoggerFactory.getLogger(HttpUtilsNew.class);

    private static String executeDefault(HttpUriRequest request) throws IOException {
        String respContent = null;
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse resp = client.execute(request);
        if (resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        } else {
            Log.error(request.getURI().toString());
            Log.error(request.getMethod());
            Log.error(request.toString());
            Log.error(request.getAllHeaders().toString());
            Log.error(resp.toString());
        }
        return respContent;
    }


    public static String httpPost(String url, List<BasicNameValuePair> nameValuePairs) throws IOException {
        return httpPost(url, null, nameValuePairs);
    }


    public static String httpPost(String url, Header header, List<BasicNameValuePair> nameValuePairs) throws IOException {
        HttpPost httpPost = new HttpPost(url);
        httpPost.setHeader(header);
        httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
        Log.info(url);
        Log.info(header.toString());
        for (BasicNameValuePair nameValuePair : nameValuePairs) {
            System.out.println(nameValuePair.getName() + " ===> "+ nameValuePair.getValue());
        }
        return executeDefault(httpPost);
    }

    public static String httpGet(String url) throws IOException {
        return httpGet(url, null);
    }

    public static String httpGet(String url, Header header) throws IOException {
        HttpGet httpGet = new HttpGet(url);
        httpGet.setHeader(header);
        return executeDefault(httpGet);
    }

    public static String httpGetWithToken(String url, String token) throws IOException {
        return httpGet(url, new BasicHeader("Authorization", "bearer " + token));
    }

    public static String httpPostwithToken(String url, String token) throws IOException {
        List<BasicNameValuePair> nameValuePairs = new ArrayList<>();
        Header header = new BasicHeader("Authorization", "Basic YXBwY2xpZW50OmFwcHNlY3JldA==");
        nameValuePairs.add(new BasicNameValuePair("token", token));
        return httpPost(url, header, nameValuePairs);
    }


    public static String getToken(String url, String app_id, String app_key, String username, String password) throws Exception {
        List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
        pairList.add(new BasicNameValuePair("grant_type", "password"));
        pairList.add(new BasicNameValuePair("username", username));
        pairList.add(new BasicNameValuePair("password", password));
        Header header = new BasicHeader("Authorization", "Basic " + Base64.encodeBytes((app_id + ":" + app_key).getBytes()));
        return httpPost(url, header, pairList);
    }

    public static void main(String[] args) throws Exception {
        String url = "http://192.168.100.40:9999/security/oauth/check_token";

        String username = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1MTc4NDE1MTgsInVzZXJfbmFtZSI6ImFkbWluIiwiYXV0aG9yaXRpZXMiOlsiU1lTVEVNIiwiVVNFUiIsIkFETUlOIl0sImp0aSI6IjllOGFkZDMwLTQyNmQtNDc2Mi1iN2IxLWE5N2I0OTQyNTE0ZiIsImNsaWVudF9pZCI6ImFwcGNsaWVudCIsInNjb3BlIjpbIm9wZW5pZCJdfQ.dH2QuJr00rhWstkQzb3lErSqGdKgE9fHgVWDvtsZIGlAmin3uQSrLHsFW0AhgjAnMz4oBEfaFgAyuYJdw5E0gHm3chv5kdsXI1RL-6vvPd_uBRV-ikeS0D-3jy3nlBJz52yB0sjwvmnHJhDZF19AxMEDs8Huj4yaBaHDZEt-vkEPmze6kSbBcbT5igl6DZ9FxfvzUUhPyMxy3uz_uqliv_WxPfVWrx3nYkRkwT5BD4BoJCSzaUAVI52-ozRP04zk4ZnaCVcplx6nusj-blHwQPAzoLikL_OYTipwprGOKoPYf9W1eodYAh3Gyk0ir-RHiNMVJa8hN5Lh61yYYpz4tQ";

        String token = httpPostwithToken(url, username);
        System.out.println(token);
    }

}
