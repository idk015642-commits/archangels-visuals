package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;

public final class AutoSwap {
	private static int cooldown = 0;

	private AutoSwap() {}

	/** Keeps the selected item in the offhand, taking it from anywhere in the inventory. */
	public static void tick(MinecraftClient client) {
		if (cooldown > 0) {
			cooldown--;
			return;
		}
		if (!AutoSwapConfig.enabled) return;

		ClientPlayerEntity player = client.player;
		if (player == null || client.interactionManager == null || client.currentScreen != null) return;

		Item wanted = AutoSwapConfig.getSelectedItem();
		if (wanted == null || player.getOffHandStack().isOf(wanted)) return;

		int found = -1;
		for (int i = 0; i < 36; i++) {
			if (player.getInventory().getStack(i).isOf(wanted)) {
				found = i;
				break;
			}
		}
		if (found < 0) return;

		// Player screen handler: hotbar = 36..44, main inventory = 9..35
		int slot = found < 9 ? 36 + found : found;
		// SWAP with button 40 swaps the clicked slot with the offhand
		client.interactionManager.clickSlot(0, slot, 40, SlotActionType.SWAP, player);
		cooldown = 3;
	}
}
