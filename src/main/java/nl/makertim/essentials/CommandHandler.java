package nl.makertim.essentials;

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.sql.ResultSet;
import java.util.ArrayList;
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

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.HoverEvent.Action;
import net.md_5.bungee.api.chat.TextComponent;
import nl.imine.api.db.DatabaseManager;
import nl.imine.api.util.ColorUtil;
import nl.imine.api.util.DateUtil;
import nl.imine.api.util.MktUtil;
import nl.imine.api.util.PlayerUtil;
import nl.makertim.essentials.GitLabAPI.Commit;
import nl.makertim.essentials.GitLabAPI.GitProject;
import nl.makertim.essentials.MapCountSorter.Sort;

public class CommandHandler {

	private static final Map<CommandSender, CommandSender> LAST_SPOKE = new HashMap<>();

	private final CommandSender sender;
	private final String command;
	private final String[] args;

	public CommandHandler(final CommandSender sender, final String command, final String[] args) {
		this.sender = sender;
		this.command = command;
		this.args = args;
	}

	public boolean onCommand() {
		if (command.equalsIgnoreCase("hub")) {
			return hub();
		} else if (command.equalsIgnoreCase("dev")) {
			return dev();
		} else if (command.equalsIgnoreCase("mute")) {
			return mute();
		} else if (command.equalsIgnoreCase("tp")) {
			return tp();
		} else if (command.equalsIgnoreCase("fly")) {
			return fly();
		} else if (command.equalsIgnoreCase("tab")) {
			return tab();
		} else if (command.equalsIgnoreCase("lagdebug")) {
			return lagdebug();
		} else if (command.equalsIgnoreCase("gm")) {
			return gm();
		} else if (command.startsWith("gm") && command.length() == 3) {
			return gmx();
		} else if (command.equalsIgnoreCase("speed")) {
			return speed();
		} else if (command.equalsIgnoreCase("banrichtlijn")) {
			return banrichtlijn();
		} else if (command.equalsIgnoreCase("msg")) {
			return msg();
		} else if (command.equalsIgnoreCase("invsee")) {
			return invsee();
		} else if (command.equalsIgnoreCase("endersee")) {
			return endersee();
		} else if (command.equalsIgnoreCase("mchistory")) {
			return mchistory();
		} else if (command.equalsIgnoreCase("git")) {
			return git();
		} else if (command.equalsIgnoreCase("plr")) {
			return plr();
		} else if (command.equalsIgnoreCase("return")) {
			return _return();
		} else if (command.equalsIgnoreCase("vanish")) {
			return vanish();
		} else if (command.equalsIgnoreCase("kill")) {
			return kill();
		} else if (command.equalsIgnoreCase("reply")) {
			return reply();
		} else if (command.equalsIgnoreCase("me")) {
			return me();
		} else if (command.equalsIgnoreCase("pl") || command.equalsIgnoreCase("plugin")
				|| command.equalsIgnoreCase("plugins")) {
			return plugin();
		} else if (command.equalsIgnoreCase("report")) {
			Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, new ServerReporter());
			return true;
		} else if (command.equalsIgnoreCase("admin")) {
			Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
				new AdminChat();
			});
			return true;
		} else if (command.equalsIgnoreCase("update")) {
			if (sender instanceof Player) {
				((Player) sender).performCommand("reload");
			} else {
				Bukkit.reload();
			}
			return true;
		}
		return false;
	}

	private boolean banrichtlijn() {
		sender.sendMessage(ColorUtil.replaceColors("&4&lBanRichtlijn"));
		sender.sendMessage("   ");
		sender.sendMessage(ColorUtil.replaceColors("&3Griefing &6- &2Permanent ban"));
		sender.sendMessage(ColorUtil.replaceColors("&3Hacks &6- &2Permanent ban"));
		sender.sendMessage(ColorUtil.replaceColors("&3Bedrijgen &6- &22weken ban "));
		sender.sendMessage(ColorUtil.replaceColors("&3Extreem schelden &6- &248 uur ban "));
		sender.sendMessage(
				ColorUtil.replaceColors("&3Ongepast taalgebruik &6- &2Waarschuwing (kick), daarna 2 tot 4 uur ban"));
		sender.sendMessage(ColorUtil.replaceColors("&3Spam &6- &2Waarschuwing (kick), daarna 2 tot 4 uur ban"));
		sender.sendMessage("   ");
		sender.sendMessage(ColorUtil.replaceColors("&eBedenk je ban verstandig en zet er een DUIDELIJKE reden bij."));
		sender.sendMessage(ColorUtil.replaceColors("&7Mocht je dit niet kunnen, geef dit door aan je leidinggevende!"));
		return true;
	}

	private boolean mchistory() {
		if (sender.hasPermission("iMine.mchistory")) {
			if (args.length > 0) {
				List<UUID> uuidsLike = PlayerUtil.getUuidsLike(args[0]);
				for (final UUID foundUUID : uuidsLike) {
					Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin,
							new NameLookup(foundUUID, sender));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need player to lookup");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean tab() {
		if (sender.hasPermission("iMine.tabchange")) {
			TabListHandler tlh = BukkitStarter.plugin.getTLH();
			if (args.length > 1) {
				String msg = "";
				for (int i = 1; i < args.length; i++) {
					msg += args[i] + " ";
				}
				msg = msg.substring(0, msg.length() - 1);
				msg = ColorUtil.replaceColors(msg);
				if (args[0].equalsIgnoreCase("top")) {
					tlh.updateTop(msg);
					sender.sendMessage(ChatColor.GOLD + "Tab top updated to " + ChatColor.RESET + msg);
				} else if (args[0].equalsIgnoreCase("bottom")) {
					tlh.updateBottom(msg);
					sender.sendMessage(ChatColor.GOLD + "Tab bottom updated to " + ChatColor.RESET + msg);
				}
			} else if (args.length == 1 && args[0].equalsIgnoreCase("update")) {
				tlh.updateAll();
			} else {
				sender.sendMessage(ChatColor.RED + "Need more args!");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean fly() {
		if (sender.hasPermission("iMine.fly")) {
			Player pl = null;
			if (args.length == 0 && sender instanceof Player) {
				pl = (Player) sender;
			} else {
				pl = PlayerUtil.getOnline(args[0]);
			}
			if (pl != null) {
				pl.setAllowFlight(!pl.getAllowFlight());
				pl.setFlying(pl.getAllowFlight());
				sender.sendMessage(ColorUtil.replaceColors("&7Player &c%s&7 %s&7 fly now.", pl.getName(),
						(pl.getAllowFlight() ? "&6can" : "&4can't")));
				if (sender != pl) {
					pl.sendMessage(ColorUtil.replaceColors("&7You %s&7 fly now.",
							(pl.getAllowFlight() ? "&6can" : "&4can't")));
				}
			} else {
				sender.sendMessage(ChatColor.RED + "No player with fly powers");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean plugin() {
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
		} else {
			sender.sendMessage(message.toPlainText());
		}
		return true;
	}

	private boolean mute() {
		if (args.length != 0 && sender.hasPermission("iMine.mute")) {
			OfflinePlayer pl = PlayerUtil.getOflline(args[0]);
			if (pl != null) {
				BukkitListener.toggleMuted(pl);
				sender.sendMessage(ChatColor.GOLD + pl.getName() + " is now "
						+ (BukkitListener.isMuted(pl) ? "muted." : "unmuted."));
			} else {
				sender.sendMessage(ChatColor.RED + "No player by name " + args[0]);
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean hub() {
		if (args.length == 0) {
			if (sender instanceof Player) {
				PlayerUtil.sendPlayerToServer((Player) sender, "hub");
			} else {
				sender.sendMessage("Player only!");
			}
		} else if (sender.hasPermission("iMine.hub")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					PlayerUtil.sendPlayerToServer((Player) sender, args[0]);
				} else {
					sender.sendMessage("Player only!");
				}
			} else if (args.length == 2) {
				Player pl = PlayerUtil.getOnline(args[1]);
				if (pl != null) {
					PlayerUtil.sendPlayerToServer(pl, args[0]);
				} else {
					sender.sendMessage("That player... is not online");
				}
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean dev() {
		if (sender.hasPermission("iMine.dev")) {
			BukkitStarter.plugin.devMode = !BukkitStarter.plugin.devMode;
			if (BukkitStarter.plugin.devMode) {
				sender.sendMessage("Devolpermodus is now enabled!");
				for (Player pl : new ArrayList<>(Bukkit.getOnlinePlayers())) {
					if (!pl.hasPermission("iMine.dev")) {
						PlayerUtil.sendPlayerToServer(pl, "hub");
					}
				}
			} else {
				sender.sendMessage("Server now public");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean lagdebug() {
		if (sender.hasPermission("iMine.lagdebug")) {
			if (args.length == 0) {
				for (World w : Bukkit.getWorlds()) {
					sender.sendMessage(ChatColor.GOLD + "Mobs in world " + w.getName() + " " + ChatColor.BOLD
							+ w.getEntities().size());
					Map<Class<? extends Entity>, List<Entity>> countMap = new HashMap<>();
					for (Entity e : w.getEntities()) {
						if (!countMap.containsKey(e.getClass())) {
							countMap.put(e.getClass(), new ArrayList<Entity>());
						}
						countMap.get(e.getClass()).add(e);
					}
					for (Class<? extends Entity> entityClass : MapCountSorter.getOrder(countMap, Sort.DESC)) {
						sender.sendMessage(ChatColor.GREEN + "  " + entityClass.getSimpleName().replace("Craft", "")
								+ "s: " + ChatColor.BOLD + countMap.get(entityClass).size());
					}
				}
			} else {
				if (args[0].toLowerCase().startsWith("player")) {
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
					sender.sendMessage(ChatColor.GOLD + "All entitys by player");
					for (String plName : MapCountSorter.getOrder(countMap, Sort.DESC)) {
						sender.sendMessage(
								ChatColor.AQUA + "  " + plName + ": " + ChatColor.BOLD + countMap.get(plName).size());
					}
				} else {
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
					sender.sendMessage(ChatColor.GOLD + "All entitys by player(s) " + ChatColor.BOLD + args[0]);
					for (Class<? extends Entity> entityClass : MapCountSorter.getOrder(countMap, Sort.DESC)) {
						sender.sendMessage(ChatColor.GREEN + "  " + entityClass.getSimpleName().replace("Craft", "")
								+ "s: " + ChatColor.BOLD + countMap.get(entityClass).size());
					}
				}
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean tp() {
		if (sender.hasPermission("iMine.tp")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					Player pl = PlayerUtil.getOnline(args[0]);
					if (pl != null) {
						((Player) sender).teleport(pl);
						sender.sendMessage(ChatColor.GOLD + "Teleporting to " + ChatColor.RED + pl.getName()
								+ ChatColor.GOLD + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Only for players!");
				}
			} else if (args.length == 2) {
				Player who = PlayerUtil.getOnline(args[0]);
				Player target = PlayerUtil.getOnline(args[1]);
				if (who != null) {
					if (target != null) {
						who.teleport(target);
						who.sendMessage(ChatColor.GOLD + "Teleporting...");
						sender.sendMessage(ChatColor.GOLD + "Teleported " + ChatColor.RED + who.getName()
								+ ChatColor.GOLD + " to " + ChatColor.RED + target.getName() + ChatColor.GOLD + ".");
					} else {
						sender.sendMessage(ChatColor.RED + "No player with name " + args[1]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
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
				try {
					if (faultArg == 0 && args.length == 4) {
						Player target = PlayerUtil.getOnline(args[0]);
						if (target != null) {
							target.teleport(new Location(target.getWorld(), coords[1], coords[2], coords[3],
									target.getLocation().getYaw(), target.getLocation().getPitch()));
							sender.sendMessage(ChatColor.GOLD + "Teleporting...");
						} else {
							sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
						}
					} else if (faultArg < 0) {
						if (sender instanceof Player) {
							Player who = (Player) sender;
							if (args.length == 3) {
								who.teleport(new Location(who.getWorld(), coords[0], coords[1], coords[2],
										who.getLocation().getYaw(), who.getLocation().getPitch()));
								sender.sendMessage(ChatColor.GOLD + "Teleporting...");
							} else {
								if (Bukkit.getWorlds().get((int) coords[3]) != null) {
									who.teleport(new Location(Bukkit.getWorlds().get((int) coords[3]), coords[0],
											coords[1], coords[2], who.getLocation().getYaw(),
											who.getLocation().getPitch()));
									sender.sendMessage(ChatColor.GOLD + "Teleporting...");
								} else {
									sender.sendMessage(ChatColor.RED + "No world on id " + coords[3]);
								}
							}
						} else {
							sender.sendMessage(ChatColor.RED + "You must be a player!");
						}
					} else {
						sender.sendMessage(ChatColor.RED + args[faultArg] + " is not a number.");
					}
				} catch (Exception ex) {
					sender.sendMessage(ChatColor.RED + "error: " + ex.getMessage() + "   - PLZ REPORT");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "No target to teleport to.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean gm() {
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
							sender.sendMessage(
									ChatColor.GOLD + "Set gamemode to " + ChatColor.RED + set.name().toLowerCase());
						} else {
							sender.sendMessage(ChatColor.RED + "No gamemode found by that name.");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "Only for players!");
					}
				} else if (args.length == 2) {
					Player who = PlayerUtil.getOnline(args[1]);
					if (who != null) {
						if (set != null) {
							who.setGameMode(set);
							who.sendMessage(
									ChatColor.GOLD + "Set gamemode to " + ChatColor.RED + set.name().toLowerCase());
							sender.sendMessage(ChatColor.GOLD + "Set gamemode to " + ChatColor.RED
									+ set.name().toLowerCase() + ChatColor.GOLD + " for " + ChatColor.RED
									+ who.getName() + ChatColor.GOLD + ".");
						} else {
							sender.sendMessage(ChatColor.RED + "No gamemode found by that name.");
						}
					} else {
						sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
					}
				} else {
					sender.sendMessage(ChatColor.RED + "No idea what to do");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "No gamemode to be set.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean gmx() {
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
				sender.sendMessage(ChatColor.RED + "That is not a gamemode!");
			}
			if (args.length == 0) {
				if (sender instanceof Player) {
					((Player) sender).setGameMode(set);
					sender.sendMessage(ChatColor.GOLD + "Set gamemode to " + ChatColor.RED + set.name().toLowerCase());
				} else {
					sender.sendMessage(ChatColor.RED + "Only for players!");
				}
			} else if (args.length == 1) {
				Player who = PlayerUtil.getOnline(args[0]);
				if (who != null) {
					who.setGameMode(set);
					who.sendMessage(ChatColor.GOLD + "Set gamemode to " + ChatColor.RED + set.name().toLowerCase());
					sender.sendMessage(ChatColor.GOLD + "Set gamemode to " + ChatColor.RED + set.name().toLowerCase()
							+ ChatColor.GOLD + " for " + ChatColor.RED + who.getName() + ChatColor.GOLD + ".");
				} else {
					sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "No idea what to do");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean speed() {
		if (sender.hasPermission("iMine.speed")) {
			if (sender instanceof Player) {
				Player pl = ((Player) sender);
				if (args.length > 0) {
					float speed = 0;
					try {
						speed = Math.min(Math.abs(Float.parseFloat(args[0]) * 0.2F), 1F);
					} catch (Exception ex) {
						sender.sendMessage(ChatColor.RED + args[0] + " is no number.");
						return false;
					}
					if (args.length == 1) {
						if (pl.isFlying()) {
							pl.setFlySpeed(speed);
							sender.sendMessage(ChatColor.GOLD + "Fly speed set to " + args[0]);
						} else {
							pl.setWalkSpeed(speed);
							sender.sendMessage(ChatColor.GOLD + "Walk speed set to " + args[0]);
						}
					} else if (args.length == 2) {
						if (args[1].toLowerCase().contains("f")) {
							pl.setFlySpeed(speed);
							sender.sendMessage(ChatColor.GOLD + "Fly speed set to " + args[0]);
						} else {
							pl.setWalkSpeed(speed);
							sender.sendMessage(ChatColor.GOLD + "Walk speed set to " + args[0]);
						}
					} else if (args.length == 3) {
						Player who = PlayerUtil.getOnline(args[2]);
						if (who != null) {
							if (args[1].toLowerCase().contains("f")) {
								who.setFlySpeed(speed);
								who.sendMessage(ChatColor.GOLD + "Fly speed set to " + args[0]);
								sender.sendMessage(ChatColor.GOLD + "Speed set.");
							} else {
								who.setWalkSpeed(speed);
								who.sendMessage(ChatColor.GOLD + "Walk speed set to " + args[0]);
								sender.sendMessage(ChatColor.GOLD + "Speed set.");
							}
						} else {
							sender.sendMessage(ChatColor.RED + "No player with name " + args[2]);
						}
					} else {
						sender.sendMessage(ChatColor.RED + "I dont know what to do!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "I dont know what to do!");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Only for players.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean msg() {
		if (sender.hasPermission("iMine.msg")) {
			if (args.length > 1) {
				Player target = PlayerUtil.getOnline(args[0]);
				if (target != null) {
					String msg = "";
					for (int i = 1; i < args.length; i++) {
						msg += args[i] + " ";
					}
					msg = ColorUtil.replaceColors(msg);
					target.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Received message from "
							+ ChatColor.RED + sender.getName() + ChatColor.DARK_GRAY + ChatColor.BOLD + " \u00BB "
							+ ChatColor.RESET + ChatColor.GRAY + msg);
					sender.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Send message to "
							+ ChatColor.RED + target.getName() + ChatColor.DARK_GRAY + ChatColor.BOLD + " \u00BB "
							+ ChatColor.RESET + ChatColor.GRAY + msg);
					if (LAST_SPOKE.containsKey(target)) {
						LAST_SPOKE.remove(target);
					}
					LAST_SPOKE.put(target, sender);
					if (LAST_SPOKE.containsKey(sender)) {
						LAST_SPOKE.remove(sender);
					}
					LAST_SPOKE.put(sender, target);
				} else {
					sender.sendMessage(ChatColor.RED + "No player with name " + args[0]);
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a player and a message.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean invsee() {
		if (sender.hasPermission("iMine.invsee")) {
			if (args.length == 1) {
				Player target = PlayerUtil.getOnline(args[0]);
				if (sender instanceof Player && target != null) {
					Player pl = (Player) sender;
					pl.openInventory(target.getInventory());
				} else {
					sender.sendMessage(ChatColor.RED + "No player to open inventory.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a player.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean endersee() {
		if (sender.hasPermission("iMine.endersee")) {
			if (args.length == 1) {
				Player target = PlayerUtil.getOnline(args[0]);
				if (sender instanceof Player && target != null) {
					Player pl = (Player) sender;
					pl.openInventory(target.getEnderChest());
				} else {
					sender.sendMessage(ChatColor.RED + "Need a player and a message.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a player.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean git() {
		byte b = 0b00;
		if (args.length > 0) {
			if (args[0].equalsIgnoreCase("projects")) {
				Set<String> projects = BukkitStarter.API.getProjects();
				for (String project : projects) {
					sender.sendMessage(project);
				}
				return true;
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
		sender.sendMessage(String.format("%s%s[%s%sGIT%s%s]%s Checking all git repos...", ChatColor.RESET,
				ChatColor.BOLD, ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
		Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, new GitCheckRunnalbe(sender, b));
		return true;
	}

	private boolean plr() {
		if ((sender.hasPermission("iMine.dev")) && args.length > 0) {
			Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
				new ReloadPlugin(sender, args);
			});
		} else {
			noPermission();
		}
		return true;
	}

	private boolean _return() {
		if (sender.hasPermission("iMine.return")) {
			if (sender instanceof Player) {
				Player sender = (Player) this.sender;
				if (BukkitListener.TP_HISTORY.containsKey(sender.getUniqueId())) {
					List<Location> locs = BukkitListener.TP_HISTORY.get(sender.getUniqueId());
					sender.teleport(locs.get(locs.size() - 1));
					sender.sendMessage(ChatColor.GOLD + "Back to previous location.");
				} else {
					sender.sendMessage(ChatColor.RED + "No history.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Player only.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean vanish() {
		if (sender.hasPermission("iMine.vanish")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player sender = (Player) this.sender;
					if (BukkitListener.VANISH.contains(sender.getUniqueId())) {
						BukkitListener.VANISH.remove(sender.getUniqueId());
						sender.sendMessage(ChatColor.GOLD + "You are visible again!");
					} else {
						BukkitListener.VANISH.add(sender.getUniqueId());
						sender.sendMessage(ChatColor.GOLD + "GhostMode!");
					}
				} else {
					sender.sendMessage(ChatColor.RED + "Player only");
				}
			} else {
				Player target = PlayerUtil.getOnline(args[0]);
				if (target != null) {
					if (BukkitListener.VANISH.contains(target.getUniqueId())) {
						BukkitListener.VANISH.remove(target.getUniqueId());
						target.sendMessage(ChatColor.GOLD + "You are visible again!");
					} else {
						BukkitListener.VANISH.add(target.getUniqueId());
						target.sendMessage(ChatColor.GOLD + "GhostMode!");
					}
					sender.sendMessage(ChatColor.GOLD + "Toggled vanish for " + ChatColor.RED + target.getName());
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found");
				}
			}
			BukkitListener.updateVanish();
		} else {
			noPermission();
		}
		return true;
	}

	private boolean kill() {
		if (sender.hasPermission("iMine.kill")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player sender = (Player) this.sender;
					sender.setHealth(0D);
					sender.sendMessage(ChatColor.GOLD + "Suisided");
				} else {
					sender.sendMessage(ChatColor.RED + "Player only");
				}
			} else {
				Player target = PlayerUtil.getOnline(args[0]);
				if (target != null) {
					target.setHealth(0D);
					sender.sendMessage(ChatColor.GOLD + "Assassinated!");
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found");
				}
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean reply() {
		if (sender.hasPermission("iMine.reply")) {
			if (args.length > 0) {
				if (LAST_SPOKE.containsKey(sender)) {
					CommandSender target = LAST_SPOKE.get(sender);
					String msg = "";
					for (int i = 0; i < args.length; i++) {
						msg += args[i] + " ";
					}
					msg = ColorUtil.replaceColors(msg);
					target.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Received message from "
							+ ChatColor.RED + sender.getName() + ChatColor.DARK_GRAY + ChatColor.BOLD + " \u00BB "
							+ ChatColor.RESET + ChatColor.GRAY + msg);
					sender.sendMessage(ChatColor.DARK_GRAY.toString() + ChatColor.ITALIC + "Send message to "
							+ ChatColor.RED + target.getName() + ChatColor.DARK_GRAY + ChatColor.BOLD + " \u00BB "
							+ ChatColor.RESET + ChatColor.GRAY + msg);
					if (LAST_SPOKE.get(target) != null) {
						LAST_SPOKE.remove(target);
					}
					LAST_SPOKE.put(target, sender);
					if (LAST_SPOKE.get(sender) != null) {
						LAST_SPOKE.remove(sender);
					}
					LAST_SPOKE.put(sender, target);
				} else {
					sender.sendMessage(ChatColor.RED + "Nobody to reply to");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a message to tell.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private boolean me() {
		if (sender.hasPermission("iMine.me")) {
			if (args.length > 0) {
				String msg = "";
				for (int i = 0; i < args.length; i++) {
					msg += args[i] + " ";
				}
				msg = msg.substring(0, msg.length() - 1);
				msg = ColorUtil.replaceColors("&6* &7%s &r" + msg, sender.getName());
				Bukkit.broadcastMessage(msg);
			} else {
				sender.sendMessage(ChatColor.RED + "Need a message to tell.");
			}
		} else {
			noPermission();
		}
		return true;
	}

	private void noPermission() {
		sender.sendMessage(
				ColorUtil.replaceColors("&cYou do not have permission to execute '&6%s&c' command!", command));
	}

	public static List<String> onTabComplete(Player sender, String command, String[] args) {
		List<String> ret = new ArrayList<>();
		if (command.equalsIgnoreCase("hub")) {
			String[] servers = { "creative", "uhc", "hub", "survival", "outlaws", "testserver", "outlawsB" };
			if (args.length == 1) {
				for (String server : servers) {
					if (server.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
						ret.add(server);
					}
				}
			} else if (args.length == 2) {
				ret.addAll(PlayerUtil.getAllOnlineNames(args[args.length - 1], sender));
			}
		} else if (command.equalsIgnoreCase("tab") && args.length == 1) {
			String[] argumenten = { "top", "bottom", "update" };
			for (String arg : argumenten) {
				if (arg.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
					ret.add(arg);
				}
			}
		} else if (command.equalsIgnoreCase("mchistory") && args.length == 1) {
			ret = PlayerUtil.getNamesLike(args[args.length - 1]);
		} else if (command.equalsIgnoreCase("git")) {
			String[] argumenten = { "-v", "-q", "projects" };
			for (String arg : argumenten) {
				if (arg.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
					ret.add(arg);
				}
			}
		} else if (command.equalsIgnoreCase("plr")) {
			for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
				if (plugin.getName().toLowerCase().contains(args[args.length - 1].toLowerCase())) {
					ret.add(plugin.getName());
				}
			}
		} else if ((command.startsWith("gm") && command.length() == 3)
				|| (command.equalsIgnoreCase("gm") && args.length == 2)
				|| (command.equalsIgnoreCase("mute") && args.length == 1)
				|| (command.equalsIgnoreCase("fly") && args.length == 1)
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

	private static void globalAdminMessage(Player sender, String message) {
		DatabaseManager db = BukkitStarter.plugin.getDB();
		ResultSet rs = db.selectQuery("SELECT UUID_Table.LastName FROM AdminRegister JOIN UUID_Table "
				+ "ON UUID_Table.UUID = AdminRegister.UUID;");
		try {
			while (rs.next()) {
				ByteArrayDataOutput out = ByteStreams.newDataOutput();
				out.writeUTF("Message");
				out.writeUTF(rs.getString(1));
				out.writeUTF(message);
				sender.sendPluginMessage(BukkitStarter.plugin, "BungeeCord", out.toByteArray());
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static class NameLookup implements Runnable {
		private final UUID uuid;
		private final CommandSender sender;

		public NameLookup(UUID uuid, CommandSender sender) {
			this.uuid = uuid;
			this.sender = sender;
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
				JsonArray nameChange = new JsonParser().parse(request).getAsJsonArray();
				if (nameChange.size() == 0) {
					sendNameInfo(null, 0L);
				} else {
					for (JsonElement nameInfo : nameChange) {
						JsonObject nameObj = nameInfo.getAsJsonObject();
						if (nameObj.has("changedToAt")) {
							sendNameInfo(nameObj.get("name").getAsString(), nameObj.get("changedToAt").getAsLong());
						} else {
							sendNameInfo(nameObj.get("name").getAsString(), 0L);
						}
					}
					if (nameChange.size() == 1) {
						sender.sendMessage(ColorUtil.replaceColors("&7 Name has never changed since."));
					}
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		private void sendNameInfo(String name, long time) {
			Date d = null;
			if (time > 0L) {
				d = new Date(time);
				if (name == null) {
					sender.sendMessage(ColorUtil.replaceColors("&c This player has no other names"));
				} else {
					sender.sendMessage(ColorUtil.replaceColors("&6 Name: '&c%s&6' changed %s ago.", name,
							DateUtil.timeUntilNow(d)));
				}
			} else {
				sender.sendMessage(ColorUtil.replaceColors("&6Getting al old playernames from '&c%s&6'.", name));
				return;
			}
		}
	}

	private class ServerReporter implements Runnable {
		private final String formatMessage = String.format("%s%s[%s%sREPORT%s%s] %s%s%s %s\u00BB%s %s", ChatColor.RESET,
				ChatColor.BOLD, ChatColor.RED, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET,
				ChatColor.GRAY, "%s", ChatColor.BOLD, ChatColor.RED, "%s");

		public ServerReporter() {
		}

		@Override
		public void run() {
			if (args.length == 0) {
				sender.sendMessage(ChatColor.RED + "/Report [Message]");
				return;
			}
			String message = "";
			for (String str : args) {
				message += str + " ";
			}
			message = ColorUtil.replaceColors(message);
			if (message.matches("^\\s*$")) {
				sender.sendMessage(ChatColor.RED + "/Report [Message]");
				return;
			}
			sender.sendMessage(ChatColor.GOLD + "Message reported!");
			if (sender instanceof Player) {
				CommandHandler.globalAdminMessage((Player) sender,
						String.format(formatMessage, sender.getName(), message));
			} else {
				sender.sendMessage("Player-only");
			}
		}
	}

	private class AdminChat {

		private final String formatMessage = String.format("%s%s[%s%sADMIN%s%s] %s%s%s %s\u00BB%s %s", ChatColor.RESET,
				ChatColor.BOLD, ChatColor.GREEN, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET,
				ChatColor.GRAY, "%s", ChatColor.BOLD, ChatColor.RESET, "%s");

		public AdminChat() {
			if (!(sender instanceof Player)) {
				sender.sendMessage(ColorUtil.replaceColors("&cPlayer Only"));
			} else {
				send();
			}
		}

		public void send() {
			if (!sender.hasPermission("iMine.adminChat") || args.length == 0) {
				sender.sendMessage(ChatColor.RED + "/Admin [Message]");
				return;
			}
			String message = "";
			for (String str : args) {
				message += str + " ";
			}
			message = ColorUtil.replaceColors(message);
			if (message.matches("^\\s*$")) {
				sender.sendMessage(ChatColor.RED + "/Admin [Message]");
				return;
			}
			CommandHandler.globalAdminMessage((Player) sender, String.format(formatMessage, sender.getName(), message));
		}
	}

	private static class ReloadPlugin implements Runnable {
		private static final String FORMAT = String.format("%s%s[%s%sUpdater%s%s]%s %s", ChatColor.RESET,
				ChatColor.BOLD, ChatColor.AQUA, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET, "%s");

		private final CommandSender sender;
		private final String[] args;

		public ReloadPlugin(CommandSender sender, String[] args) {
			this.sender = sender;
			this.args = args;
		}

		@Override
		public void run() {
			Plugin pl = Bukkit.getPluginManager().getPlugin(args[0]);
			if (pl != null) {
				sender.sendMessage(String.format(FORMAT, "Overriding to new plugins"));
				Bukkit.getPluginManager().disablePlugin(pl);
				sender.sendMessage(String.format(FORMAT,
						pl.getName() + " is now disabled [" + pl.getDescription().getVersion() + "]"));
				try {
					Thread.sleep(1500L);
					Method m = JavaPlugin.class.getDeclaredMethod("getFile");
					m.setAccessible(true);
					File f = (File) m.invoke(pl);
					BukkitStarter.plugin.updatePlugins();
					Thread.sleep(500L);
					pl = Bukkit.getPluginManager().loadPlugin(f);
					sender.sendMessage(String.format(FORMAT,
							pl.getName() + " is now reloaded! [" + pl.getDescription().getVersion() + "]"));
				} catch (UnknownDependencyException ex) {
					sender.sendMessage(ChatColor.RED + "Plugin Dependency not correctly: " + ex.getMessage());
					ex.printStackTrace();
				} catch (InvalidPluginException ex) {
					sender.sendMessage(ChatColor.RED + "Plugin invalid. because: " + ex.getMessage());
					ex.printStackTrace();
				} catch (InvalidDescriptionException ex) {
					sender.sendMessage(ChatColor.RED + "Plugin invalid description. because: " + ex.getMessage());
					ex.printStackTrace();
				} catch (Exception ex) {
					sender.sendMessage(ChatColor.RED + "You just fucked-up: " + ex.getMessage());
					ex.printStackTrace();
				}
			} else {
				sender.sendMessage("No plugin with that name.");
			}
		}
	}

	private static class GitCheckRunnalbe implements Runnable {

		private final CommandSender sender;
		// 01 = debug, 10 = stil
		private final byte quietVerbose;
		private final List<Plugin> toUpdate;

		public GitCheckRunnalbe(CommandSender sender, byte verbose) {
			this.sender = sender;
			this.quietVerbose = verbose;
			this.toUpdate = new ArrayList<>();
		}

		private void verboseMessage(String msg) {
			if ((quietVerbose & 0b01) == 1) {
				sender.sendMessage("  " + ChatColor.GRAY + msg);
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
			Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
			Matcher match = p.matcher(version);
			if (match.find()) {
				verboseMessage("plugin " + plugin.getName() + " is a GIT project!");
				GitProject git = BukkitStarter.API.getProjectData(plugin.getName());
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
				// newestversion: %gitshort% [RELOAD SERVER]
				TextComponent extra, message = new TextComponent("");
				// [GIT]
				extra = new TextComponent(String.format("%s%s[%s%sGIT%s%s]%s ", ChatColor.RESET, ChatColor.BOLD,
						ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
				message.addExtra(extra);
				// %plugin naam%
				extra = new TextComponent(String.format("%s%s%s ", ChatColor.GREEN, plugin.getName(), ChatColor.RESET));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl()));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder(git.getDescription())
								.append("\n\nCreated at: " + GitLabAPI.NL_DATE_FORMAT.format(git.getCreateDate()))
								.create()));
				message.addExtra(extra);
				// current verion:
				extra = new TextComponent(String.format("%scurrent version: ", ChatColor.RESET));
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
				extra = new TextComponent(
						String.format("%s%s%s ", ChatColor.GOLD, (current == null ? ChatColor.RED + "not found"
								: current.getTitle().replaceAll(" ", " " + ChatColor.GOLD)), ChatColor.RESET));
				extra.setColor(net.md_5.bungee.api.ChatColor.GOLD);
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/commit/"
						+ (current == null ? "master" : current.getLongId()) + "?view=parallel"));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder((current == null ? ChatColor.RED + match.group(0) + " - commit not found!"
								: "version id: " + current.getShortId())).append("\n\nPushed at: "
										+ (current == null ? "~" : GitLabAPI.NL_DATE_FORMAT.format(current.getWhen())))
										.create()));
				message.addExtra(extra);
				if (commits.isEmpty()) {
					verboseMessage("no current commit found!");
				} else {
					toUpdate.add(plugin);
					// newest verion:
					extra = new TextComponent(String.format("%snewest version: ", ChatColor.RESET));
					message.addExtra(extra);
					// %git short new version%
					extra = new TextComponent(String.format("%s%s%s ", ChatColor.GOLD,
							git.getCommits()[0].getTitle().replaceAll(" ", " " + ChatColor.GOLD), ChatColor.RESET));
					extra.setColor(net.md_5.bungee.api.ChatColor.GOLD);
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/compare/"
							+ (current == null ? "master" : current.getShortId()) + "...master?view=parallel"));
					ComponentBuilder hoverBuilder = new ComponentBuilder(
							"version id: " + git.getCommits()[0].getShortId());
					hoverBuilder.append("\nMissing Versions:");
					for (Commit commit : commits) {
						hoverBuilder.append("\n" + ChatColor.GOLD + " " + ChatColor.GRAY + commit.getTitle() + "  ["
								+ GitLabAPI.NL_DATE_FORMAT.format(commit.getWhen()) + "]");
					}
					hoverBuilder
							.append("\n\nPushed at: " + GitLabAPI.NL_DATE_FORMAT.format(git.getCommits()[0].getWhen()));
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
					message.addExtra(extra);
					// versions behind [#]:
					extra = new TextComponent(
							String.format("%s[%d]%s ", ChatColor.RESET, commits.size(), ChatColor.RESET));
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
				}
				TextComponent extra, message = new TextComponent("");
				// Files to update: #
				extra = new TextComponent(ChatColor.YELLOW + "Files to update: " + updates);
				extra.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
				ComponentBuilder cb = new ComponentBuilder("");
				Iterator<File> newFileI = MktUtil.toList(BukkitStarter.UPDATE_DIR.listFiles()).iterator();
				while (newFileI.hasNext()) {
					File newFile = newFileI.next();
					String append = ChatColor.GOLD + newFile.getName();
					if (newFileI.hasNext()) {
						append += "\n";
					}
					cb.append(append);
				}
				extra.setHoverEvent(new HoverEvent(Action.SHOW_TEXT, cb.create()));
				message.addExtra(extra);
				// GitRepos to update: #
				extra = new TextComponent(ChatColor.AQUA + "  GitRepos to update: " + gitUpdates);
				extra.setColor(net.md_5.bungee.api.ChatColor.AQUA);
				cb = new ComponentBuilder("");
				Iterator<Plugin> toUpdateI = toUpdate.iterator();
				while (toUpdateI.hasNext()) {
					Plugin plugin = toUpdateI.next();
					String append = ChatColor.DARK_AQUA + plugin.getName();
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
				extra = new TextComponent(String.format(" %s%s[%sReload Server%s%s]%s ", ChatColor.RESET,
						ChatColor.BOLD, ChatColor.DARK_GREEN + (updates == 0 ? ChatColor.STRIKETHROUGH.toString() : ""),
						ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
						new ComponentBuilder("Click to reload server").create()));
				message.addExtra(extra);
				// [REBOOT SERVER]
				extra = new TextComponent(String.format("%s%s[%sReboot Server%s%s]", ChatColor.RESET, ChatColor.BOLD,
						ChatColor.RED + (updates == 0 ? ChatColor.STRIKETHROUGH.toString() : ""), ChatColor.RESET,
						ChatColor.BOLD));
				extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/restart"));
				extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(
						ChatColor.RED + "WARNING, will shutdown server!\n" + ChatColor.RESET + "Click to reboot server")
								.create()));
				message.addExtra(extra);
				sendTextComponent(message);
			} else {
				message(ChatColor.GRAY + " No updates found!");
			}
		}

		@Override
		public void run() {
			if (sender.hasPermission("iMine.git")) {
				BukkitStarter.API.refreshData();
				if (!BukkitStarter.API.canWork()) {
					message("This server is outdated -> cant check on GitRepo's");
					return;
				}
				Bukkit.getScheduler().runTaskAsynchronously(BukkitStarter.plugin, () -> {
					prosessData();
				});
			} else {
				sender.sendMessage(ChatColor.RED + "No permission.");
			}
		}
	}
}