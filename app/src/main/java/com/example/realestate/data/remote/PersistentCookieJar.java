package com.example.realestate.data.remote;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;

public class PersistentCookieJar implements CookieJar {
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public PersistentCookieJar(Context context) {
        this.sharedPreferences = context.getSharedPreferences("app_cookies", Context.MODE_PRIVATE);
        this.gson = new Gson();
    }

    @Override
    public synchronized void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
        String host = url.host();
        Set<String> cookieStrings = new HashSet<>();
        for (Cookie cookie : cookies) {
            cookieStrings.add(serializeCookie(cookie));
        }
        sharedPreferences.edit().putStringSet(host, cookieStrings).apply();
    }

    @Override
    public synchronized List<Cookie> loadForRequest(HttpUrl url) {
        String host = url.host();
        Set<String> cookieStrings = sharedPreferences.getStringSet(host, null);
        List<Cookie> cookies = new ArrayList<>();
        if (cookieStrings != null) {
            for (String serialized : cookieStrings) {
                Cookie cookie = deserializeCookie(serialized);
                if (cookie != null) {
                    // Filter out expired cookies
                    if (cookie.expiresAt() > System.currentTimeMillis()) {
                        cookies.add(cookie);
                    }
                }
            }
        }
        return cookies;
    }

    private String serializeCookie(Cookie cookie) {
        SerializableCookie sCookie = new SerializableCookie();
        sCookie.name = cookie.name();
        sCookie.value = cookie.value();
        sCookie.expiresAt = cookie.expiresAt();
        sCookie.domain = cookie.domain();
        sCookie.path = cookie.path();
        sCookie.secure = cookie.secure();
        sCookie.httpOnly = cookie.httpOnly();
        sCookie.hostOnly = cookie.hostOnly();
        return gson.toJson(sCookie);
    }

    private Cookie deserializeCookie(String serialized) {
        try {
            SerializableCookie sCookie = gson.fromJson(serialized, SerializableCookie.class);
            Cookie.Builder builder = new Cookie.Builder()
                    .name(sCookie.name)
                    .value(sCookie.value)
                    .path(sCookie.path);
            
            if (sCookie.hostOnly) {
                builder.hostOnlyDomain(sCookie.domain);
            } else {
                builder.domain(sCookie.domain);
            }
            if (sCookie.secure) builder.secure();
            if (sCookie.httpOnly) builder.httpOnly();
            builder.expiresAt(sCookie.expiresAt);
            return builder.build();
        } catch (Exception e) {
            return null;
        }
    }

    private static class SerializableCookie {
        String name;
        String value;
        long expiresAt;
        String domain;
        String path;
        boolean secure;
        boolean httpOnly;
        boolean hostOnly;
    }
}
