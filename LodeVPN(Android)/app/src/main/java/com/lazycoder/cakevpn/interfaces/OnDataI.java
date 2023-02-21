package com.lazycoder.cakevpn.interfaces;

import com.lazycoder.cakevpn.model.Server;

public interface OnDataI {
    public void onDataPass(String contry,String flagURL, String ip, String login, String password);
}
