package org.jivesoftware.openfire.cluster;

import org.dom4j.Element;
import org.dom4j.io.XMPPPacketReader;
import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.Message;
import redis.clients.jedis.JedisPubSub;

import java.io.StringReader;

import static org.jivesoftware.openfire.nio.ConnectionHandler.PARSER_CACHE;

public class ClusterRedisMessageListener extends JedisPubSub {
    private String topic = null;

    public ClusterRedisMessageListener(String topic) {
        this.topic = topic;
    }

    @Override
    public void onMessage(String channel, String message) {
        if (channel.equals(topic)){
            final XMPPPacketReader parser = PARSER_CACHE.get();
            try {
                Element doc = parser.read(new StringReader(message)).getRootElement();
                XMPPServer.getInstance().getMessageRouter().route(new Message(doc));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            System.out.println("Unkown topic " + channel);
        }
    }
}
