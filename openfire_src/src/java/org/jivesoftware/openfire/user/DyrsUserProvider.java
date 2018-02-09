package org.jivesoftware.openfire.user;

import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONObject;
import org.jivesoftware.util.HTTPUtils;
import org.jivesoftware.util.HttpUtilsNew;
import org.jivesoftware.util.JiveGlobals;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author wanganbang
 * <p>
 * DyrsUserProvider Creatd on 2018/1/9
 */
public class DyrsUserProvider implements UserProvider {
    private static final Logger Log = LoggerFactory.getLogger(DyrsUserProvider.class);

    private String app_id, app_key, token_url, sys_user, sys_pwd, user_url, client_url;
    private Map<String, Object> sys_token_info = new HashMap<>();
    private long last_time;

    public DyrsUserProvider() {
        init();
    }

    private void init() {
        this.app_id = JiveGlobals.getProperty("oauth2.system.app.id");
        this.app_key = JiveGlobals.getProperty("oauth2.system.app.key");
        this.user_url = JiveGlobals.getProperty("oauth2.system.user.url");
        this.client_url = JiveGlobals.getProperty("oauth2.system.app.url");
        this.token_url = JiveGlobals.getProperty("oauth2.system.token.url");
        this.sys_user = JiveGlobals.getProperty("oauth2.system.app.user");
        this.sys_pwd = JiveGlobals.getProperty("oauth2.system.app.pwd");
    }

    @Override
    public User loadUser(String username) throws UserNotFoundException {
        try {
            String token = getSystoken();
            if (token != null) {
                String result = HttpUtilsNew.httpGetWithToken(user_url + "/" + username, token);
                JSONObject jsonObject = new JSONObject(result);
                Log.info(result);
                if (!jsonObject.isNull("data")) {
                    String nick = jsonObject.optString("nick");
                    String email = jsonObject.optString("email");
                    Date creationDate = (Date) jsonObject.opt("creationDate");
                    Date modificationDate = (Date) jsonObject.opt("modificationDate");
                    return new User(username, nick, email, creationDate, modificationDate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage(), e.getCause());
        }
        throw new UserNotFoundException();
    }

    private synchronized void getOauth2SysToken(String url, String app_id, String app_key, String username, String password) throws Exception {
        Log.info(url);
        Log.info(app_id);
        Log.info(app_key);
        Log.info(username);
        Log.info(password);

        String result = HttpUtilsNew.getToken(url, app_id, app_key, username, password);
        Log.info(result);
        JSONObject jsonObject = new JSONObject(result);
        last_time = System.currentTimeMillis();
        Iterator itr = jsonObject.keys();
        while (itr.hasNext()) {
            String key = itr.next().toString();
            sys_token_info.put(key, jsonObject.get(key));
        }
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

    @Override
    public User createUser(String username, String password, String name, String email) throws UserAlreadyExistsException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteUser(String username) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getUserCount() {
        return getUsers().size();
    }

    @Override
    public Collection<User> getUsers() {
        try {
            String token = getSystoken();
            if (token != null) {
                String user_url = JiveGlobals.getProperty("oauth2.system.user.url");
                String result = HttpUtilsNew.httpGetWithToken(user_url, token);
                JSONObject jsonObject = new JSONObject(result);
                if (!jsonObject.isNull("data")) {
                    JSONArray userArray = jsonObject.optJSONArray("data");
                    List<User> users = new ArrayList<>();
                    for (int i = 0; i < userArray.length(); i++) {
                        String nick = jsonObject.optString("nick");
                        String username = jsonObject.optString("username");
                        String email = jsonObject.optString("email");
                        Date creationDate = (Date) jsonObject.opt("creationDate");
                        Date modificationDate = (Date) jsonObject.opt("modificationDate");
                        users.add(new User(username, nick, email, creationDate, modificationDate));
                    }
                    return users;
                }

            }
        } catch (Exception e) {
            Log.error(e.getMessage(), e.getCause());
        }
        return null;
    }

    @Override
    public Collection<String> getUsernames() {
        List<String> user_collection = new ArrayList<>();
        Collection<User> users = getUsers();
        if (users != null) {
            for (User user : users) {
                user_collection.add(user.getUsername());
            }
        }

        return user_collection;
    }

    @Override
    public Collection<User> getUsers(int startIndex, int numResults) {
        return null;
    }

    @Override
    public void setName(String username, String name) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setEmail(String username, String email) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setCreationDate(String username, Date creationDate) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setModificationDate(String username, Date modificationDate) throws UserNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<String> getSearchFields() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> findUsers(Set<String> fields, String query) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<User> findUsers(Set<String> fields, String query, int startIndex, int numResults) throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public boolean isNameRequired() {
        return false;
    }

    @Override
    public boolean isEmailRequired() {
        return false;
    }
}
