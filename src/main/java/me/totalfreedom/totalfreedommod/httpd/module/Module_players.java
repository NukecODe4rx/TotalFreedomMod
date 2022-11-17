package me.totalfreedom.totalfreedommod.httpd.module;

import com.google.gson.Gson;
import me.totalfreedom.totalfreedommod.config.ConfigEntry;
import me.totalfreedom.totalfreedommod.httpd.NanoHTTPD;
import me.totalfreedom.totalfreedommod.util.FUtil;
import org.bukkit.entity.HumanEntity;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Module_players extends HTTPDModule
{   
    private static final Gson gson = new Gson();
    
    public Module_players(NanoHTTPD.HTTPSession session)
    {
        super(session);
    }

    @Override
    public NanoHTTPD.Response getResponse()
    {
        final Map<String, List<String>> responseMap = new HashMap<>();

        final List<String> admins = new ArrayList<>();
        final List<String> senioradmins = new ArrayList<>();

        plugin.al.getActiveAdmins().stream().filter(admin -> admin.getName() != null).forEach(admin ->
        {
            switch (admin.getRank())
            {
                case ADMIN -> admins.add(admin.getName());
                case SENIOR_ADMIN -> senioradmins.add(admin.getName());
                default ->
                {
                    // Do nothing, keeps Codacy quiet
                }
            }
        });

        responseMap.put("players", server.getOnlinePlayers().stream().filter(player ->
                !plugin.al.isVanished(player.getUniqueId())).map(HumanEntity::getName).toList());
        responseMap.put("masterbuilders", plugin.pl.getMasterBuilderNames());
        responseMap.put("admins", admins);
        responseMap.put("senioradmins", senioradmins);
        responseMap.put("developers", FUtil.DEVELOPER_NAMES);
        responseMap.put("assistantexecutives", ConfigEntry.SERVER_ASSISTANT_EXECUTIVES.getStringList());
        responseMap.put("executives", ConfigEntry.SERVER_EXECUTIVES.getStringList());

        final NanoHTTPD.Response response = new NanoHTTPD.Response(NanoHTTPD.Response.Status.OK, NanoHTTPD.MIME_JSON,
                gson.toJson(responseMap));
        response.addHeader("Access-Control-Allow-Origin", "*");
        return response;
    }
}