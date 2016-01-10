package nl.makertim.essentials;

import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import nl.imine.api.util.WebUtil;

public class GitLabAPI {

	private static final String PRIVATE_TOKEN = "f1r_tSSXTUsUAvMzrj5F";
	private static final String URL_PROJECTS = "http://git.imine.nl/api/v3/projects?private_token=%s&sudo=%s";
	private static final String URL_USERS = "http://git.imine.nl/api/v3/users?private_token=%s";
	private static final String URL_COMMITS = "http://git.imine.nl/api/v3/projects/%d/repository/commits?private_token=%s";
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
	public static final DateFormat NL_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");

	private Map<String, GitProject> projects;
	private List<Integer> ids;
	private boolean canWork = true;

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

	public void refreshData() {
		try {
			JsonArray je = new JsonParser().parse(WebUtil.getResponse(new URL(String.format(URL_USERS, PRIVATE_TOKEN))))
					.getAsJsonArray();
			projects = new HashMap<>();
			ids = new ArrayList<>();

			for (int i = 0; i < je.size(); i++) {
				String username = je.get(i).getAsJsonObject().get("username").getAsString();
				if (je.get(i).getAsJsonObject().get("state").getAsString().equals("blocked")) {
					continue;
				}
				JsonArray projectsFromUser = new JsonParser()
						.parse(WebUtil.getResponse(new URL(String.format(URL_PROJECTS, PRIVATE_TOKEN, username))))
						.getAsJsonArray();
				for (int j = 0; j < projectsFromUser.size(); j++) {
					JsonObject project = projectsFromUser.get(j).getAsJsonObject();
					int projectId = project.get("id").getAsInt();
					if (!ids.contains(projectId)) {
						GitProject oldDtls = projects.get(project.get("name").getAsString());
						String data = project.get("last_activity_at").getAsString();
						if (data.isEmpty() || (oldDtls != null
								&& isNewerProject(oldDtls.getLastActivity(), DATE_FORMAT.parse(data)))) {
							continue;
						}

						JsonArray commitsJson = new JsonParser()
								.parse(WebUtil
										.getResponse(new URL(String.format(URL_COMMITS, projectId, PRIVATE_TOKEN))))
								.getAsJsonArray();
						Commit[] commits = new Commit[commitsJson.size()];
						for (int k = 0; k < commitsJson.size(); k++) {
							JsonObject commitJson = commitsJson.get(k).getAsJsonObject();
							commits[k] = new Commit(commitJson.get("short_id").getAsString(),
									commitJson.get("id").getAsString(), commitJson.get("title").getAsString(),
									commitJson.get("message").getAsString(),
									parseStringToDate(commitJson.get("created_at").getAsString()));
						}

						projects.put(project.get("name").getAsString(),
								new GitProject(projectId, project.get("web_url").getAsString(),
										project.get("description").getAsString(),
										parseStringToDate(project.get("last_activity_at").getAsString()), commits,
										parseStringToDate(project.get("created_at").getAsString())));
						ids.add(projectId);
					}
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		} catch (Error er) {
			canWork = false;
			System.err.println(er.getLocalizedMessage());
		}
	}

	public Set<String> getProjects() {
		return projects.keySet();
	}

	public boolean canWork() {
		return canWork;
	}

	private Date parseStringToDate(String str) {
		Date ret = new Date();
		try {
			ret = DATE_FORMAT.parse(str);
		} catch (Exception ex) {
			System.out.println("Couldnt get date");
		}
		return ret;
	}

	private boolean isNewerProject(Date dateA, Date dateB) {
		try {
			return dateA.after(dateB);
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public GitProject getProjectData(String pluginName) {
		if (!canWork) {
			return null;
		}
		String file;
		if (this.projects.containsKey(pluginName)) {
			file = pluginName;
		} else {
			try {
				file = Bukkit.getPluginManager().getPlugin(pluginName).getClass().getProtectionDomain().getCodeSource()
						.getLocation().getFile().replace(".jar", "").replaceAll(".*\\/plugins\\/", "");
			} catch (Exception ex) {
				ex.printStackTrace();
				return null;
			}
		}
		for (String project : projects.keySet()) {
			if (project.equalsIgnoreCase(file)) {
				return projects.get(project);
			}
		}
		return null;
	}

	public static class Commit {
		private String shortId;
		private String longId;
		private String title;
		private String message;
		private Date when;

		public Commit(String shortId, String longId, String title, String message, Date when) {
			this.shortId = shortId;
			this.longId = longId;
			this.title = title;
			this.message = message;
			this.when = when;
		}

		public Date getWhen() {
			return when;
		}

		public String getLongId() {
			return longId;
		}

		public String getMessage() {
			return message;
		}

		public String getShortId() {
			return shortId;
		}

		public String getTitle() {
			return title;
		}
	}

	public static class GitProject {
		private int projectId;
		private String webUrl;
		private String description;
		private Date lastActivity;
		private Commit[] commits;
		private Date createDate;

		public GitProject(int projectId, String webUrl, String description, Date lastActivity, Commit[] commits,
				Date createDate) {
			this.projectId = projectId;
			this.webUrl = webUrl;
			this.description = description;
			this.lastActivity = lastActivity;
			this.commits = commits;
			this.createDate = createDate;
		}

		public Commit[] getCommits() {
			return commits;
		}

		public String getDescription() {
			return description;
		}

		public Date getLastActivity() {
			return lastActivity;
		}

		public int getProjectId() {
			return projectId;
		}

		public String getWebUrl() {
			return webUrl;
		}

		public Date getCreateDate() {
			return createDate;
		}
	}
}
