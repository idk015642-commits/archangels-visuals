package com.example.archangelsvisuals;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Module states. Fullbright and Zoom are session-only on purpose (they touch game options). */
public final class VisualsConfig {
	public static final String[] COSMETIC_NAMES = {"cosmetic.cape", "cosmetic.wings"};

	public static boolean fullbright = false;
	public static boolean transformed = false; // session-only
	public static boolean zoom = false;

	public static boolean hud = true;
	public static boolean watermark = true;
	public static boolean pet = false;
	public static boolean cosmetics = true;
	public static int cosmeticMode = 0; // 0 = cape, 1 = wings
	public static boolean kwamiAdded = false;
	public static int language = -1;    // -1 = follow the game language

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static final Path PATH =
			FabricLoader.getInstance().getConfigDir().resolve("archangelsvisuals_modules.json");

	private static class Data {
		boolean hud = true;
		boolean watermark = true;
		boolean pet = false;
		boolean cosmetics = true;
		int cosmeticMode = 0;
		int language = -1;
		boolean kwamiAdded = false;
	}

	static {
		load();
	}

	private VisualsConfig() {}

	public static boolean capeOn() {
		return cosmetics && cosmeticMode == 0;
	}

	public static boolean wingsOn() {
		return cosmetics && cosmeticMode == 1;
	}

	public static void load() {
		try {
			if (!Files.exists(PATH)) return;
			Data d = GSON.fromJson(Files.readString(PATH), Data.class);
			if (d == null) return;
			hud = d.hud;
			watermark = d.watermark;
			pet = d.pet;
			cosmetics = d.cosmetics;
			cosmeticMode = Math.max(0, Math.min(COSMETIC_NAMES.length - 1, d.cosmeticMode));
			kwamiAdded = d.kwamiAdded;
			language = d.language >= 0 && d.language < Lang.NAMES.length ? d.language : -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void save() {
		try {
			Data d = new Data();
			d.hud = hud;
			d.watermark = watermark;
			d.pet = pet;
			d.cosmetics = cosmetics;
			d.cosmeticMode = cosmeticMode;
			d.language = language;
			d.kwamiAdded = kwamiAdded;
			Files.writeString(PATH, GSON.toJson(d));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
