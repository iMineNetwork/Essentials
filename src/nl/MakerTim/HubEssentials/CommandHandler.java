package nl.MakerTim.HubEssentials;

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

	public static boolean onCommand(CommandSender sender, String command, String[] args) {
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
					for (Player pl : Bukkit.getOnlinePlayers()) {
						if (!BukkitStarter.isDev(pl.getUniqueId())) {
							MktUtils.sendPlayerToServer(BukkitStarter.plugin, pl, "hub");
						}
					}
				}
			}
			return true;
		} else if (command.equalsIgnoreCase("git")) {
			if (sender.isOp() || sender.hasPermission("iMine.dev")) {
				for (Plugin pl : Bukkit.getPluginManager().getPlugins()) {
					String version = pl.getDescription().getVersion();
					Pattern p = Pattern.compile("\\b([0-9a-f]{5,40})\\b");
					Matcher match = p.matcher(version);
					if (match.find()) {
						GitProject git = BukkitStarter.API.getProjectData(pl.getName());
						if (git == null) {
							sender.sendMessage(String.format("%s%s [%s] - not in our git", ChatColor.RED.toString(),
									pl.getName(), match.group(0)));
							continue;
						}
						// [GIT] %pluginNaam% currentverion: %gitshort%
						// newestversion: %gitshort% [RELOAD SERVER]
						TextComponent extra, message = new TextComponent("");
						// [GIT]
						extra = new TextComponent(String.format("%s%s[%s%sGIT%s%s]%s ", ChatColor.RESET, ChatColor.BOLD,
								ChatColor.GOLD, ChatColor.BOLD, ChatColor.RESET, ChatColor.BOLD, ChatColor.RESET));
						message.addExtra(extra);
						// %plugin naam%
						extra = new TextComponent(String.format("%s%s%s%s ", ChatColor.GREEN, ChatColor.UNDERLINE,
								pl.getName(), ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl()));
						extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(git.getDescription()).create()));
						message.addExtra(extra);
						// current verion:
						extra = new TextComponent(String.format("%scurrent version: ", ChatColor.RESET));
						message.addExtra(extra);
						// %git short this version%
						Commit current = null;
						int index = 0;
						for (Commit commit : git.getCommits()) {
							index++;
							if (commit.getShortId().equalsIgnoreCase(match.group(0))) {
								current = commit;
							}
						}
						extra = new TextComponent(String.format("%s%s%s%s ", ChatColor.GOLD, ChatColor.UNDERLINE,
								(current == null ? ChatColor.RED + "not found" : current.getTitle()), ChatColor.RESET));
						extra.setUnderlined(true);
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,
								git.getWebUrl() + "/commit/" + (current == null ? "master" : current.getLongId())));
						extra.setHoverEvent(
								new HoverEvent(HoverEvent.Action.SHOW_TEXT,
										new ComponentBuilder((current == null
												? ChatColor.RED + match.group(0) + " - commit not found!"
												: current.getShortId()) + "\n").create()));
						message.addExtra(extra);
						// newest verion:
						extra = new TextComponent(String.format("%snewest version: ", ChatColor.RESET));
						message.addExtra(extra);
						// %git short this version%
						extra = new TextComponent(String.format("%s%s%s%s ", ChatColor.GOLD, ChatColor.UNDERLINE,
								git.getCommits()[0].getTitle(), ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, git.getWebUrl() + "/compare/"
								+ (current == null ? "master" : current.getShortId()) + "...master"));
						extra.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
								new ComponentBuilder(git.getCommits()[0].getShortId() + "\n").create()));
						message.addExtra(extra);
						// versions behind [#]:
						extra = new TextComponent(String.format("versions behind: [%d]%s ", index, ChatColor.RESET));
						message.addExtra(extra);
						// REBOOT
						extra = new TextComponent(String.format("%s%s[%s%sREBOOT SERVER%s%s]%s ", ChatColor.RESET,
								ChatColor.BOLD, ChatColor.RED, ChatColor.UNDERLINE, ChatColor.RESET, ChatColor.BOLD,
								ChatColor.RESET));
						extra.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/reboot"));
						message.addExtra(extra);
						if (sender instanceof Player) {
							((Player) sender).spigot().sendMessage(message);
						} else {
							sender.sendMessage(message.toPlainText());
						}
					}
				}
			}
			return true;
		}
		return false;
	}
}