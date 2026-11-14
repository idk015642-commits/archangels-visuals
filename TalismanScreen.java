package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Butterfly talisman: add the kwami, type the transformation phrase in your language. */
public class TalismanScreen extends Screen {
	private static final int W = 290;
	private static final int H = 186;
	private static final int COL_BG = 0xE0140B24;
	private static final int COL_BORDER = 0xFFA855F7;
	private static final int GREEN = 0xFF7CFFB0;
	private static final int RED = 0xFFFF6B6B;
	private static final int GREY = 0xFFB9A8D6;

	private final Screen parent;
	private TextFieldWidget field;
	private int px, py;
	private String statusKey;
	private int statusColor = GREY;

	public TalismanScreen(Screen parent) {
		super(Text.literal("Talisman"));
		this.parent = parent;
		this.statusKey = VisualsConfig.transformed ? "tal.msg.ok" : "tal.msg.hint";
		if (VisualsConfig.transformed) statusColor = GREEN;
	}

	@Override
	protected void init() {
		px = (width - W) / 2;
		py = (height - H) / 2;

		field = new TextFieldWidget(textRenderer, px + 14, py + 112, W - 28, 18, Text.empty());
		field.setMaxLength(80);
		field.setPlaceholder(Text.literal(Lang.t("tal.placeholder")));
		addDrawableChild(field);
		setInitialFocus(field);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}

	@Override
	public void close() {
		VisualsConfig.save();
		MinecraftClient.getInstance().setScreen(parent);
	}

	// --- geometry -------------------------------------------------------

	private boolean inside(double mx, double my, int x, int y, int w, int h) {
		return mx >= x && mx < x + w && my >= y && my < y + h;
	}

	private int slotX() { return px + 14; }
	private int slotY() { return py + 56; }
	private int addX() { return px + 62; }
	private int addY() { return py + 76; }
	private static final int ADD_W = 120;
	private int actionY() { return py + 138; }

	// --- drawing --------------------------------------------------------

	@Override
	public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
		super.renderBackground(context, mouseX, mouseY, delta);
		context.fill(px, py, px + W, py + H, COL_BG);
		context.drawBorder(px, py, W, H, COL_BORDER);
	}

	private void button(DrawContext c, int x, int y, int w, int h, String text, boolean hover, int color) {
		c.fill(x, y, x + w, y + h, hover ? brighten(color) : color);
		c.drawCenteredTextWithShadow(textRenderer, text, x + w / 2, y + (h - 8) / 2, 0xFFFFFFFF);
	}

	private static int brighten(int argb) {
		int r = Math.min(255, ((argb >> 16) & 0xFF) + 25);
		int g = Math.min(255, ((argb >> 8) & 0xFF) + 25);
		int b = Math.min(255, (argb & 0xFF) + 25);
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta); // background panel + text field

		// Header
		context.drawTexture(RenderLayer::getGuiTextured, Identifierz.TALISMAN, px + 10, py + 8,
				0f, 0f, 40, 40, 128, 128, 128, 128);
		context.drawTextWithShadow(textRenderer, Lang.t("tal.title"), px + 58, py + 14, 0xFFE9D5FF);
		context.drawTextWithShadow(textRenderer, Lang.t("tal.kwami") + ": " + Lang.t("tal.kwami.name"),
				px + 58, py + 28, 0xFFA855F7);
		context.fill(px + 8, py + 51, px + W - 8, py + 52, COL_BORDER);

		// Kwami slot
		int sx = slotX(), sy = slotY();
		boolean slotHover = inside(mouseX, mouseY, sx, sy, 40, 40);
		context.fill(sx - 1, sy - 1, sx + 41, sy + 41, VisualsConfig.kwamiAdded ? 0xFFA855F7 : 0xFF554A70);
		context.fill(sx, sy, sx + 40, sy + 40, slotHover ? 0xFF2C1650 : 0xFF1E0F38);
		if (VisualsConfig.kwamiAdded) {
			int kw = KwamiTexture.width(), kh = KwamiTexture.height();
			int dh = 34, dw = Math.max(1, Math.round(dh * kw / (float) kh));
			dw = Math.min(dw, 38);
			context.drawTexture(RenderLayer::getGuiTextured, KwamiTexture.get(),
					sx + (40 - dw) / 2, sy + 3, 0f, 0f, dw, dh, kw, kh, kw, kh);
		}

		context.drawTextWithShadow(textRenderer,
				VisualsConfig.kwamiAdded ? Lang.t("tal.kwami.name") : Lang.t("tal.kwami.empty"),
				addX(), py + 60, VisualsConfig.kwamiAdded ? 0xFFFFFFFF : 0xFF8F86A8);
		button(context, addX(), addY(), ADD_W, 18,
				Lang.t(VisualsConfig.kwamiAdded ? "tal.remove" : "tal.add"),
				inside(mouseX, mouseY, addX(), addY(), ADD_W, 18),
				VisualsConfig.kwamiAdded ? 0xFF6B2D5C : 0xFF44256F);

		// Phrase hint + input label
		context.drawTextWithShadow(textRenderer, Lang.t("tal.phrase") + ": " + Talisman.phrase(),
				px + 14, py + 98, GREY);

		// Transform / detransform
		boolean on = VisualsConfig.transformed;
		button(context, px + 14, actionY(), W - 28, 18,
				Lang.t(on ? "tal.detransform" : "tal.transform"),
				inside(mouseX, mouseY, px + 14, actionY(), W - 28, 18),
				on ? 0xFF6B2D5C : 0xFF7B3FD0);

		// Status
		context.drawCenteredTextWithShadow(textRenderer, Lang.t(statusKey), px + W / 2, py + 165, statusColor);
	}

	// --- input ----------------------------------------------------------

	private void setStatus(String key, int color) {
		statusKey = key;
		statusColor = color;
	}

	private void toggleKwami() {
		VisualsConfig.kwamiAdded = !VisualsConfig.kwamiAdded;
		VisualsConfig.save();
		setStatus("tal.msg.hint", GREY);
	}

	private void action() {
		MinecraftClient client = MinecraftClient.getInstance();
		if (VisualsConfig.transformed) {
			Talisman.detransform(client);
			setStatus("tal.msg.off", GREY);
			return;
		}
		if (!VisualsConfig.kwamiAdded) {
			setStatus("tal.msg.needkwami", RED);
			return;
		}
		if (Talisman.matches(field.getText())) {
			Talisman.transform(client);
			field.setText("");
			setStatus("tal.msg.ok", GREEN);
		} else {
			setStatus("tal.msg.wrong", RED);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		if (button == 0) {
			if (inside(mx, my, slotX(), slotY(), 40, 40)
					|| inside(mx, my, addX(), addY(), ADD_W, 18)) {
				toggleKwami();
				return true;
			}
			if (inside(mx, my, px + 14, actionY(), W - 28, 18)) {
				action();
				return true;
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
			action();
			return true;
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public void removed() {
		VisualsConfig.save();
	}
}
