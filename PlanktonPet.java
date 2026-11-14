package com.example.archangelsvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Client-side pet: a Plankton sprite that follows the player. */
public final class PlanktonPet {
	private static final Identifier TEXTURE =
			Identifier.of("archangelsvisuals", "textures/pet/plankton.png");
	private static final float SIZE = 0.9f;
	private static final int FULL_BRIGHT = 15728880;

	private static Vec3d pos = Vec3d.ZERO;
	private static Vec3d prev = Vec3d.ZERO;
	private static boolean initialized = false;

	private PlanktonPet() {}

	public static void tick(MinecraftClient client) {
		ClientPlayerEntity player = client.player;
		if (!VisualsConfig.pet || player == null || client.world == null) {
			initialized = false;
			return;
		}

		double yaw = Math.toRadians(player.getYaw());
		double fx = -Math.sin(yaw), fz = Math.cos(yaw);   // forward
		double rx = -Math.cos(yaw), rz = -Math.sin(yaw);  // right
		Vec3d target = player.getPos().add(rx * 1.2 - fx * 0.6, 0.7, rz * 1.2 - fz * 0.6);

		if (!initialized || pos.squaredDistanceTo(target) > 16 * 16) {
			pos = target;
			prev = target;
			initialized = true;
			return;
		}
		prev = pos;
		pos = pos.lerp(target, 0.12);
	}

	public static void render(WorldRenderContext ctx) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (!VisualsConfig.pet || !initialized || client.world == null) return;

		float delta = ctx.tickCounter().getTickProgress(false);
		Vec3d p = prev.lerp(pos, delta);
		float bob = MathHelper.sin((client.world.getTime() + delta) * 0.12f) * 0.07f;
		Vec3d cam = ctx.camera().getPos();

		MatrixStack m = ctx.matrixStack();
		VertexConsumerProvider consumers = ctx.consumers();
		if (m == null || consumers == null) return;

		RenderLayer layer = RenderLayer.getEntityCutoutNoCull(TEXTURE);

		m.push();
		m.translate(p.x - cam.x, p.y + bob - cam.y, p.z - cam.z);
		m.multiply(ctx.camera().getRotation());

		MatrixStack.Entry e = m.peek();
		VertexConsumer vc = consumers.getBuffer(layer);
		float h = SIZE / 2f;
		vertex(vc, e, -h, 0f, 0f, 1f);
		vertex(vc, e, h, 0f, 1f, 1f);
		vertex(vc, e, h, SIZE, 1f, 0f);
		vertex(vc, e, -h, SIZE, 0f, 0f);
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
