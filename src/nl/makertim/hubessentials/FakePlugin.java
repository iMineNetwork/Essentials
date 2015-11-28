package nl.makertim.hubessentials;

import org.bukkit.plugin.java.JavaPlugin;

public class FakePlugin extends JavaPlugin {

	@Override
	public void onEnable() {
		System.out.println("Fake plugin loaded");
	}

	@Override
	public void onDisable() {
		System.out.println("Fake plugin unloaded");
	}
}
