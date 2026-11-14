package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/** Main module menu. Everything is toggled from here (scroll with the mouse wheel if it does not fit). */
public class MenuScreen extends Screen {
	private static final int PANEL_W = 270;
	private static final int HEADER_H = 44;
	private static final int ROW_H = 20;
	private static final int TOGGLE_W = 40;
	private static final int EXTRA_W = 58;

	private static final int COL_BG = 0xE0140B24;
	private static final int COL_BORDER = 0xFFA855F7;
	private static final int COL_ROW = 0xA0261445;
	private static final int COL_ROW_HOVER = 0xC03A1D66;
	private static final int COL_ON = 0xFFA855F7;
	private static final int COL_OFF = 0xFF3A3450;

	/** A menu row. If {@code on} is null the row has no ON/OFF button and only the extra button. */
	private static class Row {
		final Supplier<String> name;
		final BooleanSupplier on;
		final Runnable toggle;
		final Supplier<String> extraText;
		final Runnable extraClick;

		Row(Supplier<String> name, BooleanSupplier on, Runnable toggle,
			Supplier<String> extraText, Runnable extraClick) {
			this.name = name;
			this.on = on;
			this.toggle = toggle;
			this.extraText = extraText;
			this.extraClick = extraClick;
		}
	}

	private final List<Row> rows = new ArrayList<>();
	private int px, py, ph, visible, scroll = 0;

	public MenuScreen() {
		super(Text.literal("Archangels Visuals"));

		rows.add(new Row(() -> Lang.t("menu.fullbright"), () -> VisualsConfig.fullbright,
				() -> VisualsConfig.fullbright = !VisualsConfig.fullbright, null, null));
		rows.add(new Row(() -> Lang.t("menu.zoom"), () -> VisualsConfig.zoom,
				() -> VisualsConfig.zoom = !VisualsConfig.zoom, null, null));
		rows.add(new Row(() -> Lang.t("menu.hud"), () -> VisualsConfig.hud,
				() -> VisualsConfig.hud = !VisualsConfig.hud, null, null));
		rows.add(new Row(() -> Lang.t("menu.watermark"), () -> VisualsConfig.watermark,
				() -> VisualsConfig.watermark = !VisualsConfig.watermark, null, null));
		rows.add(new Row(() -> Lang.t("menu.autoswap"), () -> AutoSwapConfig.enabled,
				() -> AutoSwapConfig.enabled = !AutoSwapConfig.enabled,
				() -> Lang.t("btn.wheel"),
				() -> MinecraftClient.getInstance().setScreen(new AutoSwapScreen(this))));
		rows.add(new Row(() -> Lang.t("menu.pet"), () -> VisualsConfig.pet,
				() -> VisualsConfig.pet = !VisualsConfig.pet, null, null));
		rows.add(new Row(() -> Lang.t("menu.cosmetics"), () -> VisualsConfig.cosmetics,
				() -> VisualsConfig.cosmetics = !VisualsConfig.cosmetics,
				() -> Lang.t(VisualsConfig.COSMETIC_NAMES[VisualsConfig.cosmeticMode]),
				() -> VisualsConfig.cosmeticMode =
						(VisualsConfig.cosmeticMode + 1) % VisualsConfig.COSMETIC_NAMES.length));
		rows.add(new Row(() -> Lang.t("menu.kwami"), () -> VisualsConfig.kwamiAdded,
				() -> VisualsConfig.kwamiAdded = !VisualsConfig.kwamiAdded, null, null));
		rows.add(new Row(() -> Lang.t("menu.darkwings"), () -> VisualsConfig.transformed,
				() -> {
					MinecraftClient mc = MinecraftClient.getInstance();
					if (VisualsConfig.transformed) Talisman.detransform(mc);
					else Talisman.transform(mc);
				},
				() -> Lang.t("tal.phrase"),
				() -> MinecraftClient.getInstance().setScreen(new TalismanScreen(this))));
		rows.add(new Row(() -> Lang.t("menu.language"), null, null,
				Lang::name, Lang::next));
	}

