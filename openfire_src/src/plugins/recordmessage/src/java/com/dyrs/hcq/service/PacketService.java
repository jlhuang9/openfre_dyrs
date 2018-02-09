package com.dyrs.hcq.service;

import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Packet;

public interface PacketService {

    void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException;
}
