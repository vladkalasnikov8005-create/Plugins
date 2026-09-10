package org.examplee.plague.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.event.client.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.examplee.plague.PlagueMod;
import org.examplee.plague.darkness.item.DarkMapItem;
import org.examplee.plague.entity.GloomStalkerModel;
import org.examplee.plague.entity.GloomStalkerRenderer;
import org.examplee.plague.entity.ModEntities;
import org.examplee.plague.entity.PlagueDoctorModel;
import org.examplee.plague.entity.PlagueDoctorRenderer;
import org.examplee.plague.leper.item.LeperItems;
import org.examplee.plague.leper.item.UmbrellaItem;
import org.examplee.plague.network.ModNetworking;
import org.examplee.plague.pale.item.PaleMapItem;
import org.examplee.plague.registry.ModSounds;
import org.lwjgl.glfw.GLFW;

public class PlagueClient implements ClientModInitializer {
    public static KeyMapping MAP_KEY;
    public static int lastRadius = 6;
    private static boolean lastDark = false;
    private static long wardRequestedAt = 0;

    @Override
    public void onInitializeClient() {
        EntityModelLayerRegistry.registerModelLayer(PlagueDoctorModel.LAYER, PlagueDoctorModel::createBodyLayer);
        EntityModelLayerRegistry.registerModelLayer(GloomStalkerModel.LAYER, GloomStalkerModel::createBodyLayer);
        EntityRendererRegistry.register(ModEntities.PLAGUE_DOCTOR, PlagueDoctorRenderer::new);
        EntityRendererRegistry.register(ModEntities.GLOOM_STALKER, GloomStalkerRenderer::new);
        EntityRendererRegistry.register(ModEntities.SNEEZE, ThrownItemRenderer::new);
        EntityRendererRegistry.register(ModEntities.HOLY_WATER, ThrownItemRenderer::new);

        MAP_KEY = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.plague.map",
                com.mojang.blaze3d.platform.InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_M, "key.plague.category"));

        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.InfectionSync.TYPE,
                (p, ctx) -> ctx.client().execute(() -> onInfection(p)));
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.ZoneSync.TYPE,
                (p, ctx) -> ctx.client().execute(() -> {
                    ClientState.paleStage = p.paleStage();
                    ClientState.dark = p.dark();
                    ClientState.lantern = p.lantern();
                }));
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.MapData.TYPE,
                (p, ctx) -> ctx.client().execute(() -> {
                    if (Minecraft.getInstance().screen instanceof PlagueMapScreen s) s.onData(p);
                }));
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.WardInfo.TYPE,
                (p, ctx) -> ctx.client().execute(() -> {
                    ClientState.ward = p;
                    ClientState.wardAtMs = System.currentTimeMillis();
                }));
        ClientPlayNetworking.registerGlobalReceiver(ModNetworking.Cinematic.TYPE,
                (p, ctx) -> ctx.client().execute(() -> {
                    ClientState.cinematicUntilMs = p.startedAt() + 3500L;
                    playUi(ModSounds.CONVERSION, 1.0f);
                }));

        HudElementRegistry.addLast(PlagueMod.id("hud"), PlagueHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(PlagueClient::tick);

        // Umbrella open/closed model swap (client prediction of sun danger).
        registerUmbrella(LeperItems.UMBRELLA_TINY);
        registerUmbrella(LeperItems.UMBRELLA_WEAK);
        registerUmbrella(LeperItems.UMBRELLA_NORMAL);
        registerUmbrella(LeperItems.UMBRELLA_STRONG);
    }

    private static void registerUmbrella(Item item) {
        ItemProperties.register(item, PlagueMod.id("open"),
                (stack, level, entity, seed) -> umbrellaOpen(stack, entity) ? 1.0f : 0.0f);
    }

    private static boolean umbrellaOpen(ItemStack stack, LivingEntity entity) {
        if (!(entity instanceof Player p) || !(p.level() instanceof ClientLevel level)) return false;
        if (!(p.getOffhandItem().getItem() instanceof UmbrellaItem)) return false;
        if (level.dimension() != Level.OVERWORLD || level.isRaining() || level.isThundering()) return false;
        long dayTime = level.getDayTime() % 24000L;
        if (dayTime < 0 || dayTime > 12300) return false;
        BlockPos pos = p.blockPosition();
        return level.canSeeSky(pos.above()) && level.getBrightness(LightLayer.SKY, pos) >= 14;
    }

    private static void onInfection(ModNetworking.InfectionSync p) {
        boolean stageUp = p.stage() > ClientState.stage && p.stage() >= 1;
        boolean newlyLeper = p.leper() && !ClientState.leper;
        ClientState.leper = p.leper();
        ClientState.hits = p.hits();
        ClientState.stage = p.stage();
        ClientState.blessed = p.blessed();
        ClientState.rageLeftMs = p.rageLeftMs();
        if (newlyLeper) playUi(ModSounds.CONVERSION, 1.0f);
        else if (stageUp) playUi(ModSounds.PLAGUE_HIT, 1.0f);
    }

    private static void playUi(net.minecraft.sounds.SoundEvent sound, float volume) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.player.playSound(sound, volume, 1.0f);
    }

    public static void openMap(Minecraft mc) {
        if (mc.player == null) return;
        Item main = mc.player.getMainHandItem().getItem();
        Item off = mc.player.getOffhandItem().getItem();
        if (main instanceof DarkMapItem || off instanceof DarkMapItem) lastDark = true;
        else if (main instanceof PaleMapItem || off instanceof PaleMapItem) lastDark = false;
        mc.setScreen(new PlagueMapScreen(lastDark, lastRadius));
    }

    private static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null) return;
        while (MAP_KEY.consumeClick()) openMap(mc);

        // Conversion cinematic hook: slow camera pan + soul particles.
        if (ClientState.cinematicActive()) {
            mc.player.setYRot(mc.player.getYRot() + 2.0f);
            if (mc.level.random.nextInt(3) == 0) {
                double a = mc.level.random.nextDouble() * Math.PI * 2.0;
                mc.level.addParticle(ParticleTypes.SCULK_SOUL,
                        mc.player.getX() + Math.cos(a) * 1.2, mc.player.getY() + 1.0 + mc.level.random.nextDouble(),
                        mc.player.getZ() + Math.sin(a) * 1.2, 0, 0.05, 0);
            }
        }

        // Ward/lantern charge lookup for the targeted block (throttled).
        HitResult hit = mc.hitResult;
        if (hit instanceof BlockHitResult b) {
            BlockPos pos = b.getBlockPos();
            long now = System.currentTimeMillis();
            if (!pos.equals(ClientState.wardPos) || now - wardRequestedAt > 2000L) {
                ClientState.wardPos = pos;
                wardRequestedAt = now;
                ClientPlayNetworking.send(new ModNetworking.WardQuery(pos.getX(), pos.getY(), pos.getZ()));
            }
        }
    }
}
