package org.jivesoftware.openfire.mysession;

import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.BasicModule;
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.session.LocalClientSession;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.HttpUtilsNew;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import redis.clients.jedis.Jedis;

import java.util.*;

public class DyrsSessionManagerImpl extends BasicModule implements DyrsSessionManager {

    private static final Logger Log = LoggerFactory.getLogger(DyrsSessionManagerImpl.class);
    /**
     * <p>Create a basic module with the given name.</p>
     * <p>
     * The name for the module or null to use the default
     */

    private long last_time;
    private String app_id, app_key, token_url, sys_user, sys_pwd, user_url;
    private static final String CLUSTER_PROPERTY_NODE = "clustering.node";
    private String ims_node = JiveGlobals.getXMLProperty(CLUSTER_PROPERTY_NODE);
    private Map<String, Object> sys_token_info = new HashMap<>();


    private void init() {
        this.app_id = JiveGlobals.getProperty("oauth2.system.app.id");
        this.app_key = JiveGlobals.getProperty("oauth2.system.app.key");
        this.user_url = JiveGlobals.getProperty("oauth2.system.user.url");
        this.token_url = JiveGlobals.getProperty("oauth2.system.token.url");
        this.sys_user = JiveGlobals.getProperty("oauth2.system.app.user");
        this.sys_pwd = JiveGlobals.getProperty("oauth2.system.app.pwd");
    }

    public DyrsSessionManagerImpl() {
        super("DyrsSessionManagerImpl");
        init();
    }

    @Override
    public void addSession(LocalClientSession session) {
        Jedis jedis = null;
        try {
            jedis = XMPPServer.getInstance().getJedisPool().getResource();

            String jid = session.getAddress().toString();

            String node = session.getAddress().getNode();
            String[] tmp_node = node.split("_");
            if (tmp_node.length > 1) {
                //添加在线客服
                JSONObject userMessage = getUserMessage(node);
                userMessage.put("server_node", ims_node);
                userMessage.put("jid", jid);
                userMessage.put("time", System.currentTimeMillis());

                jedis.hset("online_users:" + tmp_node[0] + ":" + tmp_node[1], jid, userMessage.toString());
                //添加在线客服管理组
                jedis.sadd("online_users_service:" + tmp_node[0] + ":" + tmp_node[1], null);
            } else {
                Map<String, String> map = new HashMap<>();
                map.put("server_node", ims_node);
                map.put("jid", jid);
                map.put("time", System.currentTimeMillis() + "");
                //添加在线租户
                jedis.hset("online_guest_users" + node, jid, new JSONObject(map).toString());
            }

        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }


    private JSONObject getUserMessage(String username) {
        try {
            String token = getSystoken();
            if (token != null) {
                String result = HttpUtilsNew.httpGetWithToken(user_url + "/" + username, token);
                JSONObject jsonObject = new JSONObject(result);
                Log.info(result);
                if (!jsonObject.isNull("data")) {
                    return jsonObject.optJSONObject("data");
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e.getCause());
        }
        return new JSONObject();
    }


    private String getSystoken() throws Exception {
        if (sys_token_info.isEmpty())
            getOauth2SysToken(token_url, this.app_id, this.app_key, sys_user, sys_pwd);
        long expires_in = Long.parseLong(sys_token_info.getOrDefault("expires_in", "0").toString());
        if (((System.currentTimeMillis() - last_time) / 1000) > expires_in) {
            getOauth2SysToken(token_url, this.app_id, this.app_key, sys_user, sys_pwd);
        }
        return sys_token_info.get("access_token").toString();
    }


    private synchronized void getOauth2SysToken(String url, String app_id, String app_key, String username, String password) throws Exception {
        String result = HttpUtilsNew.getToken(url, app_id, app_key, username, password);
        JSONObject jsonObject = new JSONObject(result);
        last_time = System.currentTimeMillis();
        Iterator itr = jsonObject.keys();
        while (itr.hasNext()) {
            String key = itr.next().toString();
            sys_token_info.put(key, jsonObject.get(key));
        }
    }

    @Override
    public void removeSession(JID fullJID, ClientSession session) {
        Jedis jedis = null;
        try {
            jedis = XMPPServer.getInstance().getJedisPool().getResource();
            String node = fullJID.getNode();
            String[] tmp_node = node.split("_");
            if (tmp_node.length > 1) {
                //判断是否还有其他方式登陆的!!
                List<JID> routes = XMPPServer.getInstance().getRoutingTable().getRoutes(new JID(node + "@" + fullJID.getDomain()), null);
                //没有，全都退出了.
                if (routes.size() == 0) {
                    jedis.del("online_users:" + tmp_node[0] + ":" + tmp_node[1], session.getAddress().toString(), fullJID.toFullJID());
                    Set<String> smembers = jedis.smembers(DyrsUtils.ONLINE_SERVICER + tmp_node[0] + ":" + tmp_node[1]);
                    Iterator<String> iterator = smembers.iterator();
                    while (iterator.hasNext()) {
                        String member = iterator.next();
                        if (member != null) {
                            //客服下线通知每个聊天租户
                            Message message = new Message();
                            message.setBody(MessageUtils.getDefaultLogoutMessageBody("admin",member,"您的服务客服已经退出！").toString());
                            message.setType(Message.Type.chat);
                            XMPPServer.getInstance().getRoutingTable().routePacket(new JID(member), message, false);
                        }
                    }
                    jedis.del(DyrsUtils.ONLINE_SERVICER + tmp_node[0] + ":" + tmp_node[1]);
                }

            } else {
                jedis.hdel("online_guest_users" + node, fullJID.toFullJID());
                DyrsSessionCache.getInstance().customerLoginOut(fullJID.toString());
            }
        } catch (Exception e) {
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
    }
}
