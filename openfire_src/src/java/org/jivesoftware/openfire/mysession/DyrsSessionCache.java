package org.jivesoftware.openfire.mysession;

import org.jivesoftware.openfire.XMPPServer;
import org.xmpp.packet.JID;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DyrsSessionCache {

    private Map<String, String> guestCustomerMap = new ConcurrentHashMap<>();

    private JedisPool jedisPool = XMPPServer.getInstance().getJedisPool();

    private static DyrsSessionCache dyrsSessionCache = new DyrsSessionCache();

    public static DyrsSessionCache getInstance() {
        return dyrsSessionCache;
    }

    /**
     * 建立游客客服关系
     * @param guest  游客node
     * @param customer  客服node
     */
    public void addRelation(JID guest , JID customer ) {

        String model = DyrsUtils.parseCustomerName(customer);
        String guestFullName = guest.toString();
        guestCustomerMap.put(guest.toFullJID(), model);
        Jedis resource = null;
        try {
            resource = jedisPool.getResource();
            resource.sadd(DyrsUtils.ONLINE_SERVICER + model, guestFullName);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (resource != null) {
                resource.close();
            }
        }
    }

    /**
     * 游客退出
     * @param guest  游客node
     */
    public void customerLoginOut(String guest) {
        String customer = guestCustomerMap.get(guest);
        if (customer != null) {
            Jedis resource = null;
            try {
                resource = jedisPool.getResource();
                resource.srem(DyrsUtils.ONLINE_SERVICER + customer, guest);
            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                if (resource != null) {
                    resource.close();
                }
            }
        }

    }
}
