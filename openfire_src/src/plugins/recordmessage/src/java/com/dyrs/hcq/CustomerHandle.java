package com.dyrs.hcq;

import com.dyrs.hcq.redis.RedisManager;
import com.mongodb.util.JSON;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.util.HTTPUtils;
import org.jivesoftware.util.HttpUtilsNew;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import redis.clients.jedis.Jedis;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CustomerHandle {
    private static final Logger Log = LoggerFactory.getLogger(CustomerHandle.class);
    private static CustomerHandle ourInstance = new CustomerHandle();

    public static CustomerHandle getInstance() {
        return ourInstance;
    }


    private static final String PUBLIC_URL = JiveGlobals.getProperty("oauth2.public.url", "http://127.0.0.1:9000/public");

    private CustomerHandle() {
    }

    //欢迎语图
    public static Map<String, String> customerWelcomeMap = new ConcurrentHashMap<>();


    public static String getWelcome(JID customerJID) {
        String customerNode = customerJID.getNode();
        String welcome = null;
        String userInfo = new RedisManager() {
            @Override
            public String getString(Jedis jedis) {
                return jedis.get(makeUserInfo(customerNode));
            }
        }.execute();
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(userInfo);
            welcome = jsonObject.getString("welcome");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (welcome == null) {
            JSONObject welcomeRemote = getWelcome(customerNode);
            try {
                String msg1 = welcomeRemote.getString("msg");
                welcome = msg1;
                customerWelcomeMap.put(customerNode, welcome);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return welcome;
    }

    private static String makeUserInfo(String username) {
        String[] split = username.split("_");
        return "users:info:" + split[0] + ":" + split[1];
    }

    public static JSONObject getWelcome(String customerNode) {
        try {
            return new JSONObject(HttpUtilsNew.httpGet(PUBLIC_URL + "/wellcome/msg/" + customerNode));
        } catch (Exception e) {
            return null;
        }
    }
}
