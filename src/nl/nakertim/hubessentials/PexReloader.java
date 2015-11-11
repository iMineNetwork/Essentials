package nl.nakertim.hubessentials;

import org.bukkit.Bukkit;

public class PexReloader implements Runnable {

	@Override
	public void run() {
		System.out.println("[PEX] reload");
		Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "pex reload");
	}

}
