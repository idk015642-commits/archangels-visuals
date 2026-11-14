package com.example.archangelsvisuals;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class AutoSwapScreen extends Screen {
	private static final int SLOT = 22;
	private static final int INV_SLOT = 18;

	private int cx, cy, radius, invX, invY;

	private final Screen parent;

	public AutoSwapScreen(Screen parent) {
		super(Text.literal("Auto Swap"));
		this.parent = parent;
	}

	@Override
	public void close() {
		AutoSwapConfig.save();
		MinecraftClient.getInstance().setScreen(parent);
	}

	@Override
	protected void init() {
		invX = width / 2 - 9 * INV_SLOT / 2;
		invY = height - (3 * INV_SLOT + 4 + INV_SLOT) - 8;
		cx = width / 2;
		cy = (invY + 34) / 2;
		radius = Math.max(22, Math.min(60, (invY - 46) / 2 - SLOT / 2 - 2));
	}

	private int[] wheelPos(int i) {
		double a = -Math.PI / 2 + i * 2 * Math.PI / AutoSwapConfig.SIZE;
		return new int[]{
				(int) Math.round(cx + Math.cos(a) * radius) - SLOT / 2,
				(int) Math.round(cy + Math.sin(a) * radius) - SLOT / 2
		};
	}

	private int[] invPos(int i) {
		if (i < 9) { // hotbar
			return new int[]{invX + i * INV_SLOT, invY + 3 * INV_SLOT + 4};
		}
		int idx = i - 9;
		return new int[]{invX + (idx % 9) * INV_SLOT, invY + (idx / 9) * INV_SLOT};
	}

	private static boolean inside(double mx, double my, int x, int y, int size) {
		return mx >= x && mx < x + size && my >= y && my < y + size;
	}

	private boolean inCenter(double mx, double my) {
		double dx = mx - cx, dy = my - cy;
		return dx * dx + dy * dy <= 18 * 18;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);

		context.drawCenteredTextWithShadow(textRenderer, Lang.t("swap.title"), width / 2, 5, 0xFFFFFFFF);
		context.drawCenteredTextWithShadow(textRenderer, Lang.t("swap.hint1"), width / 2, 17, 0xFFAAAAAA);
		context.drawCenteredTextWithShadow(textRenderer, Lang.t("swap.hint2"), width / 2, 27, 0xFFAAAAAA);

		// Circle outline
		for (int k = 0; k < 96; k++) {
			double a = k * 2 * Math.PI / 96;
			int px = (int) Math.round(cx + Math.cos(a) * radius);
			int py = (int) Math.round(cy + Math.sin(a) * radius);
			context.fill(px, py, px + 2, py + 2, 0x80FFFFFF);
		}

		ItemStack tooltip = ItemStack.EMPTY;

		// Wheel slots
		for (int i = 0; i < AutoSwapConfig.SIZE; i++) {
			int[] p = wheelPos(i);
			boolean hover = inside(mouseX, mouseY, p[0], p[1], SLOT);
			int border = i == AutoSwapConfig.selected ? 0xFF55FF55 : (hover ? 0xFFFFFFFF : 0xFF666666);
			context.fill(p[0] - 1, p[1] - 1, p[0] + SLOT + 1, p[1] + SLOT + 1, border);
			context.fill(p[0], p[1], p[0] + SLOT, p[1] + SLOT, 0xFF202020);
			Item item = AutoSwapConfig.getItem(i);
			if (item != null) {
				ItemStack st = new ItemStack(item);
				context.drawItem(st, p[0] + 3, p[1] + 3);
				if (hover) tooltip = st;
			}
		}

		// Center toggle
		boolean on = AutoSwapConfig.enabled;
		context.drawCenteredTextWithShadow(textRenderer, Lang.t(on ? "common.on" : "common.off"), cx, cy - 4,
				on ? 0xFF55FF55 : 0xFFFF5555);

		// Inventory
		for (int i = 0; i < 36; i++) {
			int[] p = invPos(i);
			boolean hover = inside(mouseX, mouseY, p[0], p[1], INV_SLOT);
			context.fill(p[0], p[1], p[0] + INV_SLOT - 1, p[1] + INV_SLOT - 1,
					hover ? 0xFF606060 : 0xFF303030);
			ItemStack st = MinecraftClient.getInstance().player.getInventory().getStack(i);
			if (!st.isEmpty()) {
				context.drawItem(st, p[0] + 1, p[1] + 1);
				context.drawStackOverlay(textRenderer, st, p[0] + 1, p[1] + 1);
				if (hover) tooltip = st;
			}
		}

		if (!tooltip.isEmpty()) {
			context.drawItemTooltip(textRenderer, tooltip, mouseX, mouseY);
		}
	}

	@Override
	public boolean mouseClicked(double mx, double my, int button) {
		// Wheel slots
		for (int i = 0; i < AutoSwapConfig.SIZE; i++) {
			int[] p = wheelPos(i);
			if (inside(mx, my, p[0], p[1], SLOT)) {
				if (button == 1) {
					AutoSwapConfig.setItem(i, null);
				} else if (button == 0 && AutoSwapConfig.getItem(i) != null) {
					AutoSwapConfig.selected = i;
				}
				AutoSwapConfig.save();
				return true;
			}
		}

		// Center on/off
		if (button == 0 && inCenter(mx, my)) {
			AutoSwapConfig.enabled = !AutoSwapConfig.enabled;
			AutoSwapConfig.save();
			return true;
		}

		// Inventory: add item to the wheel
		if (button == 0) {
			for (int i = 0; i < 36; i++) {
				int[] p = invPos(i);
				if (inside(mx, my, p[0], p[1], INV_SLOT)) {
					ItemStack st = MinecraftClient.getInstance().player.getInventory().getStack(i);
					if (!st.isEmpty()) addToWheel(st.getItem());
					return true;
				}
			}
		}
		return super.mouseClicked(mx, my, button);
	}

	private void addToWheel(Item item) {
		int existing = AutoSwapConfig.indexOf(item);
		if (existing >= 0) {
			AutoSwapConfig.selected = existing;
		} else {
			int target = AutoSwapConfig.firstEmpty();
			if (target < 0) target = AutoSwapConfig.selected; // wheel full: replace selected
			AutoSwapConfig.setItem(target, item);
			AutoSwapConfig.selected = target;
		}
		AutoSwapConfig.save();
	}

	@Override
	public void removed() {
		AutoSwapConfig.save();
	}
}
