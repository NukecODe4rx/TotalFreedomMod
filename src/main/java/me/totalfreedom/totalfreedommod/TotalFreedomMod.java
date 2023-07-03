package me.totalfreedom.totalfreedommod;

import java.io.File;
import java.io.InputStream;
import java.util.Properties;
import me.totalfreedom.totalfreedommod.admin.AdminList;
import me.totalfreedom.totalfreedommod.banning.BanManager;
import me.totalfreedom.totalfreedommod.banning.IndefiniteBanList;
import me.totalfreedom.totalfreedommod.blocking.BlockBlocker;
import me.totalfreedom.totalfreedommod.blocking.EditBlocker;
import me.totalfreedom.totalfreedommod.blocking.EventBlocker;
import me.totalfreedom.totalfreedommod.blocking.InteractBlocker;
import me.totalfreedom.totalfreedommod.blocking.MobBlocker;
import me.totalfreedom.totalfreedommod.blocking.PVPBlocker;
import me.totalfreedom.totalfreedommod.blocking.PotionBlocker;
import me.totalfreedom.totalfreedommod.blocking.command.CommandBlocker;
import me.totalfreedom.totalfreedommod.bridge.BukkitTelnetBridge;
import me.totalfreedom.totalfreedommod.bridge.CoreProtectBridge;
import me.totalfreedom.totalfreedommod.bridge.EssentialsBridge;
import me.totalfreedom.totalfreedommod.bridge.LibsDisguisesBridge;
import me.totalfreedom.totalfreedommod.bridge.WorldEditBridge;
import me.totalfreedom.totalfreedommod.bridge.WorldGuardBridge;
import me.totalfreedom.totalfreedommod.caging.Cager;
import me.totalfreedom.totalfreedommod.command.CommandLoader;
import me.totalfreedom.totalfreedommod.config.MainConfig;
import me.totalfreedom.totalfreedommod.discord.Discord;
import me.totalfreedom.totalfreedommod.freeze.Freezer;
import me.totalfreedom.totalfreedommod.fun.ItemFun;
import me.totalfreedom.totalfreedommod.fun.Jumppads;
import me.totalfreedom.totalfreedommod.fun.Landminer;
import me.totalfreedom.totalfreedommod.fun.MP44;
import me.totalfreedom.totalfreedommod.fun.Trailer;
import me.totalfreedom.totalfreedommod.httpd.HTTPDaemon;
import me.totalfreedom.totalfreedommod.permissions.PermissionConfig;
import me.totalfreedom.totalfreedommod.permissions.PermissionManager;
import me.totalfreedom.totalfreedommod.player.PlayerList;
import me.totalfreedom.totalfreedommod.punishments.PunishmentList;
import me.totalfreedom.totalfreedommod.rank.RankManager;
import me.totalfreedom.totalfreedommod.shop.Shop;
import me.totalfreedom.totalfreedommod.shop.Votifier;
import me.totalfreedom.totalfreedommod.sql.SQLite;
import me.totalfreedom.totalfreedommod.util.FLog;
import me.totalfreedom.totalfreedommod.util.FUtil;
import me.totalfreedom.totalfreedommod.util.MethodTimer;
import me.totalfreedom.totalfreedommod.world.CleanroomChunkGenerator;
import me.totalfreedom.totalfreedommod.world.WorldManager;
import me.totalfreedom.totalfreedommod.world.WorldRestrictions;
import org.bukkit.Bukkit;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class TotalFreedomMod extends JavaPlugin
{
    public static final String CONFIG_FILENAME = "config.yml";
    public static String pluginName;
    public static String pluginVersion;
    private static TotalFreedomMod plugin;
    //
    public MainConfig config;
    public PermissionConfig permissions;
    //
    // Service Handler
    public FreedomServiceHandler fsh;
    // Command Loader
    public CommandLoader cl;
    // Services
    public WorldManager wm;
    public AdminList al;
    public RankManager rm;
    public CommandBlocker cb;
    public EventBlocker eb;
    public BlockBlocker bb;
    public MobBlocker mb;
    public InteractBlocker ib;
    public PotionBlocker pb;
    public LoginProcess lp;
    public AntiNuke nu;
    public AntiSpam as;
    public PlayerList pl;
    public Shop sh;
    public Votifier vo;
    public SQLite sql;
    public Announcer an;
    public ChatManager cm;
    public Discord dc;
    public PunishmentList pul;
    public BanManager bm;
    public IndefiniteBanList im;
    public PermissionManager pem;
    public GameRuleHandler gr;
    public CommandSpy cs;
    public Cager ca;
    public Freezer fm;
    public EditBlocker ebl;
    public PVPBlocker pbl;
    public Orbiter or;
    public Muter mu;
    public Fuckoff fo;
    public AutoKick ak;
    public Monitors mo;
    public MovementValidator mv;
    public ServerPing sp;
    public ItemFun it;
    public Landminer lm;
    public MP44 mp;
    public Jumppads jp;
    public Trailer tr;
    public HTTPDaemon hd;
    public WorldRestrictions wr;
    public EntityWiper ew;
    public Sitter st;
    public VanishHandler vh;
    //
    // Bridges
    public BukkitTelnetBridge btb;
    public EssentialsBridge esb;
    public LibsDisguisesBridge ldb;
    public CoreProtectBridge cpb;
    public WorldEditBridge web;
    public WorldGuardBridge wgb;

    public static TotalFreedomMod getPlugin()
    {
        return plugin;
    }

    public static TotalFreedomMod plugin()
    {
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins())
        {
            if (plugin.getName().equalsIgnoreCase(pluginName))
            {
                return (TotalFreedomMod)plugin;
            }
        }
        return null;
    }

    @Override
    public void onLoad()
    {
        plugin = this;
        TotalFreedomMod.pluginName = plugin.getDescription().getName();
        TotalFreedomMod.pluginVersion = plugin.getDescription().getVersion();

        FLog.setPluginLogger(plugin.getLogger());
        FLog.setServerLogger(getServer().getLogger());
    }

    @Override
    public void onEnable()
    {
        FLog.info("Created by Madgeek1450 and Prozza");

        final MethodTimer timer = new MethodTimer();
        timer.start();

        // Delete unused files
        FUtil.deleteCoreDumps();
        FUtil.deleteFolder(new File("./_deleteme"));

        fsh = new FreedomServiceHandler();

        config = new MainConfig();

        if (FUtil.inDeveloperMode())
        {
            FLog.debug("Developer mode enabled.");
        }

        cl = new CommandLoader();
        cl.loadCommands();

        permissions = new PermissionConfig();
        permissions.load();

        mv = new MovementValidator();
        sp = new ServerPing();

        new Initializer();

        fsh.startServices();

        FLog.info("Started " + fsh.getServiceAmount() + " services.");

        timer.update();
        FLog.info("Version " + pluginVersion + " enabled in " + timer.getTotal() + "ms");
    }

    @Override
    public void onDisable()
    {
        // Stop services and bridges
        fsh.stopServices();

        getServer().getScheduler().cancelTasks(plugin);

        FLog.info("Plugin disabled");
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(@NotNull String worldName, String id)
    {
        return new CleanroomChunkGenerator(id);
    }

    /**
     * This class is provided to please Codacy.
     */
    private final class Initializer
    {
        public Initializer()
        {
            initServices();
            initAdminUtils();
            initBridges();
            initFun();
            initHTTPD();
        }

        private void initServices()
        {
            // Start services
            wm = new WorldManager();
            sql = new SQLite();
            al = new AdminList();
            rm = new RankManager();
            cb = new CommandBlocker();
            eb = new EventBlocker();
            bb = new BlockBlocker();
            mb = new MobBlocker();
            ib = new InteractBlocker();
            pb = new PotionBlocker();
            lp = new LoginProcess();
            nu = new AntiNuke();
            as = new AntiSpam();
            wr = new WorldRestrictions();
            pl = new PlayerList();
            sh = new Shop();
            vo = new Votifier();
            an = new Announcer();
            cm = new ChatManager();
            dc = new Discord();
            pul = new PunishmentList();
            bm = new BanManager();
            im = new IndefiniteBanList();
            pem = new PermissionManager();
            gr = new GameRuleHandler();
            ew = new EntityWiper();
            st = new Sitter();
            vh = new VanishHandler();
        }

        private void initAdminUtils()
        {
            // Single admin utils
            cs = new CommandSpy();
            ca = new Cager();
            fm = new Freezer();
            or = new Orbiter();
            mu = new Muter();
            ebl = new EditBlocker();
            pbl = new PVPBlocker();
            fo = new Fuckoff();
            ak = new AutoKick();
            mo = new Monitors();
        }

        private void initBridges()
        {
            // Start bridges
            btb = new BukkitTelnetBridge();
            cpb = new CoreProtectBridge();
            esb = new EssentialsBridge();
            ldb = new LibsDisguisesBridge();
            web = new WorldEditBridge();
            wgb = new WorldGuardBridge();
        }

        private void initFun()
        {
            // Fun
            it = new ItemFun();
            lm = new Landminer();
            mp = new MP44();
            jp = new Jumppads();
            tr = new Trailer();
        }

        private void initHTTPD()
        {
            // HTTPD
            hd = new HTTPDaemon();
        }
    }
}
