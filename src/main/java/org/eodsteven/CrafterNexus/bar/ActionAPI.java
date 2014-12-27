/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.eodsteven.CrafterNexus.bar;

import net.minecraft.server.v1_8_R1.ChatSerializer;
import net.minecraft.server.v1_8_R1.IChatBaseComponent;
import net.minecraft.server.v1_8_R1.PacketPlayOutChat;

import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;

/**
 *
 * @author steve
 */
public class ActionAPI {
 

	public static void sendAnnouncement(Player p, String msg) {
		String s = ChatColor.translateAlternateColorCodes('&', msg);

		IChatBaseComponent icbc = ChatSerializer.a("{\"text\": \"" + s + "\"}");
		PacketPlayOutChat bar = new PacketPlayOutChat(icbc, (byte) 2);
		((CraftPlayer) p).getHandle().playerConnection.sendPacket(bar);
	}

	public static void sendPlayerAnnouncement(Player p, String msg) {
		ActionAPI.sendAnnouncement(p, msg);
	}

	public static void sendServerAnnouncement(String msg) {
		for (Player p : Bukkit.getServer().getOnlinePlayers()) {
			ActionAPI.sendAnnouncement(p, msg);
		}
	}
}
