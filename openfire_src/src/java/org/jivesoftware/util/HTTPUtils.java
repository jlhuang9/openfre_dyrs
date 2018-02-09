package org.jivesoftware.util;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.util.Base64;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author wanganbang
 * <p>
 * HTTPUtils Creatd on 2018/1/2
 */
public class HTTPUtils {
    public static String httpGetWithJSON(String url, String token) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;
        httpGet.addHeader("Authorization", "bearer " + token);
        HttpResponse resp = client.execute(httpGet);
        if (resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        }
        return respContent;
    }

    public static String httpGetWithJSON(String url) throws Exception {
        HttpGet httpGet = new HttpGet(url);
        CloseableHttpClient client = HttpClients.createDefault();
        HttpResponse resp = client.execute(httpGet);
        String respContent = null;
        if (resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        }
        return respContent;
    }



    public static String httpPostWithUrlencoded(String url, String app_id, String app_key, String username, String password) throws Exception {

        HttpPost httpPost = new HttpPost(url);
        CloseableHttpClient client = HttpClients.createDefault();
        String respContent = null;

//        表单方式
        List<BasicNameValuePair> pairList = new ArrayList<BasicNameValuePair>();
        pairList.add(new BasicNameValuePair("grant_type", "password"));
        pairList.add(new BasicNameValuePair("username", username));
        pairList.add(new BasicNameValuePair("password", password));

        httpPost.setHeader("Authorization", "Basic " + Base64.encodeBytes((app_id + ":" + app_key).getBytes()));
        httpPost.setEntity(new UrlEncodedFormEntity(pairList, "utf-8"));

        HttpResponse resp = client.execute(httpPost);
        if (resp.getStatusLine().getStatusCode() == 200) {
            HttpEntity he = resp.getEntity();
            respContent = EntityUtils.toString(he, "UTF-8");
        }
        return respContent;
    }




}
