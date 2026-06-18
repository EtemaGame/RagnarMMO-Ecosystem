package com.etema.ragnarmmo.combat.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = com.etema.ragnarmmo.combat.RagnarMMOCombat.MOD_ID, value = Dist.CLIENT,
        bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatPopoffHandler {
    private static final long LIFETIME_MS = 900L;
    private static final float BASE_SCALE = -0.025F * 0.95F;
    private static final int TEXT_BACKPLATE = 0x660B1118;
    private static final Map<Integer, List<Popoff>> POPOFFS = new ConcurrentHashMap<>();

    private CombatPopoffHandler() {
    }

    public static void addPopoff(int entityId, String text, int color) {
        List<Popoff> list = POPOFFS.computeIfAbsent(entityId, k -> new ArrayList<>());
        int slot = list.size() % 5;
        float xDrift = (slot - 2) * 3.0F;
        list.add(new Popoff(text, color, System.currentTimeMillis(), xDrift));
    }

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<LivingEntity, ?> event) {
        Minecraft mc = Minecraft.getInstance();
        LivingEntity entity = event.getEntity();
        List<Popoff> entityPopoffs = POPOFFS.get(entity.getId());
        if (entityPopoffs == null || entityPopoffs.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(0.0D, Math.max(0.75D, entity.getBbHeight() * 0.72D), 0.0D);
        poseStack.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        float scale = BASE_SCALE;
        poseStack.scale(scale, scale, -scale);

        Font font = mc.font;
        int stackIndex = 0;
        Iterator<Popoff> it = entityPopoffs.iterator();
        while (it.hasNext()) {
            Popoff popoff = it.next();
            long age = now - popoff.startTime;
            if (age > LIFETIME_MS) {
                it.remove();
                continue;
            }

            float progress = (float) age / (float) LIFETIME_MS;
            float eased = 1.0F - (float) Math.pow(1.0F - progress, 3.0D);
            float yOffset = -eased * 12.0F - stackIndex * 5.0F;
            float xOffset = popoff.xDrift * (0.45F + progress * 0.5F);
            float popScale = progress < 0.12F ? 0.72F + (progress / 0.12F) * 0.28F : 1.0F;
            int alpha = computeAlpha(progress);
            int color = withAlpha(popoff.color, alpha);
            int backplate = withAlpha(TEXT_BACKPLATE, Math.min(120, alpha / 2));

            poseStack.pushPose();
            poseStack.translate(xOffset, yOffset, 0.0D);
            poseStack.scale(popScale, popScale, popScale);

            float x = -font.width(popoff.text) / 2.0F;
            font.drawInBatch(popoff.text, x, 0.0F, color, false, poseStack.last().pose(),
                    event.getMultiBufferSource(), Font.DisplayMode.SEE_THROUGH, backplate, event.getPackedLight());

            poseStack.popPose();
            stackIndex++;
        }

        poseStack.popPose();
    }

    private static int computeAlpha(float progress) {
        if (progress < 0.68F) {
            return 255;
        }
        return (int) (255.0F * Math.max(0.0F, 1.0F - ((progress - 0.68F) / 0.32F)));
    }

    private static int withAlpha(int rgb, int alpha) {
        return (rgb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
    }

    private record Popoff(String text, int color, long startTime, float xDrift) {
    }
}
