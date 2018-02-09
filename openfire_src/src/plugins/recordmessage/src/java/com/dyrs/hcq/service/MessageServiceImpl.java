package com.dyrs.hcq.service;

import com.alibaba.fastjson.JSONObject;
import com.dyrs.hcq.MessageHandle;
import com.dyrs.hcq.utils.MongoDBDaoUtil;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import redis.clients.jedis.Jedis;

import java.net.UnknownHostException;

/**
 * @author hcq
 * @create 2018-02-07 下午 2:08
 **/

public class MessageServiceImpl implements PacketService{

    private static MessageServiceImpl ourInstance = new MessageServiceImpl();


    public static MessageServiceImpl getInstance() {
        return ourInstance;
    }

    private MessageServiceImpl() {
    }

    private XMPPServer server = XMPPServer.getInstance();
    private MongoDBDaoUtil mongoDBDaoUtil = MongoDBDaoUtil.getInstance();

    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        String body = ((Message) packet).getBody();

        if (incoming == true && processed == false && body != null) {
            Jedis jedis = null;
            try {
                jedis = server.getJedisPool().getResource();
                String ip = jedis.hget("client_ips", packet.getFrom().toBareJID());
                if (ip == null || ip.length() < 1) {
                    try {
                        ip = session.getHostAddress();
                    } catch (UnknownHostException e) {
                        ip = "unknown";
                    }
                }
                JSONObject message = MessageHandle.parseMessage(body, ip);
                mongoDBDaoUtil.insert("test", "ChatLog", message);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                jedis.close();
            }
        }
    }
}
