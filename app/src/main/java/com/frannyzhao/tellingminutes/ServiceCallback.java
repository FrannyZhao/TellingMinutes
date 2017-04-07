package com.frannyzhao.tellingminutes;

/**
 * Created by zhaofengyi on 3/31/17.
 */

public interface ServiceCallback {
    int KEY_CURRENT_TIME = 0;
    void getServiceData(int key, Object value);
}
