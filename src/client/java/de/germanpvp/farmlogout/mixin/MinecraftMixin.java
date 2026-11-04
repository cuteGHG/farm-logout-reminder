package de.germanpvp.farmlogout.mixin;

import de.germanpvp.farmlogout.FarmLogoutReminderClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
	private static boolean confirmedDisconnect;

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"), cancellable = true)
	private void farmLogoutReminder$confirmDisconnect(Screen screen, boolean transferring, CallbackInfo callbackInfo) {
		Minecraft client = (Minecraft) (Object) this;
		if (!shouldAsk(client)) {
			return;
		}

		if (confirmedDisconnect) {
			confirmedDisconnect = false;
			return;
		}

		callbackInfo.cancel();
		client.setScreen(new ConfirmScreen(
			confirmed -> {
				if (confirmed) {
					confirmedDisconnect = true;
					client.disconnect(screen, transferring);
				} else {
					client.setScreen(screen);
				}
			},
			Component.literal("Farm ausschalten?"),
			Component.literal("Hast du die Farm auf smp.germanpvp.de ausgeschaltet?")
		));
	}

	@Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"), cancellable = true)
	private void farmLogoutReminder$confirmDisconnectWithSounds(
		Screen screen,
		boolean transferring,
		boolean stopSounds,
		CallbackInfo callbackInfo
	) {
		Minecraft client = (Minecraft) (Object) this;
		if (!shouldAsk(client)) {
			return;
		}

		if (confirmedDisconnect) {
			confirmedDisconnect = false;
			return;
		}

		callbackInfo.cancel();
		client.setScreen(new ConfirmScreen(
			confirmed -> {
				if (confirmed) {
					confirmedDisconnect = true;
					client.disconnect(screen, transferring, stopSounds);
				} else {
					client.setScreen(screen);
				}
			},
			Component.literal("Farm ausschalten?"),
			Component.literal("Hast du die Farm auf smp.germanpvp.de ausgeschaltet?")
		));
	}

	private static boolean shouldAsk(Minecraft client) {
		if (client.getCurrentServer() == null) {
			return false;
		}

		String host = client.getCurrentServer().ip.toLowerCase(Locale.ROOT);
		int portSeparator = host.indexOf(':');
		if (portSeparator >= 0) {
			host = host.substring(0, portSeparator);
		}
		return "smp.germanpvp.de".equals(host);
	}
}
