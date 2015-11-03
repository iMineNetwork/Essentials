package nl.MakerTim.HubEssentials;

import java.io.File;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
import nl.MakerTim.HubEssentials.GitLabAPI.Commit;
import nl.MakerTim.HubEssentials.GitLabAPI.GitProject;

public class CommandHandler {
	public static boolean onCommand(final CommandSender sender, String command, String[] args) {
		if (command.equalsIgnoreCase("hub") && sender instanceof Player) {
			if (args.length == 0) {
				MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, "hub");
			} else if (sender.isOp() || sender.hasPermission("iMine.hub")) {
				MktUtils.sendPlayerToServer(BukkitStarter.plugin, (Player) sender, args[0]);
			} else {
				sender.sendMessage(ChatColor.RED + "You have no acces to this command");
			}
			return true;
		} else if (command.equalsIgnoreCase("dev")) {
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
		} else if (command.equalsIgnoreCase("git")) {
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
		} else if (command.equalsIgnoreCase("plr")) {
			if ((sender.isOp() || sender.hasPermission("iMine.dev")) && args.length > 0) {
				new Thread(new ReloadPlugin(sender, args)).start();
			} else {
				return false;
			}
			return true;
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

	public static List<String> onTabComplete(Player sender, String command, String[] args) {
		List<String> ret = new ArrayList<>();
		if (command.equalsIgnoreCase("hub")) {
			ret.add("creative");
			ret.add("uhc");
			ret.add("hub");
			ret.add("survival");
			ret.add("outlaws");
		} else if (command.equalsIgnoreCase("dev")) {
		} else if (command.equalsIgnoreCase("git")) {
			ret.add("-v");
			ret.add("projects");
		} else if (command.equalsIgnoreCase("plr")) {
			for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
				ret.add(pl.getName());
			}
		} else if (command.equalsIgnoreCase("admin")) {
		} else if (command.equalsIgnoreCase("report")) {
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
			if (args.length == 0) {
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
					Thread.sleep(1500L);
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
								String.format("  %s%s[%sRELOAD SERVER%s%s]%s ", ChatColor.RESET, ChatColor.BOLD,
										ChatColor.DARK_GREEN, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"));
						extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder("Click to reload server").create()));
						message.addExtra(extra);

						extra = new TextComponent(String.format("%s%s[%sREBOOT SERVER%s%s]%s ", ChatColor.RESET,
								ChatColor.BOLD, ChatColor.RED, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
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