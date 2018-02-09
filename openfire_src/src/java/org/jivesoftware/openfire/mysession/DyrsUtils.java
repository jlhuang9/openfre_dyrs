package org.jivesoftware.openfire.mysession;

import org.xmpp.packet.JID;

public class DyrsUtils {
    public static final String ONLINE_SERVICER = "online_servicer:";


    public static String parseCustomerName(JID customerJID) {
        return parseCustomerName(customerJID.getNode());
    }

    public static String parseCustomerName(String customerNode) {
        String[] split = customerNode.split("_");
        return split[0] + ":" + split[1];
    }

}
