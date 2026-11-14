package com.example.archangelsvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class AutoSwapConfig {
	public static final int SIZE = 8;

	public static String[] slots = new String[SIZE];
	public static int selected = 0;
	public static boolean enabled = true;

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH =
			FabricLoader.getInstance().getConfigDir().resolve("archangelsvisuals_autoswap.json");

	private static class Data {
		String[] slots;
		int selected;
		boolean enabled;
	}

	static {
		Arrays.fill(slots, "");
		slots[0] = "minecraft:totem_of_undying";
		load();
	}

	private AutoSwapConfig() {}

	public static void load() {
		try {
			if (!Files.exists(PATH)) return;
			Data d = GSON.fromJson(Files.readString(PATH), Data.class);
			if (d == null || d.slots == null) return;
			for (int i = 0; i < SIZE; i++) {
				slots[i] = i < d.slots.length && d.slots[i] != null ? d.slots[i] : "";
			}
			selected = Math.max(0, Math.min(SIZE - 1, d.selected));
			enabled = d.enabled;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		try {
			Data d = new Data();
			d.slots = slots;
			d.selected = selected;
			d.enabled = enabled;
			Files.writeString(PATH, GSON.toJson(d));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Item getItem(int index) {
		String id = slots[index];
		if (id == null || id.isEmpty()) return null;
		Identifier ident = Identifier.tryParse(id);
		if (ident == null || !Registries.ITEM.containsId(ident)) return null;
		return Registries.ITEM.get(ident);
	}

	public static Item getSelectedItem() {
		return getItem(selected);
	}

	public static void setItem(int index, Item item) {
		slots[index] = item == null ? "" : Registries.ITEM.getId(item).toString();
	}

	public static int indexOf(Item item) {
		for (int i = 0; i < SIZE; i++) {
			if (getItem(i) == item) return i;
		}
		return -1;
	}

	public static int firstEmpty() {
		for (int i = 0; i < SIZE; i++) {
			if (slots[i] == null || slots[i].isEmpty()) return i;
		}
		return -1;
	}
}
