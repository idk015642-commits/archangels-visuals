package com.example.archangelsvisuals;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * The kwami sprite. By default a built-in generic butterfly spirit is used.
 * Put your own PNG at config/archangelsvisuals/nooroo.png to replace it.
 */
public final class KwamiTexture {
	private static final Identifier DEFAULT =
			Identifier.of("archangelsvisuals", "textures/kwami/default.png");
	private static final Identifier CUSTOM =
			Identifier.of("archangelsvisuals", "dynamic/nooroo_custom");

	private static boolean tried = false;
	private static boolean custom = false;
	private static int width = 128;
	private static int height = 128;

	private KwamiTexture() {}

	private static void load() {
		if (tried) return;
		tried = true;
		try {
			Path file = FabricLoader.getInstance().getConfigDir()
					.resolve("archangelsvisuals").resolve("nooroo.png");
			if (!Files.exists(file)) return;
			try (InputStream in = Files.newInputStream(file)) {
				NativeImage image = NativeImage.read(in);
				width = image.getWidth();
				height = image.getHeight();
				MinecraftClient.getInstance().getTextureManager()
						.registerTexture(CUSTOM, new NativeImageBackedTexture(image));
				custom = true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static Identifier get() {
		load();
		return custom ? CUSTOM : DEFAULT;
	}

	public static int width() {
		load();
		return width;
	}

	public static int height() {
		load();
		return height;
	}
}
