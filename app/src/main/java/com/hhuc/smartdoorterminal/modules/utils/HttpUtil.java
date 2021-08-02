package com.hhuc.smartdoorterminal.modules.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpUtil {
    private final static int TIMEOUT_IN_MILLIONS = 5000;

    public static String httpGet(String aUrl) {
        String res = "";
        try {
            URL url;
            HttpURLConnection conn = null;
            InputStream is = null;
            ByteArrayOutputStream baos = null;
            try {
                url = new URL(aUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(TIMEOUT_IN_MILLIONS);
                conn.setConnectTimeout(TIMEOUT_IN_MILLIONS);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("accept", "*/*");
                conn.setRequestProperty("connection", "Keep-Alive");
                if (conn.getResponseCode() == 200) {
                    is = conn.getInputStream();
                    baos = new ByteArrayOutputStream();
                    int len;
                    byte[] buf = new byte[128];
                    while ((len = is.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    baos.flush();
                    return baos.toString();
                } else {
                    throw new RuntimeException("ResponseCode is not 200 ...");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (is != null)
                        is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    if (baos != null)
                        baos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (conn != null)
                    conn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}
