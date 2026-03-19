package com.aetherteam.aether.client.renderer.entity;

import com.aetherteam.aether.entity.monster.AbstractWhirlwind;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;

public class WhirlwindRenderer extends EntityRenderer<AbstractWhirlwind, LivingEntityRenderState> {
    public WhirlwindRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
