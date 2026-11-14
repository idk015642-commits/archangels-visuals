package com.example.archangelsvisuals;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.Item;
import org.lwjgl.glfw.GLFW;

public class ArchangelsVisualsClient implements ClientModInitializer {
	private static final double FULLBRIGHT_GAMMA = 16.0;
	private static final int ZOOM_FOV = 20;

	/** The only key: opens the menu (Right Shift by default). Everything else is toggled in the menu. */
	public static KeyBinding menuKey;

	private static boolean fullbrightApplied = false;
	private static boolean zoomApplied = false;
	private static double savedGamma = 1.0;
	private static int savedFov = 70;

	@Override
	public void onInitializeClient() {
		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.archangelsvisuals.menu", InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT, "category.archangelsvisuals"));

		ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
		HudRenderCallback.EVENT.register(this::renderHud);
		WorldRenderEvents.AFTER_ENTITIES.register(ctx -> {
			Cosmetics.render(ctx);
			PlanktonPet.render(ctx);
			KwamiPet.render(ctx);
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(ArchangelsVisualsClient::restoreAll);
	}

	private void onTick(MinecraftClient client) {
		while (menuKey.wasPressed()) {
			if (client.currentScreen == null && client.player != null) {
				client.setScreen(new MenuScreen());
			}
		}

		// Fullbright
		if (VisualsConfig.fullbright && !fullbrightApplied) {
			savedGamma = client.options.getGamma().getValue();
			client.options.getGamma().setValue(FULLBRIGHT_GAMMA);
			fullbrightApplied = true;
		} else if (!VisualsConfig.fullbright && fullbrightApplied) {
			client.options.getGamma().setValue(savedGamma);
			fullbrightApplied = false;
		}

		// Zoom
		if (VisualsConfig.zoom && !zoomApplied) {
			savedFov = client.options.getFov().getValue();
			client.options.getFov().setValue(ZOOM_FOV);
			zoomApplied = true;
		} else if (!VisualsConfig.zoom && zoomApplied) {
			client.options.getFov().setValue(savedFov);
			zoomApplied = false;
		}

		PlanktonPet.tick(client);
		KwamiPet.tick(client);
		Talisman.tick(client);
		AutoSwap.tick(client);
	}

	private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player == null || client.options.hudHidden
				|| client.getDebugHud().shouldShowDebugHud()) {
			return;
		}

		int y = 4;
		if (VisualsConfig.watermark) {
			y = Watermark.render(context, client);
		}

		if (!VisualsConfig.hud) return;

		Item swapItem = AutoSwapConfig.getSelectedItem();
		String swapName = swapItem == null ? Lang.t("hud.none") : swapItem.getName().getString();

		String[] lines = {
				"FPS: " + client.getCurrentFps(),
				String.format("XYZ: %d %d %d",
						client.player.getBlockX(), client.player.getBlockY(), client.player.getBlockZ()),
				Lang.t("hud.facing") + ": " + Lang.t("dir." + client.player.getHorizontalFacing().asString()),
				Lang.t("hud.autoswap") + ": " + Lang.t(AutoSwapConfig.enabled ? "common.on" : "common.off") + " (" + swapName + ")"
		};

		for (String line : lines) {
			context.drawTextWithShadow(client.textRenderer, line, 4, y, 0xFFFFFFFF);
			y += 10;
		}
	}

	private static void restoreAll(MinecraftClient client) {
		if (fullbrightApplied) {
			client.options.getGamma().setValue(savedGamma);
			fullbrightApplied = false;
		}
		if (zoomApplied) {
			client.options.getFov().setValue(savedFov);
			zoomApplied = false;
		}
	}
}
