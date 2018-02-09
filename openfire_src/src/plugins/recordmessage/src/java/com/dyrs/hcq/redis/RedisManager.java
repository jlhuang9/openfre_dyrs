package com.dyrs.hcq.redis;

import org.jivesoftware.openfire.XMPPServer;
import redis.clients.jedis.Jedis;

/**
 * @author hcq
 * @create 2018-02-09 上午 9:51
 **/

public abstract class RedisManager {

    public abstract String getString(Jedis jedis);

    public String execute() {
        Jedis jedis = null;
        try {
            jedis = XMPPServer.getInstance().getJedisPool().getResource();
            return getString(jedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
        return null;
    }
}