	@Override
	protected void init() {
		visible = Math.max(3, Math.min(rows.size(), (height - HEADER_H - 16) / ROW_H));
		scroll = Math.max(0, Math.min(scroll, rows.size() - visible));
		ph = HEADER_H + visible * ROW_H + 8;
		px = (width - PANEL_W) / 2;
		py = (height - ph) / 2;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	/** Y of the row at screen position {@code slot} (0 = first visible row). */
	private int rowY(int slot) {
		return py + HEADER_H + slot * ROW_H;
	}

	private int toggleX() {
		return px + PANEL_W - 8 - TOGGLE_W;
	}

	private int extraX() {
		return toggleX() - 4 - EXTRA_W;
	}

	private int extraW(Row r) {
		return r.on == null ? EXTRA_W + 4 + TOGGLE_W : EXTRA_W;
	}

	private static boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		// Panel
		context.fill(px, py, px + PANEL_W, py + ph, COL_BG);
		context.drawBorder(px, py, PANEL_W, ph, COL_BORDER);

		// Header: logo + name
		int logoH = 32;
		Watermark.drawLogo(context, px + 10, py + 5, logoH);
		Watermark.drawName(context, textRenderer, px + 10 + Watermark.logoWidth(logoH) + 8, py + 15, 1.5f);
		context.fill(px + 8, py + HEADER_H - 3, px + PANEL_W - 8, py + HEADER_H - 2, COL_BORDER);

		// Rows
		for (int slot = 0; slot < visible; slot++) {
			Row r = rows.get(scroll + slot);
			int y = rowY(slot);
			boolean hover = inside(mouseX, mouseY, px + 8, y, PANEL_W - 16, ROW_H - 2);
			context.fill(px + 8, y, px + PANEL_W - 8, y + ROW_H - 2, hover ? COL_ROW_HOVER : COL_ROW);
			context.drawTextWithShadow(textRenderer, r.name.get(), px + 14, y + 5, 0xFFFFFFFF);

			if (r.extraText != null) {
				int ex = r.on == null ? extraX() : extraX(), ew = extraW(r);
				boolean eh = inside(mouseX, mouseY, ex, y + 2, ew, ROW_H - 6);
				context.fill(ex, y + 2, ex + ew, y + ROW_H - 4, eh ? 0xFF5B2FA0 : 0xFF44256F);
				context.drawCenteredTextWithShadow(textRenderer, r.extraText.get(), ex + ew / 2, y + 5, 0xFFE9D5FF);
			}

			if (r.on != null) {
				boolean on = r.on.getAsBoolean();
				int tx = toggleX();
				context.fill(tx, y + 2, tx + TOGGLE_W, y + ROW_H - 4, on ? COL_ON : COL_OFF);
				context.drawCenteredTextWithShadow(textRenderer, Lang.t(on ? "common.on" : "common.off"),
						tx + TOGGLE_W / 2, y + 5, on ? 0xFFFFFFFF : 0xFF9A93B5);
			}
		}

		// Scroll indicators
		if (scroll > 0) {
			context.drawCenteredTextWithShadow(textRenderer, "^", px + PANEL_W / 2, py + HEADER_H - 12, 0xFFE9D5FF);
		}
		if (scroll + visible < rows.size()) {
			context.drawCenteredTextWithShadow(textRenderer, "v", px + PANEL_W / 2, py + ph - 10, 0xFFE9D5FF);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			for (int slot = 0; slot < visible; slot++) {
				Row r = rows.get(scroll + slot);
				int y = rowY(slot);
				if (!inside(mx, my, px + 8, y, PANEL_W - 16, ROW_H - 2)) continue;

				if (r.on == null) {
					r.extraClick.run(); // e.g. language row: the whole row cycles
				} else if (r.extraText != null && inside(mx, my, extraX(), y + 2, extraW(r), ROW_H - 6)) {
					r.extraClick.run();
				} else {
					r.toggle.run();
				}
				VisualsConfig.save();
				AutoSwapConfig.save();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean mouseScrolled(double mx, double my, double horizontal, double vertical) {
		if (rows.size() > visible) {
			scroll = Math.max(0, Math.min(rows.size() - visible, scroll - (int) Math.signum(vertical)));
			return true;
		}
		return super.mouseScrolled(mx, my, horizontal, vertical);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (ArchangelsVisualsClient.menuKey != null
				&& ArchangelsVisualsClient.menuKey.matchesKey(keyCode, scanCode)) {
			close();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed() {
		VisualsConfig.save();
		AutoSwapConfig.save();
	}
}
