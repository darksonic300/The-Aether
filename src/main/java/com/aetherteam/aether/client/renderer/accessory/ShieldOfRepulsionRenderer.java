package com.aetherteam.aether.client.renderer.accessory;

import com.aetherteam.aether.attachment.AetherDataAttachments;
import com.aetherteam.aether.client.renderer.AetherModelLayers;
import com.aetherteam.aether.item.accessories.miscellaneous.ShieldOfRepulsionItem;
import com.aetherteam.aether.mixin.mixins.client.accessor.PlayerModelAccessor;
import com.aetherteam.nitrogen.ConstantsUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import io.wispforest.accessories.api.client.AccessoryRenderer;
import io.wispforest.accessories.api.client.SimpleAccessoryRenderer;
import io.wispforest.accessories.api.slot.SlotReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class ShieldOfRepulsionRenderer implements SimpleAccessoryRenderer {
    private final HumanoidModel<LivingEntity> shieldModel;
    private final PlayerModel<LivingEntity> shieldModelSlim;
    public final HumanoidModel<LivingEntity> shieldModelArm;
    public final PlayerModel<LivingEntity> dummyArm;
    public final PlayerModel<LivingEntity> dummyArmSlim;

    public ShieldOfRepulsionRenderer() {
        this.shieldModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AetherModelLayers.SHIELD_OF_REPULSION));
        this.shieldModelSlim = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AetherModelLayers.SHIELD_OF_REPULSION_SLIM), true);
        this.shieldModelArm = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(AetherModelLayers.SHIELD_OF_REPULSION_ARM));
        this.dummyArm = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        this.dummyArmSlim = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
    }

    /**
     * Renders the Shield of Repulsion overlay over the player's model in third person.
     *
     * @param stack             The {@link ItemStack} for the accessory.
     * @param reference         The {@link SlotReference} for the accessory.
     * @param poseStack         The rendering {@link PoseStack}.
     * @param entityModel       The {@link EntityModel} for the renderer.
     * @param buffer            The rendering {@link MultiBufferSource}.
     * @param packedLight       The {@link Integer} for the packed lighting for rendering.
     * @param limbSwing         The {@link Float} for the limb swing rotation.
     * @param limbSwingAmount   The {@link Float} for the limb swing amount.
     * @param partialTicks      The {@link Float} for the game's partial ticks.
     * @param ageInTicks        The {@link Float} for the entity's age in ticks.
     * @param netHeadYaw        The {@link Float} for the head yaw rotation.
     * @param headPitch         The {@link Float} for the head pitch rotation.
     */
    @Override
    public <M extends LivingEntity> void render(ItemStack stack, SlotReference reference, PoseStack matrices, EntityModel<M> entityModel, MultiBufferSource multiBufferSource, int light, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        LivingEntity livingEntity = reference.entity();
        ShieldOfRepulsionItem shield = (ShieldOfRepulsionItem) stack.getItem();
        ResourceLocation texture;
        HumanoidModel<LivingEntity> model;

        if (livingEntity instanceof Player player && entityModel instanceof PlayerModel<?> playerModel) {
            PlayerModelAccessor playerModelAccessor = (PlayerModelAccessor) playerModel;
            model = playerModelAccessor.aether$getSlim() ? this.shieldModelSlim : this.shieldModel;
            if (!player.getData(AetherDataAttachments.AETHER_PLAYER).isMoving()) {
                texture = playerModelAccessor.aether$getSlim() ? shield.getShieldOfRepulsionSlimTexture() : shield.getShieldOfRepulsionTexture();
            } else {
                texture = playerModelAccessor.aether$getSlim() ? shield.getShieldOfRepulsionSlimInactiveTexture() : shield.getShieldOfRepulsionInactiveTexture();
            }
        } else {
            model = this.shieldModel;
            Vec3 motion = livingEntity.getDeltaMovement();
            if (motion.x() == 0.0 && (motion.y() == ConstantsUtil.DEFAULT_DELTA_MOVEMENT_Y || motion.y() == 0.0) && motion.z() == 0.0) {
                texture = shield.getShieldOfRepulsionTexture();
            } else {
                texture = shield.getShieldOfRepulsionInactiveTexture();
            }
        }

        this.align(stack, reference, model, matrices);
        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(multiBufferSource, RenderType.entityTranslucent(texture), false);
        model.renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY);
    }


    @Override
    public <M extends LivingEntity> void align(ItemStack stack, SlotReference reference, EntityModel<M> model, PoseStack poseStack) {
        if (model instanceof HumanoidModel<? extends LivingEntity> humanoidModel) {
            AccessoryRenderer.translateToFace(poseStack, humanoidModel, reference.entity());
            AccessoryRenderer.followBodyRotations(reference.entity(), (HumanoidModel<LivingEntity>) humanoidModel);
        }
    }

    /**
     * Renders the Shield of Repulsion overlay over the player's hands in first person.
     * This also renders a dummy model of the player's hand to get around an issue with transparency culling
     * normally making the player's hand invisible.
     *
     * @param stack       The {@link ItemStack} for the Curio.
     * @param poseStack   The rendering {@link PoseStack}.
     * @param buffer      The rendering {@link MultiBufferSource}.
     * @param packedLight The {@link Integer} for the packed lighting for rendering.
     * @param player      The {@link AbstractClientPlayer} to render for.
     * @param arm         The {@link HumanoidArm} to render on.
     */
    public void renderFirstPerson(ItemStack stack, PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, HumanoidArm arm) {
        boolean isSlim = player.getSkin().model() == PlayerSkin.Model.SLIM;
        if (!player.isInvisible()) {
            this.setupHand(isSlim ? this.dummyArmSlim : this.dummyArm, poseStack, buffer, packedLight, player, arm, isSlim);
        }
        this.setupShieldOnHand(stack, this.shieldModelArm, poseStack, buffer, packedLight, player, arm, isSlim);
    }

    /**
     * Handles rendering the shield overlay model over the player's hands.
     *
     * @param stack       The {@link ItemStack} for the Curio.
     * @param model       The player's {@link HumanoidModel}.
     * @param poseStack   The rendering {@link PoseStack}.
     * @param buffer      The rendering {@link MultiBufferSource}.
     * @param packedLight The {@link Integer} for the packed lighting for rendering.
     * @param player      The {@link AbstractClientPlayer} to render for.
     * @param arm         The {@link HumanoidArm} to render on.
     * @param isSlim      Whether the arm model is slim, as a {@link Boolean}.
     */
    private void setupShieldOnHand(ItemStack stack, HumanoidModel<LivingEntity> model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, HumanoidArm arm, boolean isSlim) {
        this.setupModel(model, player);

        ResourceLocation texture;
        ShieldOfRepulsionItem shield = (ShieldOfRepulsionItem) stack.getItem();

        if (!player.getData(AetherDataAttachments.AETHER_PLAYER).isMoving()) {
            texture = shield.getShieldOfRepulsionTexture();
        } else {
            texture = shield.getShieldOfRepulsionInactiveTexture();
        }

        VertexConsumer consumer = ItemRenderer.getArmorFoilBuffer(buffer, RenderType.entityTranslucent(texture), false);
        if (isSlim) {
            poseStack.translate((arm != HumanoidArm.LEFT ? 1.0F : -1.0F) * 0.05F, 0.0F, 0.0F);
        }
        if (arm == HumanoidArm.RIGHT) {
            this.renderShieldOnHand(model.rightArm, poseStack, packedLight, consumer);
        } else if (arm == HumanoidArm.LEFT) {
            this.renderShieldOnHand(model.leftArm, poseStack, packedLight, consumer);
        }
    }

    /**
     * Handles rendering the player's hands.
     *
     * @param model       The player's {@link PlayerModel}.
     * @param poseStack   The rendering {@link PoseStack}.
     * @param buffer      The rendering {@link MultiBufferSource}.
     * @param packedLight The {@link Integer} for the packed lighting for rendering.
     * @param player      The {@link AbstractClientPlayer} to render for.
     * @param arm         The {@link HumanoidArm} to render on.
     * @param isSlim      Whether the arm model is slim, as a {@link Boolean}.
     */
    private void setupHand(PlayerModel<LivingEntity> model, PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, HumanoidArm arm, boolean isSlim) {
        this.setupModel(model, player);

        if (!player.getData(AetherDataAttachments.AETHER_PLAYER).isMoving()) {
            VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
            if (isSlim) {
                poseStack.translate((arm != HumanoidArm.LEFT ? 1.0F : -1.0F) * -0.05F, 0.0F, 0.0F);
            }
            if (arm == HumanoidArm.RIGHT) {
                this.renderHand(model.rightArm, model.rightSleeve, poseStack, packedLight, consumer);
            } else if (arm == HumanoidArm.LEFT) {
                this.renderHand(model.leftArm, model.leftSleeve, poseStack, packedLight, consumer);
            }
        }
    }

    /**
     * Applies basic model properties for an arm model.
     *
     * @param model  The player's {@link PlayerModel}.
     * @param player The {@link AbstractClientPlayer} to render for.
     */
    private void setupModel(HumanoidModel<LivingEntity> model, AbstractClientPlayer player) {
        model.setAllVisible(false);
        model.attackTime = 0.0F;
        model.crouching = false;
        model.swimAmount = 0.0F;
        model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
    }

    /**
     * Renders the shield overlay model on a player's hand.
     *
     * @param shieldArm   The {@link ModelPart} for the arm.
     * @param poseStack   The rendering {@link PoseStack}.
     * @param packedLight The {@link Integer} for the packed lighting for rendering.
     * @param consumer    The {@link VertexConsumer} for rendering.
     */
    private void renderShieldOnHand(ModelPart shieldArm, PoseStack poseStack, int packedLight, VertexConsumer consumer) {
        shieldArm.visible = true;
        shieldArm.xRot = 0.0F;
        shieldArm.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }

    /**
     * Renders a dummy model of the player's hand.
     *
     * @param dummyArm    The {@link ModelPart} for the arm.
     * @param dummySleeve The {@link ModelPart} for the sleeve.
     * @param poseStack   The rendering {@link PoseStack}.
     * @param packedLight The {@link Integer} for the packed lighting for rendering.
     * @param consumer    The {@link VertexConsumer} for rendering.
     */
    private void renderHand(ModelPart dummyArm, ModelPart dummySleeve, PoseStack poseStack, int packedLight, VertexConsumer consumer) {
        dummyArm.visible = true;
        dummySleeve.visible = true;
        dummyArm.xRot = 0.0F;
        dummySleeve.xRot = 0.0F;
        dummyArm.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
        dummySleeve.render(poseStack, consumer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
