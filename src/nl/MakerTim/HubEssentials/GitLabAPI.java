package nl.MakerTim.HubEssentials;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class GitLabAPI {

	private static final String PRIVATE_TOKEN = "f1r_tSSXTUsUAvMzrj5F";
	private static final String URL_PROJECTS = "http://git.imine.nl/api/v3/projects?private_token=%s&sudo=%s";
	private static final String URL_USERS = "http://git.imine.nl/api/v3/users?private_token=%s";

	private Map<String, String[]> projects;
	private List<Integer> ids;

	/**
	 * Lief dagboek,
	 * 
	 * vandaag is sander me aan het grammernazien, ik weet niet hoe je dit spelt
	 * maakt niet uit. verder probeer ik /git wat meer usefull te maken. waarom
	 * weet ik nog niet maar ik had tijd over om niet aan outlaws te werken
	 * 
	 * DAG IEDEREEEEEN, mwho
	 */
	public GitLabAPI() {
		refreshData();
	}

	private void refreshData() {
		try {
			JsonArray je = new JsonParser().parse(getResponseFromURL(new URL(String.format(URL_USERS, PRIVATE_TOKEN))))
					.getAsJsonArray();
			projects = new HashMap<>();
			ids = new ArrayList<>();

			for (int i = 0; i < je.size(); i++) {
				String username = je.get(i).getAsJsonObject().get("username").getAsString();
				if (je.get(i).getAsJsonObject().get("state").getAsString().equals("blocked")) {
					continue;
				}
				JsonArray projectsFromUser = new JsonParser()
						.parse(getResponseFromURL(new URL(String.format(URL_PROJECTS, PRIVATE_TOKEN, username))))
						.getAsJsonArray();
				for (int j = 0; j < projectsFromUser.size(); j++) {
					JsonObject project = projectsFromUser.get(j).getAsJsonObject();
					int projectId = project.get("id").getAsInt();
					if (!ids.contains(projectId)) {
						projects.put(project.get("name").getAsString(),
								new String[] { Integer.toString(projectId), project.get("web_url").getAsString(),
										project.get("description").getAsString(),
										project.get("last_activity_at").getAsString() });
						// DateFormat format = new
						// SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
						// Locale.ENGLISH);
						// Date date = format.parse(string);
						ids.add(projectId);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public String[] getProjectData(String pluginName) {
		refreshData();
		String file;
		if (this.projects.containsKey(pluginName)) {
			file = pluginName;
		} else {
			try {
				file = Bukkit.getPluginManager().getPlugin(pluginName).getClass().getProtectionDomain().getCodeSource()
						.getLocation().getFile().replace(".jar", "").replace("/plugins/", "");
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		System.out.println(pluginName + " " + file);
		for (String str : projects.keySet()) {
			for (OfflinePlayer pl : Bukkit.getOperators()) {
				((Player) pl).sendMessage(str);
			}
		}
		System.out.println();
		return projects.get(file);
	}

	private String getResponseFromURL(URL url) {
		String ret = "";
		try {
			InputStream is = url.openStream();
			Scanner in = new Scanner(is);
			while (in.hasNext()) {
				ret += in.next();
			}

			in.close();
			is.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ret;
	}
}
