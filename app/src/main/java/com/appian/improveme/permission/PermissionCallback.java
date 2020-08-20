package com.appian.improveme.permission;

/**
 * Created by ywwynm on 2016/5/21.
 * permission callback
 */
public interface PermissionCallback {
    void onGranted();
    void onDenied();
}
