package nl.makertim.essentials;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
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
import nl.imine.api.sorters.MapCountSorter;
import nl.imine.api.sorters.MapCountSorter.Sort;
import nl.imine.api.sorters.StringSearchSorter;
import nl.imine.api.util.ColorUtil;
import nl.imine.api.util.DateUtil;
import nl.imine.api.util.ItemUtil;
import nl.imine.api.util.MktUtil;
import nl.imine.api.util.PlayerUtil;
import nl.imine.api.util.WebUtil;
import nl.makertim.essentials.GitLabAPI.Commit;
import nl.makertim.essentials.GitLabAPI.GitProject;

public class CommandHandler {

	private static final GitLabAPI API = new GitLabAPI();
	private final String adminChatFormat = ColorUtil.replaceColors("&r&l[&a&lADMIN&r&l] &r&7%s &r&l\u00BB &r%s");
	private final String reportChatFormat = ColorUtil.replaceColors("&r&l[&c&lREPORT&r&l] &r&7%s &r&l\u00BB &r%s");
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

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
		String finalAwsner = null;
		if (command.equalsIgnoreCase("hub")) {
			finalAwsner = hub();
		} else if (command.equalsIgnoreCase("dev")) {
			finalAwsner = dev();
		} else if (command.equalsIgnoreCase("mute")) {
			finalAwsner = mute();
		} else if (command.equalsIgnoreCase("tp")) {
			finalAwsner = tp();
		} else if (command.equalsIgnoreCase("fly")) {
			finalAwsner = fly();
		} else if (command.equalsIgnoreCase("tab")) {
			finalAwsner = tab();
		} else if (command.equalsIgnoreCase("lagdebug")) {
			finalAwsner = lagdebug();
		} else if (command.equalsIgnoreCase("gm")) {
			finalAwsner = gm();
		} else if (command.startsWith("gm") && command.length() == 3) {
			finalAwsner = gmx();
		} else if (command.equalsIgnoreCase("speed")) {
			finalAwsner = speed();
		} else if (command.equalsIgnoreCase("whois")) {
			finalAwsner = whois();
		} else if (command.equalsIgnoreCase("banrichtlijn")) {
			finalAwsner = banrichtlijn();
		} else if (command.equalsIgnoreCase("msg")) {
			finalAwsner = msg();
		} else if (command.equalsIgnoreCase("invsee")) {
			finalAwsner = invsee();
		} else if (command.equalsIgnoreCase("endersee")) {
			finalAwsner = endersee();
		} else if (command.equalsIgnoreCase("mchistory")) {
			finalAwsner = mchistory();
		} else if (command.equalsIgnoreCase("git")) {
			finalAwsner = git();
		} else if (command.equalsIgnoreCase("plr")) {
			finalAwsner = plr();
		} else if (command.equalsIgnoreCase("returntp")) {
			finalAwsner = returnTP();
		} else if (command.equalsIgnoreCase("vanish")) {
			finalAwsner = vanish();
		} else if (command.equalsIgnoreCase("kill")) {
			finalAwsner = kill();
		} else if (command.equalsIgnoreCase("reply")) {
			finalAwsner = reply();
		} else if (command.equalsIgnoreCase("me")) {
			finalAwsner = me();
		} else if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("plugin")
				|| command.equalsIgnoreCase("plugins")) {
			finalAwsner = plugin();
		} else if (command.equalsIgnoreCase("world")) {
			finalAwsner = world();
		} else if (command.equalsIgnoreCase("report")) {
			finalAwsner = reportChat();
		} else if (command.equalsIgnoreCase("admin")) {
			finalAwsner = adminChat();
		} else if (command.equalsIgnoreCase("update")) {
			if (sender instanceof Player) {
				((Player) sender).performCommand("reload");
			} else {
				Bukkit.reload();
			}
		}
		if (finalAwsner == null) {
			return false;
		} else {
			if (!finalAwsner.trim().isEmpty()) {
				sender.sendMessage(finalAwsner);
			}
			return true;
		}
	}

	private String whois() {
		if (args.length == 0) {
			return noOption();
		}
		if (!(sender instanceof Player)) {
			return noPlayer();
		}
		if (!sender.hasPermission("iMine.whois")) {
			return noPermission();
		}
		UUID uuid = PlayerUtil.getUUID(args[0], false);
		if (uuid == null) {
			return noOnline(args[0]);
		}
		Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
			iMinePlayer ipl = iMinePlayer.findPlayer(uuid);
			NameLookup nl = new NameLookup(uuid, false);
			nl.run();
			List<String> names = nl.getNames();
			List<String> ips = new ArrayList<>();
			List<String> ipsinfo = new ArrayList<>();
			ResultSet rs = BukkitStarter.plugin.getDB()
					.selectQuery(String.format("SELECT ip FROM ipLookup WHERE uuid = '%s';", uuid.toString()));
			try {
				while (rs.next()) {
					String ip = rs.getString("ip");
					ips.add(ip);
					com.google.gson.JsonObject ipInfo = new com.google.gson.JsonParser()
							.parse(WebUtil.getResponse(new URL("http://ip-api.com/json/" + ip))).getAsJsonObject();
					ipsinfo.add(ColorUtil.replaceColors("&e%s %s %s &7(&c%s&7).", ipInfo.get("city").getAsString(),
						ipInfo.get("regionName").getAsString(), ipInfo.get("country").getAsString(),
						ipInfo.get("isp").getAsString()));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			Container ui = GuiManager.getInstance().createContainer(ipl.getName(), 27, false, false);
			SkullMeta meta = (SkullMeta) Bukkit.getItemFactory().getItemMeta(Material.SKULL_ITEM);
			meta.setOwner(ipl.getName());
			ui.addButton(new Button(ui, ItemUtil.getBuilder(Material.SKULL_ITEM, meta).setDurability((short) 3)
					.setName(ColorUtil.replaceColors("&7%s", ipl.getName())).build(), 4));
			int index = 9;
			ui.addButton(new Button(ui, ItemUtil.getBuilder(Material.BOOK_AND_QUILL)
					.setName(ColorUtil.replaceColors("&cName history")).setLore(names).build(), index++));
			ui.addButton(new Button(ui,
					ItemUtil.getBuilder(Material.SIGN).setName(ColorUtil.replaceColors("&cLast seen"))
							.setLore(new String[]{
									ColorUtil.replaceColors("&7Last seen: &c%s&7.", dateFormat.format(ipl.getDate()))})
					.build(), index++));
			ui.addButton(new Button(ui, ItemUtil.getBuilder(Material.EXP_BOTTLE)
					.setName(ColorUtil.replaceColors("&cIP's")).setLore(ips).build(), index++));
			ui.addButton(new Button(ui, ItemUtil.getBuilder(Material.GLASS_BOTTLE)
					.setName(ColorUtil.replaceColors("&cIP info")).setLore(ipsinfo).build(), index++));
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
			return ColorUtil.replaceColors("&cUse /report for contacting admins");
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
		sender.sendMessage(ColorUtil.replaceColors("&7Mocht je dit niet kunnen, geef dit door aan je leidinggevende!"));
		return "";
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
				return ColorUtil.replaceColors("&cNeed player to lookup");
			}
		} else {
			return noPermission();
		}
	}

	private String tab() {
		if (sender.hasPermission("iMine.tabchange")) {
			TabListHandler tlh = BukkitStarter.plugin.getTLH();
			if (args.length > 1) {
				String msg = "";
				for (int i = 1; i < args.length; i++) {
					msg += args[i] + " ";
				}
				msg = msg.trim();
				msg = ColorUtil.replaceColors(msg);
				if (args[0].equalsIgnoreCase("top")) {
					tlh.updateTop(msg);
				} else if (args[0].equalsIgnoreCase("bottom")) {
					tlh.updateBottom(msg);
				} else {
					return ColorUtil.replaceColors("&cCant update '%s' to %s", args[0], args[1]);
				}
				return ColorUtil.replaceColors("&6Tab %s updated to &r%s", args[0], msg);
			} else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
				tlh.updateAll();
				return ColorUtil.replaceColors("&7Tab updated!");
			} else {
				return noOption();
			}
		} else {
			return noPermission();
		}
	}

	private String fly() {
		if (sender.hasPermission("iMine.fly")) {
			Player pl = null;
			if (args.length == 0) {
				if (sender instanceof Player) {
					pl = (Player) sender;
				} else {
					return noPlayer();
				}
			} else {
				pl = PlayerUtil.getOnline(args[0]);
			}
			if (pl != null) {
				pl.setAllowFlight(!pl.getAllowFlight());
				pl.setFlying(pl.getAllowFlight());
				if (sender != pl) {
					pl.sendMessage(
						ColorUtil.replaceColors("&7You %s&7 fly now.", (pl.getAllowFlight() ? "&6can" : "&4can't")));
				}
				return ColorUtil.replaceColors("&7Player &c%s&7 %s&7 fly now.", pl.getName(),
					(pl.getAllowFlight() ? "&6can" : "&4can't"));
			} else {
				return noOnline(args[0]);
			}
		} else {
			return noPermission();
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
				return ColorUtil.replaceColors("&c%s&7 is now %s&7.", pl.getName(),
					(BukkitListener.isMuted(pl) ? "&cmuted." : "&6unmuted."));
			} else {
				return noOnline(args[0]);
			}
		} else {
			return noPermission();
		}
	}

	private String hub() {
		if (args.length == 0) {
			if (sender instanceof Player) {
				PlayerUtil.sendPlayerToServer((Player) sender, "hub");
				return ColorUtil.replaceColors("&7To the hub!");
			} else {
				return noPlayer();
			}
		} else if (sender.hasPermission("iMine.hub")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					PlayerUtil.sendPlayerToServer((Player) sender, args[0]);
					return ColorUtil.replaceColors("&7To the %s!", args[0]);
				} else {
					return noPlayer();
				}
			} else if (args.length == 2) {
				Player pl = PlayerUtil.getOnline(args[1]);
				if (pl != null) {
					PlayerUtil.sendPlayerToServer(pl, args[0]);
					return ColorUtil.replaceColors("&7Sended '&c%s&7' to %s!", pl.getName(), args[0]);
				} else {
					return noOnline(args[1]);
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
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
				return ColorUtil.replaceColors("&7The server is now &cprivate&7.");
			} else {
				return ColorUtil.replaceColors("&7The server is now &6public&7.");
			}
		} else {
			return noPermission();
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
							ColorUtil.replaceColors("  &c%d &7- &e%ss", countMap.get(plName).size(), plName));
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
			return noPermission();
		}
	}

	private String tp() {
		if (sender.hasPermission("iMine.tp")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					Player pl = PlayerUtil.getOnline(args[0]);
					if (pl != null) {
						((Player) sender).teleport(pl);
						return ColorUtil.replaceColors("&7Teleporting to &c%s&7.", pl.getName());
					} else {
						return noOnline(args[0]);
					}
				} else {
					return noPlayer();
				}
			} else if (args.length == 2) {
				Player who = PlayerUtil.getOnline(args[0]);
				Player target = PlayerUtil.getOnline(args[1]);
				if (who != null) {
					if (target != null) {
						who.teleport(target);
						who.sendMessage(ColorUtil.replaceColors("&7Teleporting"));
						return ColorUtil.replaceColors("&7Teleported &c%s&7 to &c%s&7.", who.getName(),
							target.getName());
					} else {
						return noOnline(args[1]);
					}
				} else {
					return noOnline(args[0]);
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
						return noPlayer();
					}
				} else if (faultArg == 0) {
					who = PlayerUtil.getOnline(args[0]);
				}
				if (who == null) {
					return noOnline(args[0]);
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
					who.teleport(new Location(world, coords[0], coords[1], coords[2]));
					return ColorUtil.replaceColors("&7Teleported to &e%f,%f,%f&7 in World &e%s&7.", coords[0],
						coords[1], coords[2], world.getName());
				} else {
					who.teleport(new Location(world, coords[1], coords[2], coords[3]));
					return ColorUtil.replaceColors("&7Teleported &c%s&7 to &e%f,%f,%f&7 in World &e%s&7.",
						who.getName(), coords[0], coords[1], coords[2], world.getName());
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
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
							return ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", MktUtil.readableEnum(set));
						} else {
							return ColorUtil.replaceColors("&cThere is no gamemode found with '&e%s&c'.", args[0]);
						}
					} else {
						return noPlayer();
					}
				} else if (args.length == 2) {
					Player who = PlayerUtil.getOnline(args[1]);
					if (who != null) {
						if (set != null) {
							who.setGameMode(set);
							if (sender != who) {
								who.sendMessage(
									ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", MktUtil.readableEnum(set)));
							}
							return ColorUtil.replaceColors("&7Set gamemode to &e%s&7 for &c%s&7.",
								MktUtil.readableEnum(set), who.getName());
						} else {
							return ColorUtil.replaceColors("&cThere is no gamemode found with '&e%s&c'.", args[0]);
						}
					} else {
						return noOnline(args[0]);
					}
				} else {
					return noOption();
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
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
					return ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", MktUtil.readableEnum(set));
				} else {
					return noPlayer();
				}
			} else if (args.length == 1) {
				Player who = PlayerUtil.getOnline(args[0]);
				if (who != null) {
					who.setGameMode(set);
					if (who != sender) {
						who.sendMessage(
							ColorUtil.replaceColors("&7Set gamemode to &e%s&7.", MktUtil.readableEnum(set)));
					}
					return ColorUtil.replaceColors("&7Set gamemode to &e%s&7 for &c%s&7.", MktUtil.readableEnum(set),
						who.getName());
				} else {
					return noOnline(args[0]);
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
		}
	}

	private String speed() {
		if (sender.hasPermission("iMine.speed")) {
			if (args.length > 0) {
				float speed = 0;
				try {
					speed = Math.min(Math.abs(Float.parseFloat(args[0]) * 0.2F), 1F);
				} catch (Exception ex) {
					return ColorUtil.replaceColors("&c%s is not a number.", args[0]);
				}
				if (args.length == 1) {
					if (sender instanceof Player) {
						Player pl = ((Player) sender);
						if (pl.isFlying()) {
							pl.setFlySpeed(speed);
							return ColorUtil.replaceColors("&7Fly speed set to %s&7.", args[0]);
						} else {
							pl.setWalkSpeed(speed);
							return ColorUtil.replaceColors("&7Walk speed set to %s&7.", args[0]);
						}
					} else {
						return noPlayer();
					}
				} else if (args.length == 2) {
					if (sender instanceof Player) {
						Player pl = ((Player) sender);
						if (args[1].toLowerCase().contains("f")) {
							pl.setFlySpeed(speed);
							return ColorUtil.replaceColors("&7Fly speed set to %s&7.", args[0]);
						} else {
							pl.setWalkSpeed(speed);
							return ColorUtil.replaceColors("&7Walk speed set to %s&7.", args[0]);
						}
					} else {
						return noPlayer();
					}
				} else if (args.length == 3) {
					Player who = PlayerUtil.getOnline(args[2]);
					if (who != null) {
						if (args[1].toLowerCase().contains("f")) {
							who.setFlySpeed(speed);
							who.sendMessage(ColorUtil.replaceColors("&7Speed set."));
							return ColorUtil.replaceColors("&7Fly speed set to %s&7.", args[0]);
						} else {
							who.setWalkSpeed(speed);
							who.sendMessage(ColorUtil.replaceColors("&7Speed set."));
							return ColorUtil.replaceColors("&7Walk speed set to %s&7.", args[0]);
						}
					} else {
						return noOnline(args[2]);
					}
				} else {
					return noOption();
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
		}
	}

	private String msg() {
		if (sender.hasPermission("iMine.msg")) {
			if (args.length > 1) {
				String target = PlayerUtil.getNameLike(args[0]);
				if (target == null) {
					return noOnline(args[0]);
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
				return noOption();
			}
		} else {
			return noPermission();
		}
	}

	private String invsee() {
		if (sender.hasPermission("iMine.invsee")) {
			if (sender instanceof Player) {
				if (args.length == 0) {
					((Player) sender).openInventory(((Player) sender).getInventory());
					return ColorUtil.replaceColors("&7Opend inventory of &c%s&7.", sender.getName());
				} else if (args.length == 1) {
					Player target = PlayerUtil.getOnline(args[0]);
					if (target != null) {
						Player pl = (Player) sender;
						pl.openInventory(target.getInventory());
						return ColorUtil.replaceColors("&7Opend inventory of &c%s&7.", pl.getName());
					} else {
						return noOnline(args[0]);
					}
				} else {
					return noOption();
				}
			} else {
				return noPlayer();
			}
		} else {
			return noPermission();
		}
	}

	private String endersee() {
		if (sender.hasPermission("iMine.endersee")) {
			if (sender instanceof Player) {
				if (args.length == 0) {
					((Player) sender).openInventory(((Player) sender).getEnderChest());
					return ColorUtil.replaceColors("&7Opend enderchest of &c%s&7.", sender.getName());
				} else if (args.length == 1) {
					Player target = PlayerUtil.getOnline(args[0]);
					if (target != null) {
						Player pl = (Player) sender;
						pl.openInventory(target.getEnderChest());
						return ColorUtil.replaceColors("&7Opend enderchest of &c%s&7.", pl.getName());
					} else {
						return noOnline(args[0]);
					}
				} else {
					return noOption();
				}
			} else {
				return noPlayer();
			}
		} else {
			return noPermission();
		}
	}

	private String git() {
		byte b = 0b00;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("projects") && sender.hasPermission("iMine.git")) {
				Set<String> projects = CommandHandler.API.getProjects();
				sender.sendMessage(ColorUtil.replaceColors("&7Listing all git projects"));
				for (String project : projects) {
					sender.sendMessage(ColorUtil.replaceColors("  &6- &e%s", project));
				}
				return "";
			}
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-v")) {
					b = (byte) (b | (1 << 0));
				}
				if (args[i].equalsIgnoreCase("-q")) {
					b = (byte) (b | (1 << 1));
				}
			}
		}
		Bukkit.getScheduler().runTaskLaterAsynchronously(BukkitStarter.plugin, new GitCheckRunnalbe(sender, b), 1L);
		return ColorUtil.replaceColors("&r&l[&6&lGIT&r&l]&7 Checking all git repos...");
	}

	private String plr() {
		if ((sender.hasPermission("iMine.dev")) && args.length > 0) {
			Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, new ReloadPlugin());
			return ColorUtil.replaceColors("&7Reloading plugin &e%s&7.", args[0]);
		} else {
			return noPermission();
		}
	}

	private String returnTP() {
		if (sender.hasPermission("iMine.return")) {
			if (sender instanceof Player) {
				Player sender = (Player) this.sender;
				if (BukkitListener.TP_HISTORY.containsKey(sender.getUniqueId())) {
					List<Location> locs = BukkitListener.TP_HISTORY.get(sender.getUniqueId());
					sender.teleport(locs.get(locs.size() - 1));
					return ColorUtil.replaceColors("&7Back to previous location.");
				} else {
					return ColorUtil.replaceColors("&cNo history.");
				}
			} else {
				return ColorUtil.replaceColors("&cPlayer only.");
			}
		} else {
			return noPermission();
		}
	}

	private String vanish() {
		if (sender.hasPermission("iMine.vanish")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player sender = (Player) this.sender;
					if (BukkitListener.VANISH.contains(sender.getUniqueId())) {
						BukkitListener.VANISH.remove(sender.getUniqueId());
						BukkitListener.updateVanish();
						return ColorUtil.replaceColors("&7You are visible again!");
					} else {
						BukkitListener.VANISH.add(sender.getUniqueId());
						BukkitListener.updateVanish();
						return ColorUtil.replaceColors("&7GhostMode!");
					}
				} else {
					return noPlayer();
				}
			} else {
				Player target = PlayerUtil.getOnline(args[0]);
				if (target != null) {
					if (BukkitListener.VANISH.contains(target.getUniqueId())) {
						BukkitListener.VANISH.remove(target.getUniqueId());
						if (target != sender) {
							target.sendMessage(ColorUtil.replaceColors("&7You are visible again!"));
						}
					} else {
						BukkitListener.VANISH.add(target.getUniqueId());
						if (target != sender) {
							target.sendMessage(ColorUtil.replaceColors("&7GhostMode!"));
						}
					}
					BukkitListener.updateVanish();
					return (ColorUtil.replaceColors("&7Toggled vanish for &c%s&7.", target.getName()));
				} else {
					return noOnline(args[0]);
				}
			}
		} else {
			return noPermission();
		}
	}

	private String kill() {
		if (sender.hasPermission("iMine.kill")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player sender = (Player) this.sender;
					sender.setHealth(0D);
					return ColorUtil.replaceColors("&7Committed suicide,");
				} else {
					return noPlayer();
				}
			} else {
				Player target = PlayerUtil.getOnline(args[0]);
				if (target != null) {
					target.setHealth(0D);
					return ColorUtil.replaceColors("&7Assassinated &c%s&7!", target.getName());
				} else {
					return noOnline(args[0]);
				}
			}
		} else {
			return noPermission();
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
					return ColorUtil.replaceColors("&cNobody to reply to");
				}
			} else {
				return noOption();
			}
		} else {
			return noPermission();
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
			return noPermission();
		}
	}

	private String world() {
		if (sender.hasPermission("iMine.world")) {
			if (sender instanceof Player) {
				Player player = (Player) sender;
				if (args.length == 0) {
					player.sendMessage(ColorUtil.replaceColors("&7Current World: &e%s", player.getWorld().getName()));
					player.sendMessage(ColorUtil.replaceColors("&7Availible worlds:"));
					Bukkit.getWorlds().stream()
							.forEach(w -> player.sendMessage(ColorUtil.replaceColors("  &e%s", w.getName())));
					return "";
				} else if (args.length == 1) {
					World world;
					try {
						world = Bukkit.getWorlds().get(Integer.parseInt(args[0]));
					} catch (NumberFormatException e) {
						world = Bukkit.getWorld(args[0]);
						if (world == null) {
							if (new File(args[0], Bukkit.getWorldContainer().getPath()).exists()) {
								world = Bukkit.createWorld(new WorldCreator(args[0]));
							}
						}
					}
					if (world != null) {
						player.teleport(world.getSpawnLocation(), PlayerTeleportEvent.TeleportCause.COMMAND);
						return ColorUtil.replaceColors("&7Teleported to world &e%s&7.", world.getName());
					} else {
						return ColorUtil.replaceColors("&cNo world with the name '&e%s&6' exists.", args[0]);
					}
				} else {
					return noOption();
				}
			} else {
				return noPlayer();
			}
		} else {
			return noPermission();
		}
	}

	private String noPermission() {
		return ColorUtil.replaceColors("&cYou do not have permission to execute '&e%s&c' command!", command);
	}

	private String noOption() {
		return ColorUtil.replaceColors("&cThere is no command with this argument.");
	}

	private String noPlayer() {
		return ColorUtil.replaceColors("&cPlayer-command only.");
	}

	private String noOnline(String arg) {
		return ColorUtil.replaceColors("&cPlayer '&e%s&c' is not online.", arg);
	}

	public static List<String> onTabComplete(Player sender, String command, String[] args) {
		List<String> ret = new ArrayList<>();
		if (command.equalsIgnoreCase("hub")) {
			String[] servers = {"creative", "uhc", "hub", "survival", "outlaws", "testserver", "outlawsB"};
			if (args.length == 1) {
				for (String server : servers) {
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
		} else if (command.equalsIgnoreCase("mchistory") && args.length == 1) {
			ret.addAll(PlayerUtil.getNamesLike(args[args.length - 1]));
		} else if (command.equalsIgnoreCase("git")) {
			String[] argumenten = {"-v", "-q", "projects"};
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
				|| (command.equalsIgnoreCase("whois") && args.length == 1)
				|| (command.equalsIgnoreCase("invsee") && args.length == 1)
				|| (command.equalsIgnoreCase("vanish") && args.length == 1) || (command.equalsIgnoreCase("report"))
				|| (command.equalsIgnoreCase("tp") && (args.length == 1 || args.length == 2))
				|| (command.equalsIgnoreCase("kill") && args.length == 1) || (command.equalsIgnoreCase("reply"))
				|| (command.equalsIgnoreCase("endersee") && args.length == 1) || (command.equalsIgnoreCase("msg"))
				|| (command.equalsIgnoreCase("speed") && args.length > 1) || (command.equalsIgnoreCase("me"))) {
			ret.addAll(PlayerUtil.getAllOnlineNames(args[args.length - 1], sender));
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
			Plugin pl = Bukkit.getPluginManager().getPlugin(args[0]);
			if (pl != null) {
				sender.sendMessage(String.format(format, "Overriding to new plugins"));
				Bukkit.getPluginManager().disablePlugin(pl);
				sender.sendMessage(String.format(format,
					pl.getName() + " is now disabled [" + pl.getDescription().getVersion() + "]"));
				try {
					Thread.sleep(1500L);
					Method m = JavaPlugin.class.getDeclaredMethod("getFile");
					m.setAccessible(true);
					File f = (File) m.invoke(pl);
					BukkitStarter.plugin.updatePlugins();
					Thread.sleep(1500L);
					pl = Bukkit.getPluginManager().loadPlugin(new File(f.getAbsolutePath()));
					sender.sendMessage(String.format(format,
						pl.getName() + " is now reloaded! [" + pl.getDescription().getVersion() + "]"));
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

	private class GitCheckRunnalbe implements Runnable {

		// 01 = debug, 10 = stil
		private final byte quietVerbose;
		private final List<Plugin> toUpdate;

		public GitCheckRunnalbe(CommandSender sender, byte verbose) {
			this.quietVerbose = verbose;
			this.toUpdate = new ArrayList<>();
		}

		private void verboseMessage(String msg) {
			if ((quietVerbose & 0b01) == 1) {
				sender.sendMessage(ColorUtil.replaceColors("  &7%s", msg));
			}
		}

		private boolean quietSend() {
			return (quietVerbose & 0b10) == 0;
		}

		private void message(String msg) {
			sender.sendMessage(msg);
		}

		private void sendTextComponent(TextComponent text) {
			if (sender instanceof Player) {
				((Player) sender).spigot().sendMessage(text);
			} else {
				sender.sendMessage(text.toPlainText());
			}
		}

		private void checkPlugin(Plugin plugin) {
			verboseMessage("Checking plugin " + plugin.getName());
			String version = plugin.getDescription().getVersion();
			version = version.substring(0, Math.min(7, version.length()));
			Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
			Matcher match = p.matcher(version);
			if (match.find()) {
				verboseMessage("plugin " + plugin.getName() + " is a GIT project!");
				GitProject git = CommandHandler.API.getProjectData(plugin.getName());
				if (git == null) {
					verboseMessage("but i couldnt find it our system");
					return;
				}
				/**
				 * Lief dagboek,
				 *
				 * Een mooie message maakt lelijke code. Mocht je hier onder nog
				 * iets nuttigs mee willen doen, im sorry
				 *
				 * groetjes Tim
				 */

				/**
				 * Lief dagboek,
				 *
				 * Ik zag dat Tim iets had gemaakt waar hij zelf een mooiere
				 * manier voor heeft geschreven. Zonde als dat niet gebruitk zou
				 * worden hè? ;)
				 *
				 * groetjes Sander
				 */
				/**
				 * Lief dagboek,
				 *
				 * het scheen dat ik soms dingen gebruikte en soms ook niet na
				 * veel moeite gebruik ik het nu overal
				 *
				 * hoop dat toekomst zelf het nu makkelijker kan terug lezen
				 *
				 * Groetjes Tim
				 */
				/**
				 * Lief dagboek, Tim is dik, hihi
				 * 
				 * Groetjes Sander
				 */
				// newestversion: %gitshort% [RELOAD SERVER]
				TextComponent extra, message = new TextComponent("");
				// [GIT]
				extra = new TextComponent(ColorUtil.replaceColors("&r&l[&6&lGIT&r&l]&7 "));
				message.addExtra(extra);
				// %plugin naam%
				extra = new TextComponent(ColorUtil.replaceColors("&e%s&7 ", plugin.getName()));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl()));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(git.getDescription())
								.append("\n\nCreated at: " + GitLabAPI.NL_DATE_FORMAT.format(git.getCreateDate()))
								.create()));
				message.addExtra(extra);
				// current verion:
				extra = new TextComponent(ColorUtil.replaceColors("&7current version: "));
				message.addExtra(extra);
				// %git short this version%
				Commit current = null;
				List<Commit> commits = new ArrayList<>();
				for (Commit commit : git.getCommits()) {
					if (commit.getShortId().toLowerCase().contains(match.group(0).toLowerCase())) {
						current = commit;
						verboseMessage("current commit found: " + commit.getMessage());
						break;
					}
					commits.add(commit);
				}
				if (current == null) {
					extra = new TextComponent(ColorUtil.replaceColors("&cnot found"));
					extra.setColor(net.md_5.bungee.api.ChatColor.RED);
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(ColorUtil.replaceColors("&c%s -comit not found"))
									.append("\n\nPushed at: ~").create()));
				} else {
					extra = new TextComponent(
							ColorUtil.replaceColors("&e%s ", current.getTitle().replaceAll(" ", " &e")));
					extra.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
					extra.setHoverEvent(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(ColorUtil.replaceColors("&7id:&e%s&7", current.getShortId()))
										.append(
											"\n\n&7Pushed at: &c" + GitLabAPI.NL_DATE_FORMAT.format(current.getWhen()))
										.create()));
				}
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/commit/"
						+ (current == null ? "master" : current.getLongId()) + "?view=parallel"));

				message.addExtra(extra);
				if (commits.isEmpty()) {
					verboseMessage("no current commit found!");
				} else {
					toUpdate.add(plugin);
					// newest verion:
					extra = new TextComponent(ColorUtil.replaceColors("&7newest version: "));
					message.addExtra(extra);
					// %git short new version%
					extra = new TextComponent(
							ColorUtil.replaceColors("&e%s&7 ", git.getCommits()[0].getTitle().replaceAll(" ", " &e")));
					extra.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/compare/"
							+ (current == null ? "master" : current.getShortId()) + "...master?view=parallel"));
					ComponentBuilder hoverBuilder = new ComponentBuilder(
							"&7version id: &e" + git.getCommits()[0].getShortId());
					hoverBuilder.append("\nMissing Versions:");
					for (Commit commit : commits) {
						hoverBuilder.append(ColorUtil.replaceColors("\n&7 ^&e%s &7[&c%s&7]", commit.getTitle(),
							GitLabAPI.NL_DATE_FORMAT.format(commit.getWhen())));
					}
					hoverBuilder.append(
						"\n\n&7Pushed at: &c" + GitLabAPI.NL_DATE_FORMAT.format(git.getCommits()[0].getWhen()));
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
					message.addExtra(extra);
					// versions behind [#]:
					extra = new TextComponent(ColorUtil.replaceColors("&r[%d]&r ", commits.size()));
					message.addExtra(extra);
				}
				if (quietSend()) {
					sendTextComponent(message);
				}
			}
		}

		private void prosessData() {
			for (final Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				checkPlugin(plugin);
			}
			int gitUpdates = toUpdate.size();
			int updates = BukkitStarter.UPDATE_DIR.listFiles().length;
			if (gitUpdates > 0 || updates > 0) {
				verboseMessage("Update found");
				if (gitUpdates > 0) {
					verboseMessage("  - Git");
				} else if (updates > 0) {
					verboseMessage("  - Random update");
				} else {
					verboseMessage("  - No update");
				}
				TextComponent extra, message = new TextComponent("");
				// Files to update: #
				extra = new TextComponent(ColorUtil.replaceColors("&eFiles to update: %s", updates));
				extra.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
				ComponentBuilder cb = new ComponentBuilder("");
				Iterator<File> newFileI = MktUtil.toList(BukkitStarter.UPDATE_DIR.listFiles()).iterator();
				while (newFileI.hasNext()) {
					File newFile = newFileI.next();
					String append = ColorUtil.replaceColors("&6%s", newFile.getName());
					if (newFileI.hasNext()) {
						append += "\n";
					}
					cb.append(append);
				}
				extra.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
				message.addExtra(extra);
				// GitRepos to update: #
				extra = new TextComponent(ColorUtil.replaceColors("  &bGitRepos to update: %s", gitUpdates));
				extra.setColor(net.md_5.bungee.api.ChatColor.AQUA);
				cb = new ComponentBuilder("");
				Iterator<Plugin> toUpdateI = toUpdate.iterator();
				while (toUpdateI.hasNext()) {
					Plugin plugin = toUpdateI.next();
					String append = ColorUtil.replaceColors("&3%s", plugin.getName());
					if (toUpdateI.hasNext()) {
						append += "\n";
					}
					cb.append(append);
				}
				extra.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
				message.addExtra(extra);
				sendTextComponent(message);

				message = new TextComponent("");
				// [RELOAD SERVER]
				extra = new TextComponent(
						ColorUtil.replaceColors(" &r&l[&e%sReload server&r&l]&r ", (updates == 0 ? "&m" : "")));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder("Click to reload server").create()));
				message.addExtra(extra);
				// [REBOOT SERVER]
				extra = new TextComponent(
						ColorUtil.replaceColors(" &r&l[&c%sReload server&r&l]&r ", (updates == 0 ? "&m" : "")));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/restart"));
				extra.setHoverEvent(
					new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(ColorUtil
									.replaceColors("&cWARNING, will shutdown server!\n&rClick to reboot server"))
											.create()));
				message.addExtra(extra);
				sendTextComponent(message);
			} else {
				message(ColorUtil.replaceColors("&7 No updates found!"));
			}
		}

		@Override
		public void run() {
			if (sender.hasPermission("iMine.git")) {
				CommandHandler.API.refreshData();
				if (!CommandHandler.API.canWork()) {
					message("This server is outdated -> cant check on GitRepo's");
					return;
				}
				Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
					prosessData();
				});
			} else {
				sender.sendMessage(ColorUtil.replaceColors("&cNo permission."));
			}
		}
	}
}
