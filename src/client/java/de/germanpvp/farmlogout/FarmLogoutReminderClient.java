package de.germanpvp.farmlogout;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;

public final class FarmLogoutReminderClient implements ClientModInitializer {
	private static final String TARGET_HOST = "smp.germanpvp.de";
	private boolean targetServer;
	private boolean reminderPending;

	@Override
	public void onInitializeClient() {
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (reminderPending) {
				reminderPending = false;
				showReminder(client);
			}
		});

		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			var server = client.getCurrentServer();
			targetServer = server != null && isTargetServer(server.ip);
		});

		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (!targetServer) {
				return;
			}

			targetServer = false;
			reminderPending = true;
		});
	}

	private static boolean isTargetServer(String address) {
		String host = address.toLowerCase(Locale.ROOT);
		int portSeparator = host.indexOf(':');
		if (portSeparator >= 0) {
			host = host.substring(0, portSeparator);
		}
		return TARGET_HOST.equals(host);
	}

	private static void showReminder(Minecraft client) {
		invokeSetScreen(client, new ConfirmScreen(
			confirmed -> invokeSetScreen(client, null),
			Component.literal("Farm ausgeschaltet?"),
			Component.literal("Hast du die Farm auf smp.germanpvp.de ausgeschaltet?")
		));
	}

	private static void invokeSetScreen(Minecraft client, Screen screen) {
		try {
			client.getClass().getMethod("setScreen", Screen.class).invoke(client, screen);
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Minecraft screen API is unavailable", exception);
		}
	}
}
