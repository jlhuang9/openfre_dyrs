package org.jivesoftware.openfire.mysession;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import java.util.Date;

public class MessageUtils {
    public static JSONObject getDefaultMessageBody(String from, String to, String content, MessageUtils.Type type) {

        long now = System.currentTimeMillis();
        JSONObject result = new JSONObject();
        try {
            result.put("time", now);
            result.put("c_time", now);
            result.put("date", new Date(now));
            result.put("type", type.getType());
            result.put("content", content);
            result.put("from", from);
            result.put("from_type", 2);
            result.put("to", to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static JSONObject getDefaultWelcomeMessageBody(String from, String to, String content) {
        return getDefaultMessageBody(from, to, content, Type.welcome);
    }


    public static JSONObject getDefaultLogoutMessageBody(String from, String to, String content) {
        return getDefaultMessageBody(from, to, content, Type.logout);
    }


    public enum Type{
        welcome("welcome"),  //欢迎语
        logout("logout"),  //退出
        ;
        private String type;

        Type(String type) {
            this.type = type;
        }

        public String getType() {
            return type;
        }
    }

}
