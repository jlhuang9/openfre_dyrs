package com.dyrs.hcq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dyrs.hcq.utils.StringUtils;

import java.util.Date;
import java.util.UUID;

public class MessageHandle {

    /**
     * 格式化消息
     * @param body 消息体
     * @param ip ip地址
     * @return
     */
    public static JSONObject parseMessage(String body, String ip) {
        JSONObject result = null;
        if (body != null && body.length() > 0) {
            result = JSON.parseObject(body);
            long nowTime = System.currentTimeMillis();
            String from_uid = result.getString("from_uid");
            String to_uid = result.getString("to_uid");
            String client_id = result.getString("client_id"); //公司域

            String userToUserId = StringUtils.makeNewString(from_uid, to_uid);
            String clientUserId = StringUtils.makeNewString(from_uid, to_uid, client_id);

            String msg_id = UUID.randomUUID().toString() + "-" + nowTime;
            result.put("user_user_id", userToUserId);
            result.put("client_user_id", clientUserId);
            result.put("msg_id", msg_id);
            result.put("time", nowTime);
            result.put("date", new Date(nowTime));
            result.put("ip", ip);
        }
        return result;
    }
}
