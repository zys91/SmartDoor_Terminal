package com.hhuc.smartdoorterminal.modules.login;

import android.content.Context;
import android.util.Log;

import com.hhuc.smartdoorterminal.R;
import com.hhuc.smartdoorterminal.modules.utils.HttpUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class TLogin {
    private final static String TAG = "TLogin";

    public static Map<String, String> loginUser(Context context, String uid, String pwd) {
        Map<String, String> res = new HashMap<>();
        try {
            String url = context.getResources().getString(R.string.apiUrl);
            url += "/userLogin?";
            url += "uid=" + uid;
            url += "&pwd=" + pwd;
            String resp = HttpUtil.httpGet(url);
            Type type = new TypeToken<Map<String, String>>() {
            }.getType();
            res = new Gson().fromJson(resp, type);
        } catch (Exception er) {
            Log.e(TAG, "loginUser: " + er.getMessage());
        }
        return res;
    }
}
