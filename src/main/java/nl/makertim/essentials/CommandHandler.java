package nl.makertim.essentials;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.ArrayUtils;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import nl.imine.api.db.iMinePlayer;
import nl.imine.api.gui.Button;
import nl.imine.api.gui.Container;
import nl.imine.api.gui.GuiManager;
import nl.imine.api.gui.button.ButtonBrowse;
import nl.imine.api.gui.button.ButtonCommand;
import nl.imine.api.gui.button.ButtonList;
import nl.imine.api.gui.button.ButtonListed;
import nl.imine.api.reflection.PluginReflection;
import nl.imine.api.sorters.MapCountSorter;
import nl.imine.api.sorters.MapCountSorter.Sort;
import nl.imine.api.sorters.StringSearchSorter;
import nl.imine.api.util.ColorUtil;
import nl.imine.api.util.DateUtil;
import nl.imine.api.util.FlyUtil;
import nl.imine.api.util.FlyUtil.Path;
import nl.imine.api.util.ItemUtil;
import nl.imine.api.util.PlayerUtil;
import nl.imine.api.util.StringUtil;
import nl.imine.api.util.WebUtil;
import nl.imine.api.util.command.CommandUtil;

public class CommandHandler {

    private final String adminChatFormat = ColorUtil.replaceColors("&r&l[&a&lADMIN&r&l] &r&7%s &r&l\u00BB &r%s");
    private final String reportChatFormat = ColorUtil.replaceColors("&r&l[&c&lREPORT&r&l] &r&7%s &r&l\u00BB &r%s");
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
    private static final String[] SERVERS = {"uhc", "hub", "survival", "outlaws", "outlawsB", "testserver", "creative",
            "pixelmon", "minigame"};
    private static final Map<CommandSender, CommandSender> LAST_SPOKE = new HashMap<>();

    private final CommandSender sender;
    private final String command;
    private String[] args;

    public CommandHandler(final CommandSender sender, final String command, final String[] args) {
        this.sender = sender;
        this.command = command;
        this.args = args;
    }

