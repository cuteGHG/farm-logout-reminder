package de.germanpvp.farmlogout;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.text.Text;

import java.util.Locale;

public final class FarmLogoutReminderClient implements ClientModInitializer {
	private static final String TARGET_HOST = "smp.germanpvp.de";

	@Override
	public void onInitializeClient() {
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			if (handler.getServerInfo() == null || !isTargetServer(handler.getServerInfo().address)) {
				return;
			}

			client.execute(() -> showReminder(client));
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

	private static void showReminder(MinecraftClient client) {
		client.setScreen(new ConfirmScreen(
			confirmed -> client.setScreen(null),
			Text.literal("Farm ausgeschaltet?"),
			Text.literal("Hast du die Farm auf smp.germanpvp.de ausgeschaltet?")
		));
	}
}
