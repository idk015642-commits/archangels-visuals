package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public final class Watermark {
	public static final Identifier LOGO =
			Identifier.of("archangelsvisuals", "textures/gui/logo.png");
	public static final int LOGO_TEX_W = 354;
	public static final int LOGO_TEX_H = 238;

	private static final float TEXT_SCALE = 1.5f;

	private Watermark() {}

	public static void drawLogo(DrawContext context, int x, int y, int height) {
		int width = Math.round(height * (float) LOGO_TEX_W / LOGO_TEX_H);
		context.drawTexture(RenderLayer::getGuiTextured, LOGO, x, y, 0f, 0f,
				width, height, LOGO_TEX_W, LOGO_TEX_H, LOGO_TEX_W, LOGO_TEX_H);
	}

	public static int logoWidth(int height) {
		return Math.round(height * (float) LOGO_TEX_W / LOGO_TEX_H);
	}

	/** Draws the colored name at (x, y) with the given scale. Returns the width in GUI pixels. */
	public static int drawName(DrawContext context, TextRenderer tr, int x, int y, float scale) {
		String first = "Archangels ";
		String second = "Visuals";
		MatrixStack m = context.getMatrices();
		m.push();
		m.scale(scale, scale, 1f);
		int sx = Math.round(x / scale);
		int sy = Math.round(y / scale);
		context.drawTextWithShadow(tr, first, sx, sy, 0xFFE9D5FF);
		context.drawTextWithShadow(tr, second, sx + tr.getWidth(first), sy, 0xFFA855F7);
		m.pop();
		return Math.round((tr.getWidth(first) + tr.getWidth(second)) * scale);
	}

	/** Top-left watermark: logo with the name to the right. Returns the y below it. */
	public static int render(DrawContext context, MinecraftClient client) {
		int logoH = 24;
		int x = 4, y = 4;
		drawLogo(context, x, y, logoH);
		int textY = y + (logoH - Math.round(8 * TEXT_SCALE)) / 2;
		drawName(context, client.textRenderer, x + logoWidth(logoH) + 4, textY, TEXT_SCALE);
		return y + logoH + 4;
	}
}
