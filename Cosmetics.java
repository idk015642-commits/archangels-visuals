package com.example.archangelsvisuals;

import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;

/** Client-side cape and wings drawn on the local player. */
public final class Cosmetics {
	private static final Identifier CAPE_TEX =
			Identifier.of("archangelsvisuals", "textures/cosmetics/cape.png");
	private static final Identifier WINGS_TEX =
			Identifier.of("archangelsvisuals", "textures/cosmetics/wings.png");
	private static final Identifier DARK_WINGS_TEX =
			Identifier.of("archangelsvisuals", "textures/cosmetics/dark_wings.png");
	private static final int FULL_BRIGHT = 15728880;

	private static final ModelPart CAPE;
	private static final ModelPart WING_LEFT;  // player's left (model +x)
	private static final ModelPart WING_RIGHT; // player's right (model -x)

	static {
		ModelData capeData = new ModelData();
		ModelPartData capeRoot = capeData.getRoot();
		capeRoot.addChild("cape",
				ModelPartBuilder.create().uv(0, 0).cuboid(-5f, 0f, 0f, 10f, 16f, 1f),
				ModelTransform.NONE);
		CAPE = TexturedModelData.of(capeData, 64, 32).createModel().getChild("cape");

		ModelData wingData = new ModelData();
		ModelPartData wingRoot = wingData.getRoot();
		wingRoot.addChild("left",
				ModelPartBuilder.create().uv(0, 0).cuboid(0f, 0f, 0f, 14f, 20f, 1f),
				ModelTransform.NONE);
		wingRoot.addChild("right",
				ModelPartBuilder.create().uv(40, 0).cuboid(-14f, 0f, 0f, 14f, 20f, 1f),
				ModelTransform.NONE);
		ModelPart wingModel = TexturedModelData.of(wingData, 128, 64).createModel();
		WING_LEFT = wingModel.getChild("left");
		WING_RIGHT = wingModel.getChild("right");
	}

	private Cosmetics() {}

	public static void render(WorldRenderContext ctx) {
		MinecraftClient client = MinecraftClient.getInstance();
		ClientPlayerEntity player = client.player;
		if (player == null || client.world == null) return;
		if (!VisualsConfig.capeOn() && !VisualsConfig.wingsOn() && !VisualsConfig.transformed) return;
		if (player.isSpectator() || player.isInvisible()) return;
		if (client.options.getPerspective().isFirstPerson()) return;

		MatrixStack m = ctx.matrixStack();
		VertexConsumerProvider consumers = ctx.consumers();
		if (m == null || consumers == null) return;

		float delta = ctx.tickCounter().getTickProgress(false);
		Vec3d cam = ctx.camera().getPos();
		Vec3d pos = player.getLerpedPos(delta);
		float bodyYaw = MathHelper.lerpAngleDegrees(delta, player.prevBodyYaw, player.bodyYaw);

		m.push();
		m.translate(pos.x - cam.x, pos.y - cam.y, pos.z - cam.z);
		m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180f - bodyYaw));
		m.scale(-1f, -1f, 1f);
		m.translate(0f, -1.501f, 0f);
		if (player.isSneaking()) {
			m.translate(0f, 0.2f, 0f);
		}

		RenderLayer capeLayer = RenderLayer.getEntityCutoutNoCull(CAPE_TEX);
		RenderLayer wingLayer = RenderLayer.getEntityCutoutNoCull(
				VisualsConfig.transformed ? DARK_WINGS_TEX : WINGS_TEX);

		if (VisualsConfig.capeOn()) {
			double dx = player.getX() - player.prevX;
			double dz = player.getZ() - player.prevZ;
			double yawRad = Math.toRadians(bodyYaw);
			double forward = dx * -Math.sin(yawRad) + dz * Math.cos(yawRad);
			float lean = 6f + (float) MathHelper.clamp(forward * 60.0, 0.0, 45.0);
			if (player.isSneaking()) lean += 25f;

			m.push();
			m.translate(0f, 0f, 0.125f);
			m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(lean));
			int light = client.getEntityRenderDispatcher().getLight(player, delta);
			VertexConsumer vc = consumers.getBuffer(capeLayer);
			CAPE.render(m, vc, light, OverlayTexture.DEFAULT_UV);
			m.pop();
		}

		if (VisualsConfig.wingsOn() || VisualsConfig.transformed) {
			m.push();
			if (VisualsConfig.transformed) m.scale(1.3f, 1.3f, 1.3f);
			float time = client.world.getTime() + delta;
			boolean airborne = !player.isOnGround();
			float flap = MathHelper.sin(time * (airborne ? 0.35f : 0.1f)) * (airborne ? 0.35f : 0.08f);

			WING_LEFT.setPivot(2f, -4f, 3.5f);
			WING_LEFT.yaw = -(0.7f + flap);
			WING_RIGHT.setPivot(-2f, -4f, 3.5f);
			WING_RIGHT.yaw = 0.7f + flap;

			VertexConsumer vc = consumers.getBuffer(wingLayer);
			WING_LEFT.render(m, vc, FULL_BRIGHT, OverlayTexture.DEFAULT_UV);
			WING_RIGHT.render(m, vc, FULL_BRIGHT, OverlayTexture.DEFAULT_UV);
			m.pop();
		}

		m.pop();

		if (consumers instanceof VertexConsumerProvider.Immediate imm) {
			if (VisualsConfig.capeOn()) imm.draw(capeLayer);
			if (VisualsConfig.wingsOn() || VisualsConfig.transformed) imm.draw(wingLayer);
		}
	}
}
