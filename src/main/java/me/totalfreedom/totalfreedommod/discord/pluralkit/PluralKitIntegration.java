package me.totalfreedom.totalfreedommod.discord.pluralkit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.totalfreedom.totalfreedommod.TotalFreedomMod;

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PluralKitIntegration {
    private static final String GET_SYSTEM_ENDPOINT = "https://api.pluralkit.me/v2/systems/";
    private static final Gson GSON = new GsonBuilder().create();

    private final List<String> enabledUsers;
    private final Map<String, PluralKitSystem> systems = new HashMap<>(); // TODO: Automatically clear this map so we don't store data forever.

    public PluralKitIntegration() {
        this.enabledUsers = TotalFreedomMod.getPlugin().sql.getPluralKitUsers();
    }

    public boolean isEnabled(final String id) {
        return this.enabledUsers.contains(id);
    }

    public void setEnabled(final String id, final boolean enabled) {
        TotalFreedomMod.getPlugin().sql.setDiscordValue(id, enabled);
        if (enabled) {
            this.enabledUsers.add(id);
        } else {
            this.enabledUsers.remove(id);
            this.systems.remove(id);
        }
    }

    public void setNullSystem(final String id) {
        this.systems.put(id, null);
    }

    public PluralKitSystem getSystemInfo(final String id) {
        if (!this.enabledUsers.contains(id)) return null;
        if (!this.systems.containsKey(id)) {
            try {
                this.refresh(id);
            } catch (Exception e) {
                this.setNullSystem(id);
            }
        }

        return this.systems.get(id);
    }

    public void refresh(final String id) throws Exception {
        final URL url = new URL(GET_SYSTEM_ENDPOINT + id + "/members");
        final HttpsURLConnection conn = (HttpsURLConnection)url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        conn.setUseCaches(false);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.connect();

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("Could not query members");
        }

        final PluralKitMember[] members = GSON.fromJson(new InputStreamReader(conn.getInputStream()), PluralKitMember[].class);
        this.systems.put(id, new PluralKitSystem(List.of(members)));

        System.out.println(GSON.toJson(this.systems.get(id)));
    }
}
