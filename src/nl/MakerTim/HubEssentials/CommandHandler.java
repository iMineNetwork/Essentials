package nl.MakerTim.HubEssentials;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
			} else if (sender.isOp() || sender.hasPermission("hub.op")) {
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
			new Thread(new GitCheckRunnalbe(sender)).start();
			return true;
		}
		return false;
	}

	private static class GitCheckRunnalbe implements Runnable {

		private final CommandSender sender;

		public GitCheckRunnalbe(CommandSender sender) {
			this.sender = sender;
		}

		@Override
		public void run() {
			if (sender.isOp() || sender.hasPermission("iMine.dev")) {
				sender.sendMessage(
						String.format("%s%s[%s%sGIT%s%s]%s Checking all git repos...", ChatColor.RESET, ChatColor.BOLD,
								ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
				boolean isUpdate = false;
				for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
					String version = pl.getDescription().getVersion();
					Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
					Matcher match = p.matcher(version);
					if (match.find()) {
						GitProject git = BukkitStarter.API.getProjectData(pl.getName());
						if (git == null) {
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
								break;
							}
							commits.add(commit);
						}
						extra = new TextComponent(
								String.format("%s%s%s ", ChatColor.GOLD,
										(current == null ? ChatColor.RED + "not found"
												: current.getTitle().replaceAll(" ", " " + ChatColor.GOLD)),
										ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
								git.getWebUrl() + "/commit/" + (current == null ? "master" : current.getLongId())));
						extra.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new ComponentBuilder((current == null
												? ChatColor.RED + match.group(0) + " - commit not found!"
												: current.getShortId()))
														.append("\n\nPushed at: " + (current == null ? "~"
																: GitLabAPI.NL_DATE_FORMAT.format(current.getWhen())))
														.create()));
						message.addExtra(extra);
						if (!commits.isEmpty()) {
							isUpdate = true;
							// newest verion:
							extra = new TextComponent(String.format("%snewest version: ", ChatColor.RESET));
							message.addExtra(extra);
							// %git short new version%
							extra = new TextComponent(String.format("%s%s%s ", ChatColor.GOLD,
									git.getCommits()[0].getTitle().replaceAll(" ", " " + ChatColor.GOLD),
									ChatColor.RESET));
							extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/compare/"
									+ (current == null ? "master" : current.getShortId()) + "...master"));
							ComponentBuilder hoverBuilder = new ComponentBuilder("version id: " + git.getCommits()[0].getShortId());
							for (Commit commit : commits) {
								hoverBuilder.append("\n" + ChatColor.GOLD + " " + commit.getTitle() + "  ["
										+ GitLabAPI.NL_DATE_FORMAT.format(commit.getWhen()) + "]");
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
				if (isUpdate && sender instanceof Player) {
					TextComponent extra, message = new TextComponent("");
					extra = new TextComponent(String.format("  %s%s[%sRELOAD SERVER%s%s]%s ", ChatColor.RESET,
							ChatColor.BOLD, ChatColor.DARK_GREEN, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reload"));
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder("Click to reload server").create()));
					message.addExtra(extra);

					extra = new TextComponent(String.format("%s%s[%sREBOOT SERVER%s%s]%s ", ChatColor.RESET,
							ChatColor.BOLD, ChatColor.RED, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
					extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/restart"));
					extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
							new ComponentBuilder(ChatColor.RED + "WARNING, will shutdown server!\n" + ChatColor.RESET
									+ "Click to reboot server").create()));
					message.addExtra(extra);
					((Player) sender).spigot().sendMessage(message);
				}
			}
		}
	}
}