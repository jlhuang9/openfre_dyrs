package com.dyrs.hcq.service;

import com.dyrs.hcq.CustomerHandle;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.mysession.DyrsSessionCache;
import org.jivesoftware.openfire.mysession.MessageUtils;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hcq
 * @create 2018-02-07 下午 1:47
 **/

public class PresenceServiceImpl implements PacketService {

    private static PresenceServiceImpl ourInstance = new PresenceServiceImpl();


    public static PresenceServiceImpl getInstance() {
        return ourInstance;
    }

    private PresenceServiceImpl() {
    }

    public void proess() {

    }


    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException {
        if (incoming && !processed) {
            Presence presence = (Presence) packet;
            JID from_jid = ((Presence) packet).getFrom();
            JID to_jid = ((Presence) packet).getTo();
            Presence.Type type = presence.getType();
            if (to_jid != null && to_jid.getNode() != null && to_jid.getNode().contains("_") && type == null) {
                DyrsSessionCache.getInstance().addRelation(from_jid, to_jid);
                String node = from_jid.getNode();
                Message message = new Message();
                message.setBody(MessageUtils.getDefaultWelcomeMessageBody(to_jid.getNode(), from_jid.getNode(), CustomerHandle.getWelcome(to_jid)).toString());
                message.setType(Message.Type.chat);
                XMPPServer.getInstance().getRoutingTable().routePacket(from_jid, message, false);
            }
        }
    }
}