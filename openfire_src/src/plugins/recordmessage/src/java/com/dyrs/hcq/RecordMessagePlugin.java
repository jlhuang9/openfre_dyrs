package com.dyrs.hcq;

import com.alibaba.fastjson.JSONObject;
import com.dyrs.hcq.service.MessageServiceImpl;
import com.dyrs.hcq.service.PresenceServiceImpl;
import com.dyrs.hcq.utils.MongoDBDaoUtil;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.mysession.DyrsSessionCache;
import org.jivesoftware.openfire.mysession.MessageUtils;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.net.UnknownHostException;

public class RecordMessagePlugin implements PacketInterceptor, Plugin {
    private InterceptorManager interceptorManager;


    private PresenceServiceImpl presenceService = PresenceServiceImpl.getInstance();
    private MessageServiceImpl messageService = MessageServiceImpl.getInstance();


    public RecordMessagePlugin() {
        this.interceptorManager = InterceptorManager.getInstance();
    }

    @Override
    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        interceptorManager.addInterceptor(this);
        System.out.println("initializing... RecordMessagePlugin!");
    }

    @Override
    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
        System.out.println("server stop，destroy RecordMessagePlugin!");
    }

    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {

        if (packet instanceof Message) {
            messageService.interceptPacket(packet, session, incoming, processed);
        } else if (packet instanceof Presence) {
            presenceService.interceptPacket(packet, session, incoming, processed);
        }
    }
}
