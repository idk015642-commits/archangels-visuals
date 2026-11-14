package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.random.Random;

import java.util.Locale;

/** Butterfly talisman logic: the transformation phrase (one per language) and the transformation itself. */
public final class Talisman {
	/** Index = language (en, uk, ru). The first entry is shown as the hint; all entries are accepted. */
	private static final String[][] PHRASES = {
			{
					"Nooroo, dark wings rise"
			},
			{
					"Нуру, най повстануть темні крила",
					"Нуру, нехай повстануть темні крила",
					"Нуру, темні крила, повстаньте",
					"Нууру, най повстануть темні крила",
					"Нууру, нехай повстануть темні крила",
					"Нууру, темні крила, повстаньте"
			},
			{
					"Нууру, темные крылья, восстаньте",
					"Нуру, темные крылья, восстаньте",
					"Нууру, темные крылья, расправьтесь",
					"Нуру, темные крылья, расправьтесь",
					"Нууру, пусть восстанут темные крылья",
					"Нуру, пусть восстанут темные крылья",
					"Нууру, да восстанут темные крылья",
					"Нуру, да восстанут темные крылья"
			}
	};

	private Talisman() {}

	/** The phrase for the current language. */
	public static String phrase() {
		return PHRASES[Lang.index()][0];
	}

	/** True if the input matches the phrase of the current language (case, punctuation and ё/е are ignored). */
	public static boolean matches(String input) {
		String n = normalize(input);
		if (n.isEmpty()) return false;
		for (String variant : PHRASES[Lang.index()]) {
			if (normalize(variant).equals(n)) return true;
		}
		return false;
	}

	private static String normalize(String s) {
		if (s == null) return "";
		s = s.toLowerCase(Locale.ROOT).replace('ё', 'е');
		s = s.replaceAll("[^\\p{L}\\p{N}\\s]", "").replaceAll("\\s+", " ").trim();
		return s;
	}

	public static void transform(MinecraftClient client) {
		VisualsConfig.transformed = true;
		ClientPlayerEntity p = client.player;
		if (p == null || client.world == null) return;

		Random r = client.world.getRandom();
		for (int i = 0; i < 60; i++) {
			double a = r.nextDouble() * Math.PI * 2;
			double d = 0.3 + r.nextDouble();
			client.world.addParticle(ParticleTypes.WITCH,
					p.getX() + Math.cos(a) * d, p.getY() + r.nextDouble() * 2.0, p.getZ() + Math.sin(a) * d,
					0.0, 0.05, 0.0);
		}
		p.playSound(SoundEvents.ENTITY_EVOKER_CAST_SPELL, 1f, 1f);
		p.sendMessage(Text.literal(Lang.t("tal.msg.ok")), true);
	}

	public static void detransform(MinecraftClient client) {
		VisualsConfig.transformed = false;
		if (client.player != null) {
			client.player.sendMessage(Text.literal(Lang.t("tal.msg.off")), true);
		}
	}

	/** Purple sparkles around the wings while transformed. */
	public static void tick(MinecraftClient client) {
		ClientPlayerEntity p = client.player;
		if (!VisualsConfig.transformed || p == null || client.world == null) return;
		if (client.world.getTime() % 3 != 0) return;

		Random r = client.world.getRandom();
		client.world.addParticle(ParticleTypes.WITCH,
				p.getX() + (r.nextDouble() - 0.5) * 1.6,
				p.getY() + 0.6 + r.nextDouble() * 1.2,
				p.getZ() + (r.nextDouble() - 0.5) * 1.6,
				0.0, 0.02, 0.0);
	}
}
