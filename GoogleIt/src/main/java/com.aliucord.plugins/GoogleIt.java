package com.aliucord.plugins;

import android.content.Context;
import androidx.annotation.NonNull;

import com.aliucord.Http;
import com.aliucord.Main;
import com.aliucord.api.CommandsAPI;
import com.aliucord.entities.Plugin;
import com.aliucord.annotations.AliucordPlugin;
import java.util.*;

@SuppressWarnings("unused")
@AliucordPlugin
public class GoogleIt extends Plugin {

    public static final String TargetUrl = "https://google.it/search?q=";

    @Override
    public void start(Context context) {
        commands.registerCommand("GoogleIt", "Generates a google.it link.", Collections.singletonList(CommandsAPI.requiredMessageOption), args -> {
            String msg = (String) args.get("message");
            if (msg == null) return new CommandsAPI.CommandResult(msg);
            String UrlResult = null;
            try {
                UrlResult = new Http.QueryBuilder("https://google.it/search").append("q", msg).toString();
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
            return new CommandsAPI.CommandResult(UrlResult);
        });
    }

    @Override
    public void stop(Context context) {
        commands.unregisterAll();
    }

}
