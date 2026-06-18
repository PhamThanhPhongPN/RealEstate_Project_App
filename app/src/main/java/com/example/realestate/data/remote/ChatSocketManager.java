package com.example.realestate.data.remote;

import android.content.Context;
import android.util.Log;
import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatSocketManager {
    private final Context context;
    private Socket socket;

    public ChatSocketManager(Context context) {
        this.context = context;
    }

    public synchronized void connect(final Runnable onConnect, final Runnable onDisconnect) {
        if (socket != null && socket.connected()) {
            if (onConnect != null) onConnect.run();
            return;
        }

        try {
            PersistentCookieJar cookieJar = new PersistentCookieJar(context);
            HttpUrl httpUrl = HttpUrl.get("http://10.0.2.2:5000/");
            List<Cookie> cookies = cookieJar.loadForRequest(httpUrl);
            
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < cookies.size(); i++) {
                Cookie cookie = cookies.get(i);
                sb.append(cookie.name()).append("=").append(cookie.value());
                if (i < cookies.size() - 1) {
                    sb.append("; ");
                }
            }
            String cookieHeaderValue = sb.toString();
            Log.d("ChatSocket", "Connecting with cookies: " + cookieHeaderValue);

            IO.Options opts = new IO.Options();
            Map<String, List<String>> headers = new HashMap<>();
            headers.put("cookie", Collections.singletonList(cookieHeaderValue));
            opts.extraHeaders = headers;
            opts.transports = new String[]{"websocket"};
            opts.forceNew = true;

            socket = IO.socket("http://10.0.2.2:5000/", opts);

            socket.on(Socket.EVENT_CONNECT, args -> {
                Log.d("ChatSocket", "Connected to Chat WebSocket");
                if (onConnect != null) onConnect.run();
            });

            socket.on(Socket.EVENT_DISCONNECT, args -> {
                Log.d("ChatSocket", "Disconnected from Chat WebSocket");
                if (onDisconnect != null) onDisconnect.run();
            });

            socket.on(Socket.EVENT_CONNECT_ERROR, args -> {
                Object err = args.length > 0 ? args[0] : "Unknown";
                Log.e("ChatSocket", "Connection error: " + err);
            });

            socket.connect();
        } catch (URISyntaxException e) {
            Log.e("ChatSocket", "Socket URL parsing error", e);
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public synchronized void disconnect() {
        if (socket != null) {
            socket.disconnect();
            socket = null;
            Log.d("ChatSocket", "Socket disconnected manually");
        }
    }
}