    public boolean onCommand() {
        String finalAwnser = null;
        if (command.equalsIgnoreCase("hub")) {
            finalAwnser = hub();
        } else if (command.equalsIgnoreCase("dev")) {
            finalAwnser = dev();
        } else if (command.equalsIgnoreCase("skull")) {
            finalAwnser = skull();
        } else if (command.equalsIgnoreCase("mute")) {
            finalAwnser = mute();
        } else if (command.equalsIgnoreCase("tp")) {
            finalAwnser = tp();
        } else if (command.equalsIgnoreCase("fly")) {
            finalAwnser = fly();
        } else if (command.equalsIgnoreCase("lagdebug")) {
            finalAwnser = lagdebug();
        } else if (command.equalsIgnoreCase("gm")) {
            finalAwnser = gm();
        } else if (command.startsWith("gm") && command.length() == 3) {
            finalAwnser = gmx();
        } else if (command.equalsIgnoreCase("speed")) {
            finalAwnser = speed();
        } else if (command.equalsIgnoreCase("whois")) {
            finalAwnser = whois();
        } else if (command.equalsIgnoreCase("banrichtlijn")) {
            finalAwnser = banrichtlijn();
        } else if (command.equalsIgnoreCase("msg")) {
            finalAwnser = msg();
        } else if (command.equalsIgnoreCase("invsee")) {
            finalAwnser = invsee();
        } else if (command.equalsIgnoreCase("endersee")) {
            finalAwnser = endersee();
        } else if (command.equalsIgnoreCase("flycheck")) {
            finalAwnser = flycheck();
        } else if (command.equalsIgnoreCase("mchistory")) {
            finalAwnser = mchistory();
        } else if (command.equalsIgnoreCase("plr")) {
            finalAwnser = plr();
        } else if (command.equalsIgnoreCase("return")) {
            finalAwnser = returnTP();
        } else if (command.equalsIgnoreCase("vanish")) {
            finalAwnser = vanish();
        } else if (command.equalsIgnoreCase("kill")) {
            finalAwnser = kill();
        } else if (command.equalsIgnoreCase("reply")) {
            finalAwnser = reply();
        } else if (command.equalsIgnoreCase("me")) {
            finalAwnser = me();
        } else if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("plugin")
                || command.equalsIgnoreCase("plugins")) {
            finalAwnser = plugin();
        } else if (command.equalsIgnoreCase("world")) {
            finalAwnser = world();
        } else if (command.equalsIgnoreCase("report")) {
            finalAwnser = reportChat();
        } else if (command.equalsIgnoreCase("admin")) {
            finalAwnser = adminChat();
        } else if (command.equalsIgnoreCase("update")) {
            if (sender instanceof Player) {
                ((Player) sender).performCommand("reload");
            } else {
                Bukkit.reload();
            }
        }
        if (finalAwnser == null) {
            return false;
        } else {
            if (!finalAwnser.trim().isEmpty()) {
                sender.sendMessage(finalAwnser);
            }
            return true;
        }
    }

    private String skull() {
        if (args.length == 0) {
            return CommandUtil.noOption(command, args);
        }
        if (!(sender instanceof Player)) {
            return CommandUtil.noPlayer();
        }
        if (!sender.hasPermission("iMine.skull")) {
            return CommandUtil.noPermission(command);
        }
        SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
        meta.setOwner(args[0]);
        Player pl = (Player) sender;
        pl.getInventory().addItem(ItemUtil.getBuilder(Material.SKULL_ITEM, meta).setDurability((short) 3).build());
        return ColorUtil.replaceColors("&7Gave skull of &c%s&7.", args[0]);
    }

    private String flycheck() {
        if (!(sender instanceof Player)) {
            return CommandUtil.noPlayer();
        }
        if (!sender.hasPermission("iMine.flycheck")) {
            return CommandUtil.noPermission(command);
        }
        if (args.length == 0) {
            ResultSet rs = BukkitStarter.plugin.getDB().selectQuery(
                    "SELECT DISTINCT `iMinePlayer`.`LastName`, MAX(FlyReports.Time) AS TIME FROM `iMinePlayer` RIGHT JOIN `FlyReports` ON `iMinePlayer`.`UUID` = `FlyReports`.`UUID` GROUP BY `iMinePlayer`.`LastName` ORDER BY TIME DESC LIMIT 10");
            try {
                sender.sendMessage("Checking all flys");
                while (rs.next()) {
                    TextComponent message = new TextComponent(ColorUtil.replaceColors(" - &c%s", rs.getString("LastName")));
                    message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/flycheck " + rs.getString("LastName")));
                    message.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, new ComponentBuilder("Click to check").create()));
                    if (sender instanceof Player) {
                        Player pl = (Player) sender;
                        pl.spigot().sendMessage(message);
                    } else {
                        sender.sendMessage(message.toPlainText());
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return "";
        }
        Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
            UUID uuid = PlayerUtil.getUUID(args[0], false);
            if (uuid == null) {
                sender.sendMessage(CommandUtil.noOnline(args[0]));
            }
            Container ui = getFlightContainer(uuid);
            ui.open((Player) sender);
        });
        return ColorUtil.replaceColors("&7Checking '&c%s&7' for the last &c9 &7fly possibilities.", args[0]);
    }

    private Container getFlightContainer(UUID uuid) {
        Container ui = GuiManager.getInstance().createContainer(
                ColorUtil.replaceColors("&4Last 'flying' events " + iMinePlayer.findPlayer(uuid).getName()), 9, false,
                false);
        List<Path> paths = FlyUtil.getPathsOf(uuid);
        int i = 0;
        for (Path path : paths) {
            if (i < 8) {
                ui.addButton(new FlyCheckButton(path, i++));
            }
        }
        return ui;
    }

    private String whois() {
        if (args.length == 0) {
            return CommandUtil.noOption(command, args);
        }
        if (!(sender instanceof Player)) {
            return CommandUtil.noPlayer();
        }
        if (!sender.hasPermission("iMine.whois")) {
            return CommandUtil.noPermission(command);
        }
        Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
            UUID uuid = PlayerUtil.getUUID(args[0], false);
            if (uuid == null) {
                sender.sendMessage(CommandUtil.noOnline(args[0]));
            }
            final iMinePlayer ipl = iMinePlayer.findPlayer(uuid);
            final Container ui = GuiManager.getInstance()
                    .createContainer(ColorUtil.replaceColors("&7Who is: &c%s&7.", ipl.getName()), 36, false, false);
            // Skull & Stats
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
                // TODO: playerbuttons -> for refreshing
                List<String> online = new ArrayList<>();
                List<String> stats = new ArrayList<>();
                List<String> achievements = new ArrayList<>();
                if (ipl.getPlayer() != null) {
                    OfflinePlayer targetO = ipl.getPlayer();
                    if (targetO != null && targetO.isOnline()) {
                        Player target = (Player) targetO;
                        online.add(ColorUtil.replaceColors("&aOnline&7."));
                        online.add(ColorUtil.replaceColors("&7Rank: &e%s &7[&c%d&7].", ipl.getRank().getName(),
                                ipl.getRank().getAdminRanking()));
                        Location loc = target.getLocation();
                        online.add(ColorUtil.replaceColors("&7Location: &e%s &7[&c%d&7,&c%d&7,&c%d&7].",
                                loc.getWorld().getName(), (int) loc.getX(), (int) loc.getY(), (int) loc.getZ()));
                        online.add(ColorUtil.replaceColors("&7Allow Flight? &e%s&7.",
                                Boolean.toString(target.getAllowFlight())));
                        online.add(
                                ColorUtil.replaceColors("&7Is Flying? &e%s&7.", Boolean.toString(target.isFlying())));
                        online.add(ColorUtil.replaceColors("&7Is Vanish? &e%s&7.", Boolean.toString(ipl.isVanished())));
                        online.add(ColorUtil.replaceColors("&7Speed &eWalking&7: &c%d&7, &eFlying&7: &c%d&7.",
                                (int) (target.getWalkSpeed() * 10), (int) (target.getFlySpeed() * 10)));
                        online.add(ColorUtil.replaceColors("&7Gamemode: &e%s&7.",
                                StringUtil.readableEnum(target.getGameMode())));
                        online.add(ColorUtil.replaceColors("&7Health: &c%d&7/&c%d&7.", (int) target.getHealth(),
                                (int) target.getMaxHealth()));
                        online.add(ColorUtil.replaceColors("&7UUID: &e%s&7.", target.getUniqueId()));
                        for (Statistic stat : Statistic.values()) {
                            try {
                                switch (stat.getType()) {
                                    case BLOCK:
                                    case ITEM:
                                        for (Material mat : Material.values()) {
                                            try {
                                                stats.add(ColorUtil.replaceColors("&7%s&e%s: &c%s&7.",
                                                        StringUtil.readableEnum(stat), StringUtil.readableEnum(mat),
                                                        target.getStatistic(stat, mat)));
                                            } catch (Exception ex) {
                                            }
                                        }
                                        break;
                                    case ENTITY:
                                        for (EntityType enity : EntityType.values()) {
                                            try {
                                                stats.add(ColorUtil.replaceColors("&7%s&e%s: &c%s&7.",
                                                        StringUtil.readableEnum(stat), StringUtil.readableEnum(enity),
                                                        target.getStatistic(stat, enity)));
                                            } catch (Exception ex) {
                                            }
                                        }
                                        break;
                                    default:
                                        stats.add(ColorUtil.replaceColors("&7%s: &c%s&7.", StringUtil.readableEnum(stat),
                                                target.getStatistic(stat)));
                                        break;
                                }
                            } catch (Exception ex) {
                            }
                        }
                        for (Achievement achievement : Achievement.values()) {
                            try {
                                achievements.add(
                                        ColorUtil.replaceColors("&7%s: &e%s&7.", StringUtil.readableEnum(achievement),
                                                Boolean.toString(target.hasAchievement(achievement))));
                            } catch (Exception ex) {
                            }
                        }
                    } else {
                        online.add(ColorUtil.replaceColors("&cOffline"));
                    }
                } else {
                    online.add(ColorUtil.replaceColors("&4No stats available."));
                }
                SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
                meta.setOwner(ipl.getName());
                ui.addButton(
                        new Button(
                                ItemUtil.getBuilder(Material.SKULL_ITEM, meta).setDurability((short) 3)
                                        .setName(ColorUtil.replaceColors("&6%s", ipl.getName())).setLore(online).build(),
                                4));
                if (!stats.isEmpty()) {
                    ui.addButton(new ButtonList(
                            ItemUtil.getBuilder(Material.PAPER).setName(ColorUtil.replaceColors("&aStats")).build(),
                            stats, 5));
                }
                if (!achievements.isEmpty()) {
                    ui.addButton(new ButtonList(ItemUtil.getBuilder(Material.PAPER)
                            .setName(ColorUtil.replaceColors("&aAchievements")).build(), achievements, 6));
                }
            });
            // Name history & Last Seen
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
                NameLookup nl = new NameLookup(uuid, false);
                nl.run();
                List<String> names = nl.getNames();
                ui.addButton(new Button(ItemUtil.getBuilder(Material.BOOK_AND_QUILL)
                        .setName(ColorUtil.replaceColors("&bName history")).setLore(names).build(), 9));
                ui.addButton(new Button(
                        ItemUtil.getBuilder(Material.SIGN)
                                .setName(ColorUtil.replaceColors("&bLast seen")).setLore(new String[]{ColorUtil
                                .replaceColors("&7Last seen: &c%s&7.", dateFormat.format(ipl.getDate()))})
                                .build(),
                        10));
            });
            // Ip & Ip info
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
                List<String>[] ips = new List[]{new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                        new ArrayList<>(), new ArrayList<>()};
                List<String> lore = new ArrayList<>();
                ResultSet rs = BukkitStarter.plugin.getDB()
                        .selectQuery(String.format("SELECT ip FROM ipLookup WHERE uuid = '%s';", uuid.toString()));
                try {
                    ips[0].add(ColorUtil.replaceColors("&7IP"));
                    ips[1].add(ColorUtil.replaceColors("&7City"));
                    ips[2].add(ColorUtil.replaceColors("&7Region"));
                    ips[3].add(ColorUtil.replaceColors("&7Country"));
                    ips[4].add(ColorUtil.replaceColors("&7ISP"));
                    ips[0].add("");
                    ips[1].add("");
                    ips[2].add("");
                    ips[3].add("");
                    ips[4].add("");
                    while (rs.next()) {
                        String ip = rs.getString("ip");
                        ResultSet ipBan = BukkitStarter.plugin.getDB()
                                .selectQuery(String.format("SELECT *  FROM `ip_ban` WHERE `IP` LIKE '%s';", ip));
                        if (ipBan.next()) {
                            ips[0].add(ColorUtil.replaceColors("&e&m%s &cBANNED&7.", ip));
                        } else {
                            ips[0].add(ColorUtil.replaceColors("&e%s&7.", ip));
                        }
                        com.google.gson.JsonObject ipInfo = new com.google.gson.JsonParser()
                                .parse(WebUtil.getResponse(new URL("http://ip-api.com/json/" + ip))).getAsJsonObject();
                        ips[1].add(ColorUtil.replaceColors("&e%s&7.", ipInfo.get("city").getAsString()));
                        ips[2].add(ColorUtil.replaceColors("&e%s&7.", ipInfo.get("regionName").getAsString()));
                        ips[3].add(ColorUtil.replaceColors("&e%s&7.", ipInfo.get("country").getAsString()));
                        ips[4].add(ColorUtil.replaceColors("&e%s&7.", ipInfo.get("isp").getAsString()));
                        ResultSet usersIp = BukkitStarter.plugin.getDB()
                                .selectQuery(String.format("SELECT i.uuid FROM ipLookup i WHERE i.ip LIKE '%s';", ip));
                        lore.add(ColorUtil.replaceColors("&e%s", ip));
                        while (usersIp.next()) {
                            lore.add(ColorUtil.replaceColors("  &c%s",
                                    iMinePlayer.findPlayer(UUID.fromString(usersIp.getString("uuid"))).getName()));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                ui.addButton(new ButtonListed(
                        ItemUtil.getBuilder(Material.GLASS_BOTTLE).setName(ColorUtil.replaceColors("&bIP's")).build(),
                        ips, 11));
                ui.addButton(
                        new ButtonList(
                                ItemUtil.getBuilder(Material.EXP_BOTTLE)
                                        .setName(ColorUtil.replaceColors("&bLinked users grouped by IP.")).build(),
                                lore, 12));
            });
            // Banlog
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
                List<String> bans = new ArrayList<>();
                List<String> pardons = new ArrayList<>();
                Boolean problem = null;
                ResultSet rs = BukkitStarter.plugin.getDB()
                        .selectQuery(String.format("SELECT b.* FROM ban b WHERE b.UUID LIKE '%s';", uuid.toString()));
                try {
                    while (rs.next()) {
                        bans.add(
                                ColorUtil.replaceColors("&7Banned since &e%s", dateFormat.format(rs.getDate("Timestamp"))));
                        bans.add(ColorUtil.replaceColors("   &7for &e%s &7by &c%s&7.", rs.getString("Reason"),
                                iMinePlayer.findPlayer(UUID.fromString(rs.getString("FromUUID"))).getName()));
                        problem = true;
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                rs = BukkitStarter.plugin.getDB().selectQuery(
                        String.format("SELECT b.* FROM temp_ban b WHERE b.UUID LIKE '%s';", uuid.toString()));
                try {
                    while (rs.next()) {
                        if (rs.getTimestamp("UnbanTimestamp").before(new Date())) {
                            bans.add(ColorUtil.replaceColors("&7&mTempban until &e%s",
                                    dateFormat.format(rs.getTimestamp("UnbanTimestamp"))));
                            bans.add(ColorUtil.replaceColors("   &7&mfor &e%s&7&m by &c%s&7&m.", rs.getString("Reason"),
                                    iMinePlayer.findPlayer(UUID.fromString(rs.getString("FromUUID"))).getName()));
                            if (problem == null) {
                                problem = false;
                            }
                        } else {
                            bans.add(ColorUtil.replaceColors("&7Tempban until &e%s",
                                    DateUtil.timeUntilNow(rs.getTimestamp("UnbanTimestamp"), false)));
                            bans.add(ColorUtil.replaceColors("   &7for &e%s&7 by &c%s&7.", rs.getString("Reason"),
                                    iMinePlayer.findPlayer(UUID.fromString(rs.getString("FromUUID"))).getName()));
                            problem = true;
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                rs = BukkitStarter.plugin.getDB().selectQuery(
                        String.format("SELECT b.* FROM unban_log b WHERE b.who LIKE '%s';", uuid.toString()));
                try {
                    while (rs.next()) {
                        pardons.add(ColorUtil.replaceColors("&7Got unbanned by &c%s &7at &e%s&7.",
                                iMinePlayer.findPlayer(UUID.fromString(rs.getString("from"))).getName(),
                                dateFormat.format(rs.getDate("when"))));
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                List<String> lore = new ArrayList<String>(bans);
                lore.addAll(pardons);
                ui.addButton(new ButtonList(ItemUtil.getBuilder(Material.STAINED_GLASS_PANE)
                        .setDurability((short) (problem == null ? 5 : problem == false ? 4 : 14))
                        .setName(ColorUtil.replaceColors("&4Ban log")).build(), lore, 3));
            });
            // Commands
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
                ui.addButton(
                        new ButtonCommand(
                                ItemUtil.getBuilder(Material.COMPASS)
                                        .setName(ColorUtil.replaceColors("&bTeleport to other.")).build(),
                                27, "tp " + ipl.getName()));
                ui.addButton(new ButtonCommand(ItemUtil.getBuilder(Material.COMPASS)
                        .setName(ColorUtil.replaceColors("&bTeleport to you.")).build(), 28,
                        "tp " + ipl.getName() + " " + sender.getName()));
                ui.addButton(new ButtonCommand(
                        ItemUtil.getBuilder(Material.CHEST).setName(ColorUtil.replaceColors("&bInvsee.")).build(), 29,
                        "invsee " + ipl.getName()));
                ui.addButton(new ButtonCommand(ItemUtil.getBuilder(Material.ENDER_CHEST)
                        .setName(ColorUtil.replaceColors("&bEndersee.")).build(), 30, "endersee " + ipl.getName()));
                ui.addButton(new ButtonCommand(
                        ItemUtil.getBuilder(Material.FEATHER).setName(ColorUtil.replaceColors("&bToggle Fly.")).build(),
                        31, "fly " + ipl.getName()));
                ui.addButton(new ButtonCommand(
                        ItemUtil.getBuilder(Material.POTION).setName(ColorUtil.replaceColors("&bVanish.")).build(), 32,
                        "vanish " + ipl.getName()));
                ui.addButton(new ButtonCommand(ItemUtil.getBuilder(Material.MOB_SPAWNER)
                        .setName(ColorUtil.replaceColors("&bMobcount.")).build(), 33, "mobcount " + ipl.getName()));
                ui.addButton(new ButtonCommand(
                        ItemUtil.getBuilder(Material.BARRIER).setName(ColorUtil.replaceColors("&bKill.")).build(), 34,
                        "kill " + ipl.getName()));
                ui.addButton(
                        new ButtonBrowse(
                                ItemUtil.getBuilder(Material.CHAINMAIL_BOOTS)
                                        .setName(ColorUtil.replaceColors("&bFlycheck.")).build(),
                                35, getFlightContainer(ipl.getUuid())));
            });
            ui.setRefreshRate(20L);
            ui.open((Player) sender);
        });
        return ColorUtil.replaceColors("&7Getting data for player &c%s&7.", args[0]);
    }

    private String reportChat() {
        if (args.length == 0) {
            return ColorUtil.replaceColors("&c/Report [Message]");
        }
        String message = "";
        for (String str : args) {
            message += str + " ";
        }
        message = ColorUtil.replaceColors(message);
        if (message.matches("^\\s*$")) {
            return ColorUtil.replaceColors("&c/Report [Message]");
        }
        PlayerUtil.sendGlobalAdmin(String.format(reportChatFormat, sender.getName(), message));
        return ColorUtil.replaceColors("&7Message reported!");
    }

    private String adminChat() {
        if (!sender.hasPermission("iMine.adminChat")) {
            return ColorUtil.replaceColors("&cUse &e/report &cfor contacting admins");
        }
        if (args.length == 0) {
            return ColorUtil.replaceColors("&c/Admin [Message]");
        }
        String message = "";
        for (String str : args) {
            message += str + " ";
        }
        message = ColorUtil.replaceColors(message);
        if (message.matches("^\\s*$")) {
            return ColorUtil.replaceColors("&c/Admin [Message]");
        }
        PlayerUtil.sendGlobalAdmin(String.format(adminChatFormat, sender.getName(), message));
        return "";
    }

    private String banrichtlijn() {
        sender.sendMessage(ColorUtil.replaceColors("&4&lBanRichtlijn"));
        sender.sendMessage("   ");
        sender.sendMessage(ColorUtil.replaceColors("&cGriefing &6- &aPermanent ban"));
        sender.sendMessage(ColorUtil.replaceColors("&cHacks &6- &aPermanent ban"));
        sender.sendMessage(ColorUtil.replaceColors("&cBedreigen &6- &a2weken ban"));
        sender.sendMessage(ColorUtil.replaceColors("&cExtreem schelden &6- &a48 uur ban"));
        sender.sendMessage(ColorUtil.replaceColors("&cReclame maken &6- &a12-24 uur ban"));
        sender.sendMessage(
                ColorUtil.replaceColors("&cOngepast taalgebruik &6- &aWaarschuwing (kick), daarna 2-4 uur ban"));
        sender.sendMessage(ColorUtil.replaceColors("&cSpam &6- &aWaarschuwing (kick), daarna 2-4 uur ban"));
        sender.sendMessage("   ");
        sender.sendMessage(ColorUtil.replaceColors("&eBedenk je ban verstandig en zet er een DUIDELIJKE reden bij."));
        return ColorUtil.replaceColors("&7Mocht je dit niet kunnen, geef dit door aan je leidinggevende!");
    }

    private String mchistory() {
        if (sender.hasPermission("iMine.mchistory")) {
            if (args.length > 0) {
                List<UUID> uuidsLike = PlayerUtil.getUuidsLike(args[0]);
                for (final UUID foundUUID : uuidsLike) {
                    Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, new NameLookup(foundUUID, true));
                }
                return "";
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String fly() {
        if (sender.hasPermission("iMine.fly")) {
            Player pl = null;
            if (args.length == 0) {
                if (sender instanceof Player) {
                    pl = (Player) sender;
                } else {
                    return CommandUtil.noPlayer();
                }
            } else {
                pl = PlayerUtil.getOnline(args[0]);
            }
            if (pl != null) {
                pl.setAllowFlight(!pl.getAllowFlight());
                pl.setFlying(pl.getAllowFlight());
                if (sender != pl) {
                    pl.sendMessage(
                            ColorUtil.replaceColors("&7You &e%s&7 fly now.", (pl.getAllowFlight() ? "can" : "can't")));
                }
                return ColorUtil.replaceColors("&7Player &c%s&7 &e%s&7 fly now.", pl.getName(),
                        (pl.getAllowFlight() ? "can" : "can't"));
            } else {
                return CommandUtil.noOnline(args[0]);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String plugin() {
        Plugin[] plugins = Bukkit.getPluginManager().getPlugins();
        TextComponent extra, message = new TextComponent("");
        extra = new TextComponent("Plugins (" + plugins.length + "): ");
        message.addExtra(extra);
        for (Plugin plugin : plugins) {
            extra = new TextComponent(plugin.getName());
            extra.setColor(net.md_5.bungee.api.ChatColor.GREEN);
            try {
                Method m = JavaPlugin.class.getDeclaredMethod("getFile");
                m.setAccessible(true);
                File f = (File) m.invoke(plugin);
                extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                        new ComponentBuilder(plugin.getDescription().getVersion()).append("\n\n" + f.getName())
                                .create()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            message.addExtra(extra);
            if (plugin != plugins[plugins.length - 1]) {
                extra = new TextComponent(", ");
                extra.setColor(net.md_5.bungee.api.ChatColor.WHITE);
                message.addExtra(extra);
            }
        }
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(message);
            return "";
        } else {
            return message.toPlainText();
        }
    }

    private String mute() {
        if (args.length != 0 && sender.hasPermission("iMine.mute")) {
            OfflinePlayer pl = PlayerUtil.getOflline(args[0]);
            if (pl != null) {
                BukkitListener.toggleMuted(pl);
                return ColorUtil.replaceColors("&c%s&7 is now &e%s&7.", pl.getName(),
                        (BukkitListener.isMuted(pl) ? "muted" : "unmuted"));
            } else {
                return CommandUtil.noOnline(args[0]);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String hub() {
        if (args.length == 0) {
            if (sender instanceof Player) {
                PlayerUtil.sendPlayerToServer((Player) sender, "hub");
                return ColorUtil.replaceColors("&7To the hub!");
            } else {
                return CommandUtil.noPlayer();
            }
        } else {
            Arrays.sort(SERVERS, new StringSearchSorter(args[0]));
            if (args.length == 1 && sender.hasPermission("iMine.hub." + SERVERS[0])) {
                if (sender instanceof Player) {
                    PlayerUtil.sendPlayerToServer((Player) sender, SERVERS[0]);
                    return ColorUtil.replaceColors("&7To the %s!", SERVERS[0]);
                } else {
                    return CommandUtil.noPlayer();
                }
            } else if (args.length == 2 && sender.hasPermission("iMine.hub." + SERVERS[0] + ".other")) {
                Player pl = PlayerUtil.getOnline(args[1]);
                if (pl != null) {
                    PlayerUtil.sendPlayerToServer(pl, SERVERS[0]);
                    return ColorUtil.replaceColors("&7Sended '&c%s&7' to %s!", pl.getName(), SERVERS[0]);
                } else {
                    return CommandUtil.noOnline(args[1]);
                }
            } else {
                return CommandUtil.noPermission(command);
            }
        }
    }

    private String dev() {
        if (sender.hasPermission("iMine.dev")) {
            BukkitStarter.plugin.devMode = !BukkitStarter.plugin.devMode;
            if (BukkitStarter.plugin.devMode) {
                sender.sendMessage("Devolpermodus is now enabled!");
                for (Player pl : new ArrayList<>(Bukkit.getOnlinePlayers())) {
                    if (!pl.hasPermission("iMine.dev")) {
                        PlayerUtil.sendPlayerToServer(pl, "hub");
                    }
                }
                return ColorUtil.replaceColors("&7The server is now &eprivate&7.");
            } else {
                return ColorUtil.replaceColors("&7The server is now &epublic&7.");
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String lagdebug() {
        if (sender.hasPermission("iMine.lagdebug")) {
            if (args.length == 0) {
                sender.sendMessage(ColorUtil.replaceColors("&7All mobs, grouped by &eworld&7."));
                for (World w : Bukkit.getWorlds()) {
                    sender.sendMessage(ColorUtil.replaceColors("&7Mobs in world '&e%s&7' [&c%d&7]", w.getName(),
                            w.getEntities().size()));
                    Map<Class<? extends Entity>, List<Entity>> countMap = new HashMap<>();
                    for (Entity e : w.getEntities()) {
                        if (!countMap.containsKey(e.getClass())) {
                            countMap.put(e.getClass(), new ArrayList<Entity>());
                        }
                        countMap.get(e.getClass()).add(e);
                    }
                    for (Class<? extends Entity> entityClass : MapCountSorter.getOrder(countMap, Sort.DESC)) {
                        sender.sendMessage(ColorUtil.replaceColors("  &c%d &7- &e%ss", countMap.get(entityClass).size(),
                                entityClass.getSimpleName().replace("Craft", "")));
                    }
                }
                return ColorUtil.replaceColors("&7 ---------------");
            } else {
                if (args[0].equalsIgnoreCase("players")) {
                    sender.sendMessage(ColorUtil.replaceColors("&7All mobs, grouped by &eplayers&7."));
                    Map<String, List<Class<? extends Entity>>> countMap = new HashMap<>();
                    for (World w : Bukkit.getWorlds()) {
                        for (Entity e : w.getEntities()) {
                            double distance = Math.pow(10D, 4D);
                            Player distancePlayer = null;
                            for (Player pl : Bukkit.getOnlinePlayers()) {
                                if (e.getLocation().getWorld().equals(pl.getLocation().getWorld())) {
                                    double plDistance = e.getLocation().distance(pl.getLocation());
                                    if (e.getLocation().distance(pl.getLocation()) < distance) {
                                        distance = plDistance;
                                        distancePlayer = pl;
                                    }
                                }
                            }
                            String name = (distancePlayer != null ? distancePlayer.getName() : "Spawn Chunks");
                            if (!countMap.containsKey(name)) {
                                countMap.put(name, new ArrayList<Class<? extends Entity>>());
                            }
                            countMap.get(name).add(e.getClass());
                        }
                    }
                    for (String plName : MapCountSorter.getOrder(countMap, Sort.DESC)) {
                        sender.sendMessage(
                                ColorUtil.replaceColors("  &c%d &7- &c%ss", countMap.get(plName).size(), plName));
                    }
                    return ColorUtil.replaceColors("&7 ---------------");
                } else {
                    sender.sendMessage(ColorUtil.replaceColors("&7All entitys near player(s) &c&l%s&7.", args[0]));
                    Map<Class<? extends Entity>, List<Entity>> countMap = new HashMap<>();
                    List<Player> argsPlayers = PlayerUtil.getAllOnline(args[0]);
                    for (World w : Bukkit.getWorlds()) {
                        for (Entity e : w.getEntities()) {
                            double distance = Math.pow(10D, 4D);
                            Player distancePlayer = null;
                            for (Player plw : Bukkit.getOnlinePlayers()) {
                                if (e.getLocation().getWorld().equals(plw.getLocation().getWorld())) {
                                    double plDistance = e.getLocation().distance(plw.getLocation());
                                    if (e.getLocation().distance(plw.getLocation()) < distance) {
                                        distance = plDistance;
                                        distancePlayer = plw;
                                    }
                                }
                            }
                            if (argsPlayers.contains(distancePlayer)) {
                                if (!countMap.containsKey(e.getClass())) {
                                    countMap.put(e.getClass(), new ArrayList<Entity>());
                                }
                                countMap.get(e.getClass()).add(e);
                            }
                        }
                    }
                    boolean hasSend = false;
                    for (Class<? extends Entity> entityClass : MapCountSorter.getOrder(countMap, Sort.DESC)) {
                        sender.sendMessage(ColorUtil.replaceColors("  &c%d &7- &e%s", countMap.get(entityClass).size(),
                                entityClass.getSimpleName().replace("Craft", "")));
                        hasSend = true;
                    }
                    if (!hasSend) {
                        sender.sendMessage(ColorUtil.replaceColors("  &cNo mobs near '&e%s&c'.", args[0]));
                    }
                    return ColorUtil.replaceColors("&7 ---------------");
                }
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String tp() {
        if (sender.hasPermission("iMine.tp")) {
            if (args.length == 1) {
                if (sender instanceof Player) {
                    Player pl = PlayerUtil.getOnline(args[0]);
                    if (pl != null) {
                        ((Player) sender).teleport(pl, TeleportCause.COMMAND);
                        return ColorUtil.replaceColors("&7Teleporting to &c%s&7.", pl.getName());
                    } else {
                        return CommandUtil.noOnline(args[0]);
                    }
                } else {
                    return CommandUtil.noPlayer();
                }
            } else if (args.length == 2) {
                Player who = PlayerUtil.getOnline(args[0]);
                Player target = PlayerUtil.getOnline(args[1]);
                if (who != null) {
                    if (target != null) {
                        who.teleport(target, TeleportCause.COMMAND);
                        who.sendMessage(ColorUtil.replaceColors("&7Teleporting"));
                        return ColorUtil.replaceColors("&7Teleported &c%s&7 to &c%s&7.", who.getName(),
                                target.getName());
                    } else {
                        return CommandUtil.noOnline(args[1]);
                    }
                } else {
                    return CommandUtil.noOnline(args[0]);
                }
            } else if (args.length == 3 || args.length == 4) {
                int faultArg = -1;
                double[] coords = new double[args.length];
                for (int i = 0; i < args.length; i++) {
                    try {
                        coords[i] = Double.parseDouble(args[i]);
                    } catch (Exception ex) {
                        faultArg = i;
                    }
                }
                Player who = null;
                if (faultArg == -1) {
                    if (sender instanceof Player) {
                        who = (Player) sender;
                    } else {
                        return CommandUtil.noPlayer();
                    }
                } else if (faultArg == 0) {
                    who = PlayerUtil.getOnline(args[0]);
                }
                if (who == null) {
                    return CommandUtil.noOnline(args[0]);
                }
                World world = who.getWorld();
                if ((args.length == 4 && faultArg == -1) || (args.length == 5 && faultArg == 0)) {
                    try {
                        if (who == sender) {
                            world = Bukkit.getWorlds().get((int) coords[3]);
                        } else {
                            world = Bukkit.getWorlds().get((int) coords[4]);
                        }
                    } catch (Exception ex) {
                        return ColorUtil.replaceColors("&cThere is no world with id '&c%s&7'.",
                                ((who == sender) ? args[3] : args[4]));
                    }
                }
                who.sendMessage(ColorUtil.replaceColors("&7Teleporting"));
                if (who == sender) {
                    who.teleport(new Location(world, coords[0], coords[1], coords[2]), TeleportCause.COMMAND);
                    return ColorUtil.replaceColors("&7Teleported to &e%d,%d,%d&7 in World &e%s&7.", (int) coords[0],
                            (int) coords[1], (int) coords[2], world.getName());
                } else {
                    who.teleport(new Location(world, coords[1], coords[2], coords[3]), TeleportCause.COMMAND);
                    return ColorUtil.replaceColors("&7Teleported &c%s&7 to &e%d,%d,%d&7 in World &e%s&7.",
                            who.getName(), (int) coords[0], (int) coords[1], (int) coords[2], world.getName());
                }
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String gm() {
        if (sender.hasPermission("iMine.gm")) {
            if (args.length > 0) {
                GameMode set = null;
                try {
                    int gm = Integer.parseInt(args[0]);
                    if (gm == 0) {
                        gm = 1;
                    } else if (gm == 1) {
                        gm = 0;
                    }
                    set = GameMode.values()[gm];
                } catch (Exception ex) {
                    for (GameMode gm : GameMode.values()) {
                        if (gm.name().toLowerCase().contains(args[0].toLowerCase())) {
                            set = gm;
                            break;
                        }
                    }
                }
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        if (set != null) {
                            ((Player) sender).setGameMode(set);
                            return ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", StringUtil.readableEnum(set));
                        } else {
                            return ColorUtil.replaceColors("&cThere is no gamemode found with '&e%s&c'.", args[0]);
                        }
                    } else {
                        return CommandUtil.noPlayer();
                    }
                } else if (args.length == 2) {
                    Player who = PlayerUtil.getOnline(args[1]);
                    if (who != null) {
                        if (set != null) {
                            who.setGameMode(set);
                            if (sender != who) {
                                who.sendMessage(
                                        ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", StringUtil.readableEnum(set)));
                            }
                            return ColorUtil.replaceColors("&7Set gamemode to &e%s&7 for &c%s&7.",
                                    StringUtil.readableEnum(set), who.getName());
                        } else {
                            return ColorUtil.replaceColors("&cThere is no gamemode found with '&e%s&c'.", args[0]);
                        }
                    } else {
                        return CommandUtil.noOnline(args[0]);
                    }
                } else {
                    return CommandUtil.noOption(command, args);
                }
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String gmx() {
        if (sender.hasPermission("iMine.gm")) {
            GameMode set = null;
            try {
                int gm = Integer.parseInt(command.charAt(2) + "");
                if (gm == 0) {
                    gm = 1;
                } else if (gm == 1) {
                    gm = 0;
                }
                set = GameMode.values()[gm];
            } catch (Exception ex) {
                return ColorUtil.replaceColors("&cThat is not a gamemode!");
            }
            if (args.length == 0) {
                if (sender instanceof Player) {
                    ((Player) sender).setGameMode(set);
                    return ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", StringUtil.readableEnum(set));
                } else {
                    return CommandUtil.noPlayer();
                }
            } else if (args.length == 1) {
                Player who = PlayerUtil.getOnline(args[0]);
                if (who != null) {
                    who.setGameMode(set);
                    if (who != sender) {
                        who.sendMessage(
                                ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", StringUtil.readableEnum(set)));
                    }
                    return ColorUtil.replaceColors("&7Set gamemode to &e%s&7 for &c%s&7.", StringUtil.readableEnum(set),
                            who.getName());
                } else {
                    return CommandUtil.noOnline(args[0]);
                }
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String speed() {
        if (sender.hasPermission("iMine.speed")) {
            if (args.length > 0) {
                float speed = 0;
                try {
                    speed = Math.min(Math.abs(Float.parseFloat(args[0]) * 0.2F), 1F);
                } catch (Exception ex) {
                    return ColorUtil.replaceColors("&e%s &cis not a number.", args[0]);
                }
                if (args.length == 1) {
                    if (sender instanceof Player) {
                        Player pl = ((Player) sender);
                        if (pl.isFlying()) {
                            pl.setFlySpeed(speed);
                            return ColorUtil.replaceColors("&7Fly speed set to &c%s&7.", args[0]);
                        } else {
                            pl.setWalkSpeed(speed);
                            return ColorUtil.replaceColors("&7Walk speed set to &c%s&7.", args[0]);
                        }
                    } else {
                        return CommandUtil.noPlayer();
                    }
                } else if (args.length == 2) {
                    if (sender instanceof Player) {
                        Player pl = ((Player) sender);
                        if (args[1].toLowerCase().contains("f")) {
                            pl.setFlySpeed(speed);
                            return ColorUtil.replaceColors("&7Fly speed set to &c%s&7.", args[0]);
                        } else {
                            pl.setWalkSpeed(speed);
                            return ColorUtil.replaceColors("&7Walk speed set to &c%s&7.", args[0]);
                        }
                    } else {
                        return CommandUtil.noPlayer();
                    }
                } else if (args.length == 3) {
                    Player who = PlayerUtil.getOnline(args[2]);
                    if (who != null) {
                        if (args[1].toLowerCase().contains("f")) {
                            who.setFlySpeed(speed);
                            who.sendMessage(ColorUtil.replaceColors("&7Fly speed set to &c%s&7.", args[0]));
                            return ColorUtil.replaceColors("&7Speed set.");
                        } else {
                            who.setWalkSpeed(speed);
                            who.sendMessage(ColorUtil.replaceColors("&7Walk speed set to &c%s&7.", args[0]));
                            return ColorUtil.replaceColors("&7Speed set.");
                        }
                    } else {
                        return CommandUtil.noOnline(args[2]);
                    }
                } else {
                    return CommandUtil.noOption(command, args);
                }
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String msg() {
        if (sender.hasPermission("iMine.msg")) {
            if (args.length > 1) {
                String target = PlayerUtil.getNameLike(args[0]);
                if (target == null) {
                    return CommandUtil.noOnline(args[0]);
                }
                String msg = "";
                for (int i = 1; i < args.length; i++) {
                    msg += args[i] + " ";
                }
                msg = ColorUtil.replaceColors(msg).trim();
                Player pl = PlayerUtil.getOnline(target);
                if (pl != null) {
                    if (LAST_SPOKE.containsKey(pl)) {
                        LAST_SPOKE.remove(pl);
                    }
                    LAST_SPOKE.put(pl, sender);
                    if (LAST_SPOKE.containsKey(sender)) {
                        LAST_SPOKE.remove(sender);
                    }
                    LAST_SPOKE.put(sender, pl);
                } else {
                    msg += "*";
                }
                PlayerUtil.sendGlobalTo(target, ColorUtil
                        .replaceColors("&8&oReceived message from &c%s&8&l \u00BB &r&7%s.", sender.getName(), msg));
                return ColorUtil.replaceColors("&8&oSend message to &c%s&8&l \u00BB &r&7%s.", target, msg);
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String invsee() {
        if (sender.hasPermission("iMine.invsee")) {
            if (sender instanceof Player) {
                if (args.length == 0) {
                    ((Player) sender).openInventory(((Player) sender).getInventory());
                    return ColorUtil.replaceColors("&7Opened inventory of &c%s&7.", sender.getName());
                } else if (args.length == 1) {
                    Player target = PlayerUtil.getOnline(args[0]);
                    if (target != null) {
                        Player pl = (Player) sender;
                        pl.openInventory(target.getInventory());
                        return ColorUtil.replaceColors("&7Opened inventory of &c%s&7.", target.getName());
                    } else {
                        return CommandUtil.noOnline(args[0]);
                    }
                } else {
                    return CommandUtil.noOption(command, args);
                }
            } else {
                return CommandUtil.noPlayer();
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String endersee() {
        if (sender.hasPermission("iMine.endersee")) {
            if (sender instanceof Player) {
                if (args.length == 0) {
                    ((Player) sender).openInventory(((Player) sender).getEnderChest());
                    return ColorUtil.replaceColors("&7Opened enderchest of &c%s&7.", sender.getName());
                } else if (args.length == 1) {
                    Player target = PlayerUtil.getOnline(args[0]);
                    if (target != null) {
                        Player pl = (Player) sender;
                        pl.openInventory(target.getEnderChest());
                        return ColorUtil.replaceColors("&7Opened enderchest of &c%s&7.", target.getName());
                    } else {
                        return CommandUtil.noOnline(args[0]);
                    }
                } else {
                    return CommandUtil.noOption(command, args);
                }
            } else {
                return CommandUtil.noPlayer();
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String plr() {
        if ((sender.hasPermission("iMine.dev")) && args.length > 0) {
            Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, new ReloadPlugin());
            return ColorUtil.replaceColors("&7Reloading plugin &e%s&7.", args[0]);
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String returnTP() {
        if (sender.hasPermission("iMine.return")) {
            if (sender instanceof Player) {
                Player sender = (Player) this.sender;
                if (BukkitListener.TP_HISTORY.containsKey(sender.getUniqueId())) {
                    List<Location> locs = BukkitListener.TP_HISTORY.get(sender.getUniqueId());
                    sender.teleport(locs.get(locs.size() - 1));
                    return ColorUtil.replaceColors("&7Teleported.");
                } else {
                    return ColorUtil.replaceColors("&cNo back place found.");
                }
            } else {
                return CommandUtil.noPlayer();
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String vanish() {
        if (sender.hasPermission("iMine.vanish")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player sender = (Player) this.sender;
                    iMinePlayer ipl = iMinePlayer.findPlayer(sender);
                    boolean newVanish = ipl.setVanish(!ipl.isVanished());
                    if (newVanish) {
                        return ColorUtil.replaceColors("&7You are now &evanished&7.");
                    } else {
                        return ColorUtil.replaceColors("&7You are now &evisible&7.");
                    }
                } else {
                    return CommandUtil.noPlayer();
                }
            } else {
                Player target = PlayerUtil.getOnline(args[0]);
                if (target != null) {
                    iMinePlayer ipl = iMinePlayer.findPlayer(target);
                    boolean newVanish = ipl.setVanish(!ipl.isVanished());
                    if (newVanish) {
                        target.sendMessage(ColorUtil.replaceColors("&7You are now vanished."));
                        return ColorUtil.replaceColors("&c%s &7is now vanished.", target.getDisplayName());
                    } else {
                        target.sendMessage(ColorUtil.replaceColors("&7You are now visible."));
                        return ColorUtil.replaceColors("&c%s &7is now visible.", target.getDisplayName());
                    }
                } else {
                    return CommandUtil.noOnline(args[0]);
                }
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String kill() {
        if (sender.hasPermission("iMine.kill")) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player sender = (Player) this.sender;
                    sender.setHealth(0D);
                    return ColorUtil.replaceColors("&7Committed suicide.");
                } else {
                    return CommandUtil.noPlayer();
                }
            } else {
                Player target = PlayerUtil.getOnline(args[0]);
                if (target != null) {
                    target.setHealth(0D);
                    return ColorUtil.replaceColors("&7Assassinated &c%s&7!", target.getName());
                } else {
                    return CommandUtil.noOnline(args[0]);
                }
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String reply() {
        if (sender.hasPermission("iMine.reply")) {
            if (args.length > 0) {
                if (LAST_SPOKE.containsKey(sender)) {
                    CommandSender target = LAST_SPOKE.get(sender);
                    args = (String[]) ArrayUtils.addAll(new String[]{target.getName()}, args);
                    return msg();
                } else {
                    return ColorUtil.replaceColors("&cNobody to reply to.");
                }
            } else {
                return CommandUtil.noOption(command, args);
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String me() {
        if (sender.hasPermission("iMine.me")) {
            if (args.length > 0) {
                String msg = "";
                for (int i = 0; i < args.length; i++) {
                    msg += args[i] + " ";
                }
                msg = msg.substring(0, msg.length() - 1);
                msg = ColorUtil.replaceColors("&6* &7%s &r" + msg, sender.getName());
                Bukkit.broadcastMessage(msg);
                return "";
            } else {
                return ColorUtil.replaceColors("&cNeed a message to tell.");
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    private String world() {
        if (sender.hasPermission("iMine.world")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (args.length == 0) {
                    player.sendMessage(ColorUtil.replaceColors("&7Current World: &e%s", player.getWorld().getName()));
                    player.sendMessage(ColorUtil.replaceColors("&7Available worlds:"));
                    Bukkit.getWorlds().stream()
                            .forEach(w -> player.sendMessage(ColorUtil.replaceColors("  &e%s", w.getName())));
                    return "";
                } else if (args.length == 1 || args.length == 2) {
                    World world;
                    try {
                        world = Bukkit.getWorlds().get(Integer.parseInt(args[0]));
                    } catch (NumberFormatException e) {
                        world = Bukkit.getWorld(args[0]);
                        if (world == null) {
                            if (new File(args[0], Bukkit.getWorldContainer().getPath()).exists()) {
                                if (args.length == 2) {
                                    if (Environment.valueOf(args[1].toUpperCase()) != null) {
                                        world = Bukkit.createWorld(new WorldCreator(args[0])
                                                .environment(Environment.valueOf(args[1].toUpperCase())));
                                    } else {
                                        world = Bukkit
                                                .createWorld(new WorldCreator(args[0]).environment(Environment.NORMAL));
                                    }
                                }
                            }
                        }
                    }
                    if (world != null) {
                        player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
                        return ColorUtil.replaceColors("&7Teleported to world &e%s&7.", world.getName());
                    } else {
                        return ColorUtil.replaceColors("&cNo world with the name '&e%s&c' exists.", args[0]);
                    }
                } else {
                    return CommandUtil.noOption(command, args);
                }
            } else {
                return CommandUtil.noPlayer();
            }
        } else {
            return CommandUtil.noPermission(command);
        }
    }

    public static List<String> onTabComplete(Player sender, String command, String[] args) {
        List<String> ret = new ArrayList<>();
        if (command.equalsIgnoreCase("hub")) {
            if (args.length == 1) {
                for (String server : SERVERS) {
                    if (server.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
                        ret.add(server);
                    }
                }
                Collections.sort(ret, new StringSearchSorter(args[args.length - 1]));
            } else if (args.length == 2) {
                ret.addAll(PlayerUtil.getAllOnlineNames(args[args.length - 1], sender));
            }
        } else if (command.equalsIgnoreCase("tab") && args.length == 1) {
            String[] argumenten = {"top", "bottom", "update"};
            for (String arg : argumenten) {
                if (arg.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
                    ret.add(arg);
                }
            }
            Collections.sort(ret, new StringSearchSorter(args[args.length - 1]));
        } else if (command.equalsIgnoreCase("plr")) {
            for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
                if (plugin.getName().toLowerCase().contains(args[args.length - 1].toLowerCase())) {
                    ret.add(plugin.getName());
                }
            }
            Collections.sort(ret, new StringSearchSorter(args[args.length - 1]));
        } else if (command.equalsIgnoreCase("world")) {
            for (World w : Bukkit.getWorlds()) {
                if (w.getName().startsWith(args[args.length - 1])) {
                    ret.add(w.getName());
                }
            }
            Collections.sort(ret, new StringSearchSorter(args[args.length - 1]));
        } else if ((command.startsWith("gm") && command.length() == 3)
                || (command.equalsIgnoreCase("gm") && args.length == 2)
                || (command.equalsIgnoreCase("mute") && args.length == 1)
                || (command.equalsIgnoreCase("fly") && args.length == 1)
                || (command.equalsIgnoreCase("invsee") && args.length == 1)
                || (command.equalsIgnoreCase("vanish") && args.length == 1) || (command.equalsIgnoreCase("report"))
                || (command.equalsIgnoreCase("tp") && (args.length == 1 || args.length == 2))
                || (command.equalsIgnoreCase("kill") && args.length == 1)
                || (command.equalsIgnoreCase("endersee") && args.length == 1)
                || (command.equalsIgnoreCase("speed") && args.length > 1)) {
            // Online names
            ret.addAll(PlayerUtil.getAllOnlineNames(args[args.length - 1], sender));
        } else if ((command.equalsIgnoreCase("mchistory") && args.length == 1)
                || (command.equalsIgnoreCase("whois") && args.length == 1)
                || (command.equalsIgnoreCase("skull") && args.length == 1)
                || (command.equalsIgnoreCase("flycheck") && args.length == 1) || command.equalsIgnoreCase("reply")
                || command.equalsIgnoreCase("me") || command.equalsIgnoreCase("msg")) {
            // All names
            List<String> names = PlayerUtil.getAllOnlineNames(args[args.length - 1], sender);
            Collections.sort(names, new StringSearchSorter(args[args.length - 1]));
            List<String> offline = new ArrayList<>();
            for (String offlineName : PlayerUtil.getAllOfflineNames(args[args.length - 1], sender)) {
                if (!names.contains(offlineName)) {
                    offline.add(offlineName);
                }
            }
            Collections.sort(offline, new StringSearchSorter(args[args.length - 1]));
            names.addAll(offline);
            ret.addAll(names.subList(0, Math.min(names.size(), 10)));
        } else if (command.equalsIgnoreCase("update")) {
        } else if (command.equalsIgnoreCase("lagdebug")) {
            if ("players".contains(args[args.length - 1])) {
                ret.add("players");
            }
            ret.addAll(PlayerUtil.getAllOnlineNames(args[args.length - 1], sender));
        }
        return ret;
    }

    private class NameLookup implements Runnable {

        private final UUID uuid;
        private List<String> names;
        private boolean sendChat;

        public NameLookup(UUID uuid, boolean sendChat) {
            this.uuid = uuid;
            this.names = new ArrayList<>();
            this.sendChat = sendChat;
        }

        public void run() {
            String request = "";
            try {
                URL url = new URL(
                        "https://api.mojang.com/user/profiles/" + uuid.toString().replaceAll("-", "") + "/names");
                InputStream is = url.openStream();
                Scanner in = new Scanner(is);
                while (in.hasNextLine()) {
                    request += in.nextLine();
                }
                in.close();
                is.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }
            try {
                com.google.gson.JsonArray nameChange = new com.google.gson.JsonParser().parse(request).getAsJsonArray();
                if (nameChange.size() == 0) {
                    getNameInfo(null, 0L);
                } else {
                    for (com.google.gson.JsonElement nameInfo : nameChange) {
                        com.google.gson.JsonObject nameObj = nameInfo.getAsJsonObject();
                        String name = nameObj.get("name").getAsString();
                        String response = null;
                        if (nameObj.has("changedToAt")) {
                            response = getNameInfo(name, nameObj.get("changedToAt").getAsLong());
                        } else {
                            response = getNameInfo(name, 0L);
                        }
                        if (sendChat) {
                            sender.sendMessage(response);
                        }
                        names.add(response);
                    }
                    if (nameChange.size() == 1) {
                        String str = ColorUtil.replaceColors("&8  And he never changed his name.");
                        if (sendChat) {
                            sender.sendMessage(str);
                        }
                        names.add(str);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        public List<String> getNames() {
            return names;
        }

        private String getNameInfo(String name, long time) {
            Date d = null;
            if (time > 0L) {
                d = new Date(time);
                if (name == null) {
                    return ColorUtil.replaceColors("&c  This player has no other names");
                } else {
                    return ColorUtil.replaceColors("&7  Name: '&c%s&7' changed &e%s&7 ago.", name,
                            DateUtil.timeUntilNow(d, true));
                }
            } else {
                return ColorUtil.replaceColors("&7Getting al old playernames from '&c%s&7'.", name);
            }
        }
    }

    private class ReloadPlugin implements Runnable {

        private final String format = ColorUtil.replaceColors("&r&l[&b&lUPDATER&r&l]&r %s");

        @Override
        public void run() {
            Plugin plb = Bukkit.getPluginManager().getPlugin(args[0]);
            if (plb instanceof JavaPlugin) {
                JavaPlugin pl = (JavaPlugin) plb;
                sender.sendMessage(String.format(format, "Overriding to new plugins"));
                Bukkit.getPluginManager().disablePlugin(pl);
                sender.sendMessage(String.format(format,
                        pl.getName() + " is now disabled [" + pl.getDescription().getVersion() + "]"));
                try {
                    Thread.sleep(1500L);
                    File f = PluginReflection.getFile(pl);
                    BukkitStarter.plugin.updatePlugins();
                    Thread.sleep(1500L);
                    plb = Bukkit.getPluginManager().loadPlugin(new File(f.getAbsolutePath()));
                    sender.sendMessage(String.format(format,
                            plb.getName() + " is now reloaded! [" + plb.getDescription().getVersion() + "]"));
                } catch (UnknownDependencyException ex) {
                    sender.sendMessage(
                            ColorUtil.replaceColors("&cPlugin Dependency not correctly: " + ex.getMessage()));
                    ex.printStackTrace();
                } catch (InvalidPluginException ex) {
                    sender.sendMessage(ColorUtil.replaceColors("&cPlugin invalid. because: " + ex.getMessage()));
                    ex.printStackTrace();
                } catch (InvalidDescriptionException ex) {
                    sender.sendMessage(
                            ColorUtil.replaceColors("&cPlugin invalid description. because: " + ex.getMessage()));
                    ex.printStackTrace();
                } catch (Exception ex) {
                    sender.sendMessage(ColorUtil.replaceColors("&cYou just fucked-up: " + ex.getMessage()));
                    ex.printStackTrace();
                }
            } else {
                sender.sendMessage("No plugin with that name.");
            }
        }
    }

    private class FlyCheckButton extends Button {

        private Path path;

        public FlyCheckButton(Path path, int slot) {
            super(ItemUtil.getBuilder(Material.EYE_OF_ENDER)
                    .setLore(path.getFirstPosition().toString(),
                            ColorUtil.replaceColors("&elClick &8teleport to start position."),
                            ColorUtil.replaceColors("&erClick &8to visualize event."))
                    .build(), slot);
            this.path = path;
        }

        @Override
        public ItemStack getItemStack() {
            World w = Bukkit.getWorld(path.getFirstPosition().getWorld());
            if (w == null) {
                return ItemUtil.getBuilder(Material.BARRIER).setName(ColorUtil.replaceColors("&cWrong server."))
                        .build();
            }
            return super.getItemStack();
        }

        @Override
        public void doAction(Player player, Container container, ClickType clickType) {
            World w = Bukkit.getWorld(path.getFirstPosition().getWorld());
            if (w == null) {
                player.sendMessage(ColorUtil.replaceColors("&cThis is not the same world/server as it happend from!"));
                return;
            }
            if (!w.equals(player.getWorld())) {
                player.sendMessage(ColorUtil.replaceColors("&cYou are not in the same world as it happend in!"));
                return;
            }
            if (clickType.isLeftClick()) {
                player.teleport(path.getFirstPosition().toLocation());
            } else if (clickType.isRightClick()) {
                container.close();
                for (int i = 0; i < path.getPositions().length; i++) {
                    final int index = i;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitStarter.plugin, () -> {
                        final ArmorStand as = (ArmorStand) w.spawnEntity(path.getPosition(index).toLocation(),
                                EntityType.ARMOR_STAND);
                        as.setArms(true);
                        as.setBasePlate(false);
                        as.setGravity(false);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitStarter.plugin, () -> {
                            as.remove();
                        }, FlyUtil.getCheckTickDelay() + 2);
                    }, i * FlyUtil.getCheckTickDelay());
                }
                Bukkit.getScheduler().scheduleSyncDelayedTask(BukkitStarter.plugin, () -> {
                    player.sendMessage(ColorUtil.replaceColors(" &7Animation done."));
                }, (path.getPositions().length + 1) * FlyUtil.getCheckTickDelay());
            }
        }
    }
}
