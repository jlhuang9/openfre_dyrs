package com.dyrs.hcq.manager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author hcq
 * @create 2018-02-07 下午 2:01
 **/

public class ClientCustomerManager {
    /**
     * 租户客服关系
     */
    private static Map<String, String> clientCustomerMap = new ConcurrentHashMap<>();

    public void add(String client, String customer) {
        clientCustomerMap.put(client, customer);
    }


}
