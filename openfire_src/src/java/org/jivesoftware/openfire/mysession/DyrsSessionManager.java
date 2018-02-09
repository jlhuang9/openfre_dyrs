package org.jivesoftware.openfire.mysession;

import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.xmpp.packet.JID;

public interface DyrsSessionManager {

    void addSession(LocalClientSession session);

    void removeSession(JID fullJID, ClientSession session);

}
