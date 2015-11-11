package nl.makertim.hubessentials;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

import org.bukkit.entity.Player;

public class MktUtils {

	public static void sendPlayerToServer(final Player pl, final String serverID) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			DataOutputStream dos = new DataOutputStream(baos);
			dos.writeUTF("Connect");
			dos.writeUTF(serverID);
			pl.sendPluginMessage(BukkitStarter.plugin, "BungeeCord", baos.toByteArray());
			baos.close();
			dos.close();
		} catch (Exception ex) {
			System.err.println("Player " + pl.getName() + " has not been sended to " + serverID + " because of "
					+ ex.getMessage());
			pl.sendMessage("You " + " have not been sended to " + serverID + " because of " + ex.getMessage());
		}
	}

}
