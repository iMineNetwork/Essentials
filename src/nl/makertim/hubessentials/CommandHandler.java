package nl.makertim.hubessentials;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.InvalidPluginException;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.UnknownDependencyException;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import nl.makertim.hubessentials.GitLabAPI.Commit;
import nl.makertim.hubessentials.GitLabAPI.GitProject;
import nl.makertim.hubessentials.api.PlayerGetter;

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
		if (command.equalsIgnoreCase("hub") && sender instanceof Player) {
			return hub();
		} else if (command.equalsIgnoreCase("dev")) {
			return dev();
		} else if (command.equalsIgnoreCase("tp")) {
			return tp();
		} else if (command.equalsIgnoreCase("gm")) {
			return gm();
		} else if (command.startsWith("gm") && command.length() == 3) {
			return gmx();
		} else if (command.equalsIgnoreCase("speed")) {
			return speed();
		} else if (command.equalsIgnoreCase("msg")) {
			return msg();
		} else if (command.equalsIgnoreCase("invsee")) {
			return invsee();
		} else if (command.equalsIgnoreCase("endersee")) {
			return endersee();
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
			new Thread(new ServerReporter(sender, args)).start();
			return true;
		} else if (command.equalsIgnoreCase("admin")) {
			new Thread(new AdminChat(sender, args)).start();
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

	private boolean hub() {
		if (args.length == 0) {
			MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, "hub");
		} else if (sender.isOp() || sender.hasPermission("iMine.hub")) {
			MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, args[0]);
		} else {
			sender.sendMessage(ChatColor.RED + "You have no acces to this command");
		}
		return true;
	}

	private boolean dev() {
		if (sender.isOp() || sender.hasPermission("iMine.dev")) {
			BukkitStarter.plugin.devMode = !BukkitStarter.plugin.devMode;
			if (BukkitStarter.plugin.devMode) {
				sender.sendMessage("Devolpermodus is now enabled!");
				for (Player pl : new ArrayList<>(Bukkit.getOnlinePlayers())) {
					if (!BukkitStarter.isDev(pl.getUniqueId())) {
						MktUtils.sendPlayerToServer(BukkitStarter.plugin, pl, "hub");
					}
				}
				for (Player pl : new ArrayList<>(Bukkit.getOnlinePlayers())) {
					pl.kickPlayer("DEV MODE ONLY");
				}
			} else {
				sender.sendMessage("Server now public");
			}
		}
		return true;
	}

	private boolean tp() {
		if (sender.isOp() || sender.hasPermission("iMine.tp")) {
			if (args.length == 1) {
				if (sender instanceof Player) {
					Player pl = PlayerGetter.getOnline(args[0]);
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
				Player who = PlayerGetter.getOnline(args[0]);
				Player target = PlayerGetter.getOnline(args[1]);
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
						Player target = PlayerGetter.getOnline(args[0]);
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean gm() {
		if (sender.isOp() || sender.hasPermission("iMine.gm")) {
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
					Player who = PlayerGetter.getOnline(args[1]);
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean gmx() {
		if (sender.isOp() || sender.hasPermission("iMine.gm")) {
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
				Player who = PlayerGetter.getOnline(args[0]);
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean speed() {
		if (sender.isOp() || sender.hasPermission("iMine.speed")) {
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
						Player who = PlayerGetter.getOnline(args[2]);
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean msg() {
		if (sender.isOp() || sender.hasPermission("iMine.msg")) {
			if (args.length > 1) {
				Player target = PlayerGetter.getOnline(args[0]);
				if (target != null) {
					String msg = "";
					for (int i = 1; i < args.length; i++) {
						msg += args[i] + " ";
					}
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean invsee() {
		if (sender.isOp() || sender.hasPermission("iMine.invsee")) {
			if (args.length == 1) {
				Player target = PlayerGetter.getOnline(args[0]);
				if (sender instanceof Player) {
					Player pl = (Player) sender;
					pl.openInventory(target.getInventory());
				} else {
					sender.sendMessage(ChatColor.RED + "Need a player and a message.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a player.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean endersee() {
		if (sender.isOp() || sender.hasPermission("iMine.endersee")) {
			if (args.length == 1) {
				Player target = PlayerGetter.getOnline(args[0]);
				if (sender instanceof Player) {
					Player pl = (Player) sender;
					pl.openInventory(target.getEnderChest());
				} else {
					sender.sendMessage(ChatColor.RED + "Need a player and a message.");
				}
			} else {
				sender.sendMessage(ChatColor.RED + "Need a player.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean git() {
		if (args.length == 1) {
			if (args[0].equalsIgnoreCase("projects")) {
				Set<String> projects = BukkitStarter.API.getProjects();
				for (String project : projects) {
					sender.sendMessage(project);
				}
			} else if (args[0].equalsIgnoreCase("-v")) {
				new Thread(new GitCheckRunnalbe(sender, true)).start();
			}
		} else {
			new Thread(new GitCheckRunnalbe(sender, false)).start();
		}
		return true;
	}

	private boolean plr() {
		if ((sender.isOp() || sender.hasPermission("iMine.dev")) && args.length > 0) {
			new Thread(new ReloadPlugin(sender, args)).start();
		} else {
			return false;
		}
		return true;
	}

	private boolean _return() {
		if (sender.isOp() || sender.hasPermission("iMine.return")) {
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean vanish() {
		if (sender.isOp() || sender.hasPermission("iMine.vanish")) {
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
				Player target = PlayerGetter.getOnline(args[0]);
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean kill() {
		if (sender.isOp() || sender.hasPermission("iMine.kill")) {
			if (args.length == 0) {
				if (sender instanceof Player) {
					Player sender = (Player) this.sender;
					sender.setHealth(0D);
					sender.sendMessage(ChatColor.GOLD + "Suisided");
				} else {
					sender.sendMessage(ChatColor.RED + "Player only");
				}
			} else {
				Player target = PlayerGetter.getOnline(args[0]);
				if (target != null) {
					target.setHealth(0D);
					sender.sendMessage(ChatColor.GOLD + "Assassinated!");
				} else {
					sender.sendMessage(ChatColor.RED + "Player not found");
				}
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean reply() {
		if (sender.isOp() || sender.hasPermission("iMine.reply")) {
			if (args.length > 0) {
				if (LAST_SPOKE.containsKey(sender)) {
					CommandSender target = LAST_SPOKE.get(sender);
					String msg = "";
					for (int i = 0; i < args.length; i++) {
						msg += args[i] + " ";
					}
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
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	private boolean me() {
		if (sender.isOp() || sender.hasPermission("iMine.reply")) {
			if (args.length > 0) {
				String msg = "";
				for (int i = 0; i < args.length; i++) {
					msg += args[i] + " ";
				}
				Bukkit.broadcastMessage(
						ChatColor.GOLD + "* " + ChatColor.GRAY + sender.getName() + " " + ChatColor.WHITE + msg);
			} else {
				sender.sendMessage(ChatColor.RED + "Need a message to tell.");
			}
		} else {
			sender.sendMessage(ChatColor.RED + "No permission.");
		}
		return true;
	}

	public static List<String> onTabComplete(Player sender, String command, String[] args) {
		List<String> ret = new ArrayList<>();
		if (command.equalsIgnoreCase("hub")) {
			String[] servers = { "creative", "uhc", "hub", "survival", "outlaws" };
			for (String server : servers) {
				if (server.toLowerCase().contains(args[args.length - 1].toLowerCase())) {
					ret.add(server);
				}
			}
		} else if (command.equalsIgnoreCase("git")) {
			String[] argumenten = { "-v", "projects" };
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
				|| (command.equalsIgnoreCase("invsee") && args.length == 1)
				|| (command.equalsIgnoreCase("vanish") && args.length == 1) || (command.equalsIgnoreCase("report"))
				|| (command.equalsIgnoreCase("tp") && (args.length == 1 || args.length == 2))
				|| (command.equalsIgnoreCase("kill") && args.length == 1) || (command.equalsIgnoreCase("reply"))
				|| (command.equalsIgnoreCase("endersee") && args.length == 1) || (command.equalsIgnoreCase("msg"))
				|| (command.equalsIgnoreCase("speed") && args.length > 1) || (command.equalsIgnoreCase("me"))) {
			ret.addAll(PlayerGetter.getAllOnlineNames(args[args.length - 1]));
		} else if (command.equalsIgnoreCase("update")) {
		}
		return ret;
	}

	private static void globalAdminMessage(Player sender, String message) {
		DatabaseManager db = BukkitStarter.plugin.getDB();
		ResultSet rs = db.doQuery("SELECT UUID_Table.LastName FROM AdminRegister JOIN UUID_Table "
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

	private static class ServerReporter implements Runnable {

		private static final String FORMAT_MESSAGE = String.format("%s%s[%s%sREPORT%s%s] %s%s%s %s\u00BB%s %s",
				ChatColor.RESET, ChatColor.BOLD, ChatColor.RED, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD,
				ChatColor.RESET, ChatColor.GRAY, "%s", ChatColor.BOLD, ChatColor.RED, "%s");

		private final Player sender;
		private final String[] args;

		public ServerReporter(CommandSender sender, String[] args) {
			if (sender instanceof Player) {
				this.sender = (Player) sender;
			} else {
				sender.sendMessage("Player-only");
				this.sender = null;
			}
			this.args = args;
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
			message = MktUtils.replaceColors(message);
			if (message.matches("^\\s*$")) {
				sender.sendMessage(ChatColor.RED + "/Report [Message]");
				return;
			}
			sender.sendMessage(ChatColor.GOLD + "Message reported!");
			CommandHandler.globalAdminMessage(sender, String.format(FORMAT_MESSAGE, sender.getName(), message));
		}
	}

	private static class AdminChat implements Runnable {

		private static final String FORMAT_MESSAGE = String.format("%s%s[%s%sADMIN%s%s] %s%s%s %s\u00BB%s %s",
				ChatColor.RESET, ChatColor.BOLD, ChatColor.GREEN, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD,
				ChatColor.RESET, ChatColor.GRAY, "%s", ChatColor.BOLD, ChatColor.RESET, "%s");

		private final Player sender;
		private final String[] args;

		public AdminChat(CommandSender sender, String[] args) {
			if (sender instanceof Player) {
				this.sender = (Player) sender;
			} else {
				sender.sendMessage("Player-only");
				this.sender = null;
			}
			this.args = args;
		}

		@Override
		public void run() {
			if (sender == null) {
				return;
			}
			if (!BukkitStarter.isDev(sender.getUniqueId()) || args.length == 0) {
				sender.sendMessage(ChatColor.RED + "/Admin [Message]");
				return;
			}
			String message = "";
			for (String str : args) {
				message += str + " ";
			}
			message = MktUtils.replaceColors(message);
			if (message.matches("^\\s*$")) {
				sender.sendMessage(ChatColor.RED + "/Admin [Message]");
				return;
			}
			CommandHandler.globalAdminMessage(sender, String.format(FORMAT_MESSAGE, sender.getName(), message));
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
					Thread.sleep(3000L);
					Method m = JavaPlugin.class.getDeclaredMethod("getFile");
					m.setAccessible(true);
					File f = (File) m.invoke(pl);
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
		private final boolean verbose;

		public GitCheckRunnalbe(CommandSender sender, boolean verbose) {
			this.sender = sender;
			this.verbose = verbose;
		}

		private void verboseMessage(String msg) {
			if (verbose) {
				sender.sendMessage("  " + ChatColor.GRAY + msg);
			}
		}

		@Override
		public void run() {
			if (sender.isOp() || sender.hasPermission("iMine.dev")) {
				if (!BukkitStarter.API.canWork()) {
					sender.sendMessage("This server is outdated -> cant check on GitRepo's");
					return;
				}
				sender.sendMessage(
						String.format("%s%s[%s%sGIT%s%s]%s Checking all git repos...", ChatColor.RESET, ChatColor.BOLD,
								ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
				boolean isUpdate = false;
				for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
					verboseMessage("Checking plugin " + pl.getName());
					String version = pl.getDescription().getVersion();
					Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
					Matcher match = p.matcher(version);
					if (match.find()) {
						verboseMessage("plugin " + pl.getName() + " is a GIT project!");
						GitProject git = BukkitStarter.API.getProjectData(pl.getName());
						if (git == null) {
							verboseMessage("but i couldnt find it our system");
							continue;
						}
						/**
						 * Lief dagboek,
						 * 
						 * Een mooie message maakt lelijke code. Mocht je hier
						 * onder nog iets nuttigs mee willen doen, im sorry
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
						extra = new TextComponent(
								String.format("%s%s%s ", ChatColor.GREEN, pl.getName(), ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl()));
						extra.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new ComponentBuilder(git.getDescription())
												.append("\n\nCreated at: "
														+ GitLabAPI.NL_DATE_FORMAT.format(git.getCreateDate()))
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
								String.format("%s%s%s ", ChatColor.GOLD,
										(current == null ? ChatColor.RED + "not found"
												: current.getTitle().replaceAll(" ", " " + ChatColor.GOLD)),
										ChatColor.RESET));
						extra.setColor(net.md_5.bungee.api.ChatColor.GOLD);
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/commit/"
								+ (current == null ? "master" : current.getLongId()) + "?view=parallel"));
						extra.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new ComponentBuilder((current == null
												? ChatColor.RED + match.group(0) + " - commit not found!"
												: "version id: " + current.getShortId()))
														.append("\n\nPushed at: " + (current == null ? "~"
																: GitLabAPI.NL_DATE_FORMAT.format(current.getWhen())))
														.create()));
						message.addExtra(extra);
						if (commits.isEmpty()) {
							verboseMessage("no current commit found!");
						} else {
							isUpdate = true;
							// newest verion:
							extra = new TextComponent(String.format("%snewest version: ", ChatColor.RESET));
							message.addExtra(extra);
							// %git short new version%
							extra = new TextComponent(String.format("%s%s%s ", ChatColor.GOLD,
									git.getCommits()[0].getTitle().replaceAll(" ", " " + ChatColor.GOLD),
									ChatColor.RESET));
							extra.setColor(net.md_5.bungee.api.ChatColor.GOLD);
							extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/compare/"
									+ (current == null ? "master" : current.getShortId()) + "...master?view=parallel"));
							ComponentBuilder hoverBuilder = new ComponentBuilder(
									"version id: " + git.getCommits()[0].getShortId());
							hoverBuilder.append("\nMissing Versions:");
							for (Commit commit : commits) {
								hoverBuilder.append("\n" + ChatColor.GOLD + " " + ChatColor.GRAY + commit.getTitle()
										+ "  [" + GitLabAPI.NL_DATE_FORMAT.format(commit.getWhen()) + "]");
							}
							hoverBuilder.append(
									"\n\nPushed at: " + GitLabAPI.NL_DATE_FORMAT.format(git.getCommits()[0].getWhen()));
							extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverBuilder.create()));
							message.addExtra(extra);
							// versions behind [#]:
							extra = new TextComponent(
									String.format("%s[%d]%s ", ChatColor.RESET, commits.size(), ChatColor.RESET));
							message.addExtra(extra);
						}
						if (sender instanceof Player) {
							((Player) sender).spigot().sendMessage(message);
						} else {
							sender.sendMessage(message.toPlainText());
						}
					}
				}
				if (isUpdate) {
					verboseMessage("update found");
					if (sender instanceof Player) {
						TextComponent extra, message = new TextComponent("");
						extra = new TextComponent(
								"Files to update: [" + BukkitStarter.UPDATE_DIR.listFiles().length + "]");
						message.addExtra(extra);

						extra = new TextComponent(
								String.format("  %s%s[%sRELOAD SERVER%s%s]%s ", ChatColor.RESET, ChatColor.BOLD,
										ChatColor.DARK_GREEN, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"));
						extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder("Click to reload server").create()));
						message.addExtra(extra);

						extra = new TextComponent(String.format("%s%s[%sREBOOT SERVER%s%s]", ChatColor.RESET,
								ChatColor.BOLD, ChatColor.RED, ChatColor.RESET, ChatColor.BOLD));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/restart"));
						extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(ChatColor.RED + "WARNING, will shutdown server!\n"
										+ ChatColor.RESET + "Click to reboot server").create()));
						message.addExtra(extra);
						((Player) sender).spigot().sendMessage(message);
					}
				}
			}
		}
	}
}