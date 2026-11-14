package com.example.archangelsvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** The kwami (Nooroo) flying around the player's head once it has been added to the talisman. */
public final class KwamiPet {
	private static final float SIZE = 0.55f;
	private static final int FULL_BRIGHT = 15728880;

	private static Vec3d pos = Vec3d.ZERO;
	private static Vec3d prev = Vec3d.ZERO;
	private static boolean initialized = false;

	private KwamiPet() {}

	private static Vec3d target(ClientPlayerEntity player, long time) {
		double a = time * 0.06;
		return player.getEyePos().add(Math.cos(a) * 1.1, 0.35 + Math.sin(a * 2.0) * 0.15, Math.sin(a) * 1.1);
	}

	public static void tick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (!VisualsConfig.kwamiAdded || player == null || client.world == null) {
			initialized = false;
			return;
		}

		Vec3d target = target(player, client.world.getTime());
		if (!initialized || pos.squaredDistanceTo(target) > 16 * 16) {
			pos = target;
			prev = target;
			initialized = true;
			return;
		}
		prev = pos;
		pos = pos.lerp(target, 0.25);
	}

	public static void render(WorldRenderContext ctx) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!VisualsConfig.kwamiAdded || !initialized || client.world == null) return;

		MatrixStack m = ctx.matrixStack();
		VertexConsumerProvider consumers = ctx.consumers();
		if (m == null || consumers == null) return;

		float delta = ctx.tickCounter().getTickProgress(false);
		Vec3d p = prev.lerp(pos, delta);
		Vec3d cam = ctx.camera().getPos();
		float time = client.world.getTime() + delta;

		// Wing-flap look: the sprite squashes horizontally a little
		float flap = 1f - 0.12f * Math.abs(MathHelper.sin(time * 0.5f));
		float aspect = KwamiTexture.width() / (float) KwamiTexture.height();
		float halfW = SIZE * aspect * flap / 2f;
		float halfH = SIZE / 2f;

		RenderLayer layer = RenderLayer.getEntityCutoutNoCull(KwamiTexture.get());

		m.push();
		m.translate(p.x - cam.x, p.y - cam.y, p.z - cam.z);
		m.multiply(ctx.camera().getRotation());

		MatrixStack.Entry e = m.peek();
		VertexConsumer vc = consumers.getBuffer(layer);
		vertex(vc, e, -halfW, -halfH, 0f, 1f);
		vertex(vc, e, halfW, -halfH, 1f, 1f);
		vertex(vc, e, halfW, halfH, 1f, 0f);
		vertex(vc, e, -halfW, halfH, 0f, 0f);
		m.pop();

		if (consumers instanceof VertexConsumerProvider.Immediate imm) {
			imm.draw(layer);
		}
	}

	private static void vertex(VertexConsumer vc, MatrixStack.Entry e, float x, float y, float u, float v) {
		vc.vertex(e, x, y, 0f)
				.color(255, 255, 255, 255)
				.texture(u, v)
				.overlay(OverlayTexture.DEFAULT_UV)
				.light(FULL_BRIGHT)
				.normal(e, 0f, 1f, 0f);
	}
}
