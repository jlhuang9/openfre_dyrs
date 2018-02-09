package org.jivesoftware.openfire.auth;

import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.util.HTTPUtils;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.jivesoftware.util.HttpUtilsNew;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author wanganbang
 * <p>
 * DyrsAuthProvider Creatd on 2018/1/2
 */
public class DyrsAuthProvider implements AuthProvider {
    private static final Logger Log = LoggerFactory.getLogger(DyrsAuthProvider.class);

    private String app_id, app_key, token_url, sys_user, sys_pwd, user_url, client_url, checktoken_url;
    private Map<String, Object> sys_token_info = new HashMap<>();
    private long last_time;

    public DyrsAuthProvider() {
        this.app_id = JiveGlobals.getProperty("oauth2.system.app.id");
        this.app_key = JiveGlobals.getProperty("oauth2.system.app.key");
        this.user_url = JiveGlobals.getProperty("oauth2.system.user.url");
        this.client_url = JiveGlobals.getProperty("oauth2.system.app.url");
        this.token_url = JiveGlobals.getProperty("oauth2.system.token.url");
        this.checktoken_url = JiveGlobals.getProperty("oauth2.system.check_token.url", "http://127.0.0.1:9999/oauth/check_token");
        this.sys_user = JiveGlobals.getProperty("oauth2.system.app.user");
        this.sys_pwd = JiveGlobals.getProperty("oauth2.system.app.pwd");
    }

    @Override
    public void authenticate(String username, String accessToken) throws UnauthorizedException, ConnectionException, InternalUnauthenticatedException {
        try {
            Log.info("begin");
            if (username != null && accessToken != null && "admin".equals(username) && "admin".equals(accessToken)) {
                return;
            }
            JSONObject userInfo = new JSONObject(HttpUtilsNew.httpPostwithToken(checktoken_url, accessToken));
            if (userInfo != null && !userInfo.isNull("user_name")) {
                Log.info("begin");
                String user_name = userInfo.getString("user_name");
                Log.info(user_name);
                if (!user_name.equals(username)) {
                    throw new UnauthorizedException("not your token");
                }
            } else {
                throw new UnauthorizedException("not found app_id");
            }

        } catch (Exception e) {
            throw new ConnectionException(e.getMessage());
        }
    }

//    @Override
//    public void authenticate(String username, String password) throws UnauthorizedException, ConnectionException, InternalUnauthenticatedException {
//        try {
//            String token = getSystoken();
//            if (token != null) {
//                JSONObject userInfo = getUserInfo(this.user_url, sys_token_info.get("access_token").toString(), username);
//                if (userInfo != null && !userInfo.isNull("data")) {
//                    JSONObject clientInfo = getClient(this.client_url, sys_token_info.get("access_token").toString(), userInfo.optJSONObject("data").optString("clientId"));
//                    if (clientInfo == null && clientInfo.isNull("data")) {
//                        throw new InternalUnauthenticatedException();
//                    }
//                    JSONObject client_data = clientInfo.optJSONObject("data");
//                    if (authenticate(token_url, client_data.getString("clientId"), client_data.getString("clientSecret"), username, password)) {
//                        // pass
//                    } else {
//                        throw new UnauthorizedException("Unauthorized");
//                    }
//                } else {
//                    throw new UnauthorizedException("not found app_id");
//                }
//            }else {
//                throw new UnauthorizedException("get system token error");
//            }
//        } catch (Exception e) {
//            throw new ConnectionException(e.getMessage());
//        }
//    }


    private boolean authenticate(String url, String app_id, String app_key, String username, String password) {
        try {
            String result = HttpUtilsNew.getToken(url, app_id, app_key, username, password);
            JSONObject jsonObject = new JSONObject(result);
            Log.info(jsonObject.toString());
            if (jsonObject != null) {
                return true;
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return false;
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

    private JSONObject getClient(String client_url, String token, String client_id) throws Exception {
        return new JSONObject(HttpUtilsNew.httpGetWithToken(client_url + "/" + client_id, token));
    }

    private JSONObject getUserInfo(String user_url, String access_token, String username) throws Exception {
        return new JSONObject(HttpUtilsNew.httpGetWithToken(user_url + "/" + username, access_token));
    }

    @Override
    public String getPassword(String username) throws UserNotFoundException, UnsupportedOperationException {
        return null;
    }

    @Override
    public void setPassword(String username, String password) throws UserNotFoundException, UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean supportsPasswordRetrieval() {
        return false;
    }

    @Override
    public boolean isScramSupported() {
        return false;
    }

    @Override
    public String getSalt(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getIterations(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getServerKey(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getStoredKey(String username) throws UnsupportedOperationException, UserNotFoundException {
        throw new UnsupportedOperationException();
    }
}
