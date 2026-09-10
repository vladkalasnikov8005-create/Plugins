package org.examplee.plague.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import org.examplee.plague.PlagueConfig;
import org.examplee.plague.darkness.DarkBlockItem;
import org.examplee.plague.darkness.DarknessWorldData;
import org.examplee.plague.darkness.item.DarkItems;
import org.examplee.plague.leper.LeperData;
import org.examplee.plague.leper.LeperLogic;
import org.examplee.plague.leper.SneezeProjectile;
import org.examplee.plague.leper.item.LeperItems;
import org.examplee.plague.leper.item.UmbrellaItem;
import org.examplee.plague.pale.GreatWardBlock;
import org.examplee.plague.pale.PaleTick;
import org.examplee.plague.pale.PaleWorldData;
import org.examplee.plague.pale.item.PaleItems;
import org.examplee.plague.registry.ModBlocks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * /plague — parity with Bukkit commands:
 * leper add/remove/bless/unbless/sneeze/status/give; purify → vaccine give;
 * pale on/off/speed/info/give; dark on/off/speed/growth/infectall/info/give.
 */
public class PlagueCommands {
    private static final Map<UUID, Long> SNEEZE_COMMAND_CD = new HashMap<>();

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, access, env) -> {
            dispatcher.register(Commands.literal("plague")
                    .then(leper())
                    .then(pale())
                    .then(dark()));
            // /purify convenience alias (plugin parity).
            dispatcher.register(Commands.literal("purify").requires(s -> s.hasPermission(2))
                    .executes(ctx -> {
                        ServerPlayer p = ctx.getSource().getPlayerOrException();
                        giveOrDrop(p, new ItemStack(LeperItems.VACCINE));
                        ctx.getSource().sendSuccess(() -> Component.literal("You received a vaccine."), false);
                        return 1;
                    })
                    .then(Commands.argument("target", EntityArgument.player())
                            .executes(ctx -> {
                                ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                                giveOrDrop(t, new ItemStack(LeperItems.VACCINE));
                                ctx.getSource().sendSuccess(() -> Component.literal("Gave vaccine to " + t.getScoreboardName()), false);
                                return 1;
                            })));
        });
    }

    private static LiteralArgumentBuilder<CommandSourceStack> leper() {
        return Commands.literal("leper")
                .then(Commands.literal("add").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                            LeperLogic.setLeper(t, true);
                            ctx.getSource().sendSuccess(() -> Component.literal(t.getScoreboardName() + " is now a leper."), true);
                            return 1;
                        })))
                .then(Commands.literal("remove").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                            LeperLogic.cure(t);
                            ctx.getSource().sendSuccess(() -> Component.literal(t.getScoreboardName() + " cured."), true);
                            return 1;
                        })))
                .then(Commands.literal("bless").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                            LeperData.set(t, LeperData.get(t).withBlessed(true));
                            ctx.getSource().sendSuccess(() -> Component.literal(t.getScoreboardName() + " blessed."), true);
                            return 1;
                        })))
                .then(Commands.literal("unbless").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx -> {
                            ServerPlayer t = EntityArgument.getPlayer(ctx, "target");
                            LeperData.set(t, LeperData.get(t).withBlessed(false));
                            ctx.getSource().sendSuccess(() -> Component.literal(t.getScoreboardName() + " unblessed."), true);
                            return 1;
                        })))
                .then(Commands.literal("sneeze").requires(s -> s.hasPermission(0))
                        .executes(ctx -> sneeze(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player())
                                .requires(s -> s.hasPermission(2))
                                .executes(ctx -> sneeze(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("status")
                        .executes(ctx -> status(ctx.getSource(), ctx.getSource().getPlayerOrException()))
                        .then(Commands.argument("target", EntityArgument.player()).executes(ctx ->
                                status(ctx.getSource(), EntityArgument.getPlayer(ctx, "target")))))
                .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.word())
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> giveLeper(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "item"),
                                                EntityArgument.getPlayer(ctx, "target"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveLeper(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "item"),
                                                        EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "count")))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> pale() {
        return Commands.literal("pale")
                .then(Commands.literal("on").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    PlagueConfig.INSTANCE.pale.running = true;
                    PlagueConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal("Pale spread enabled."), true);
                    return 1;
                }))
                .then(Commands.literal("off").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    PlagueConfig.INSTANCE.pale.running = false;
                    PlagueConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal("Pale spread disabled."), true);
                    return 1;
                }))
                .then(Commands.literal("speed").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 10000)).executes(ctx -> {
                            PlagueConfig.INSTANCE.pale.speedPerChunk = IntegerArgumentType.getInteger(ctx, "value");
                            PlagueConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.literal("Pale speed set."), true);
                            return 1;
                        })))
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    PaleWorldData d = PaleWorldData.get(p.serverLevel());
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Pale: running=" + PlagueConfig.INSTANCE.pale.running
                                    + " speed=" + PlagueConfig.INSTANCE.pale.speedPerChunk
                                    + " rate=" + String.format("%.4f", PlagueConfig.effectiveRatePerSecPerChunk())
                                    + " sources=" + d.sourceCount() + " wards=" + d.wardCount()
                                    + " greatwards=" + d.greatWards().size() + " biomes=" + d.biomeCellCount()
                                    + " attempts=" + PaleTick.totalAttempts + " prot=" + PaleTick.totalProtected
                                    + " cleansed=" + PaleTick.totalCleansed + " infected/min=" + PaleTick.infectedLastMinute()), false);
                    return 1;
                }))
                .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.word())
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> givePale(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "item"),
                                                EntityArgument.getPlayer(ctx, "target"), 1, 0))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> givePale(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "item"),
                                                        EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "count"), 0))
                                                .then(Commands.argument("extra", IntegerArgumentType.integer(0, 100000))
                                                        .executes(ctx -> givePale(ctx.getSource(),
                                                                StringArgumentType.getString(ctx, "item"),
                                                                EntityArgument.getPlayer(ctx, "target"),
                                                                IntegerArgumentType.getInteger(ctx, "count"),
                                                                IntegerArgumentType.getInteger(ctx, "extra"))))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> dark() {
        return Commands.literal("dark")
                .then(Commands.literal("on").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    PlagueConfig.INSTANCE.dark.enabled = true;
                    PlagueConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal("Darkness enabled."), true);
                    return 1;
                }))
                .then(Commands.literal("off").requires(s -> s.hasPermission(2)).executes(ctx -> {
                    PlagueConfig.INSTANCE.dark.enabled = false;
                    PlagueConfig.save();
                    ctx.getSource().sendSuccess(() -> Component.literal("Darkness disabled."), true);
                    return 1;
                }))
                .then(Commands.literal("speed").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 10000)).executes(ctx -> {
                            PlagueConfig.INSTANCE.dark.infectSpeed = IntegerArgumentType.getInteger(ctx, "value");
                            PlagueConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.literal("Darkness speed set."), true);
                            return 1;
                        })))
                .then(Commands.literal("growth").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("value", IntegerArgumentType.integer(0, 10000)).executes(ctx -> {
                            PlagueConfig.INSTANCE.dark.growthSpeed = IntegerArgumentType.getInteger(ctx, "value");
                            PlagueConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.literal("Darkness growth set."), true);
                            return 1;
                        })))
                .then(Commands.literal("infectall").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("value", BoolArgumentType.bool()).executes(ctx -> {
                            boolean v = BoolArgumentType.getBool(ctx, "value");
                            PlagueConfig.INSTANCE.dark.infectAll = v;
                            PlagueConfig.save();
                            ctx.getSource().sendSuccess(() -> Component.literal("infect-all=" + v), true);
                            return 1;
                        })))
                .then(Commands.literal("info").executes(ctx -> {
                    ServerPlayer p = ctx.getSource().getPlayerOrException();
                    ctx.getSource().sendSuccess(() -> Component.literal(
                            "Dark: enabled=" + PlagueConfig.INSTANCE.dark.enabled
                                    + " speed=" + PlagueConfig.INSTANCE.dark.infectSpeed
                                    + " growth=" + PlagueConfig.INSTANCE.dark.growthSpeed
                                    + " infectAll=" + PlagueConfig.INSTANCE.dark.infectAll
                                    + " total=" + DarknessWorldData.get(p.serverLevel()).size()), false);
                    return 1;
                }))
                .then(Commands.literal("give").requires(s -> s.hasPermission(2))
                        .then(Commands.argument("item", StringArgumentType.word())
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(ctx -> giveDark(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "item"),
                                                EntityArgument.getPlayer(ctx, "target"), 1))
                                        .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                                                .executes(ctx -> giveDark(ctx.getSource(),
                                                        StringArgumentType.getString(ctx, "item"),
                                                        EntityArgument.getPlayer(ctx, "target"),
                                                        IntegerArgumentType.getInteger(ctx, "count")))))));
    }

    // ---- helpers ----

    private static int sneeze(CommandSourceStack src, ServerPlayer t) {
        LeperData d = LeperData.get(t);
        if (!d.leper()) {
            src.sendFailure(Component.literal(t.getScoreboardName() + " is not a leper."));
            return 0;
        }
        long now = System.currentTimeMillis();
        Long cd = SNEEZE_COMMAND_CD.get(t.getUUID());
        if (cd != null && now < cd) {
            src.sendFailure(Component.literal("Sneeze on cooldown."));
            return 0;
        }
        SNEEZE_COMMAND_CD.put(t.getUUID(), now + 5000L);
        SneezeProjectile.spawn(t);
        src.sendSuccess(() -> Component.literal(t.getScoreboardName() + " sneezed."), false);
        return 1;
    }

    private static int status(CommandSourceStack src, ServerPlayer t) {
        LeperData d = LeperData.get(t);
        ItemStack off = t.getOffhandItem();
        int umb = off.getItem() instanceof UmbrellaItem ? UmbrellaItem.getRemaining(off) : -1;
        src.sendSuccess(() -> Component.literal(t.getScoreboardName()
                + ": leper=" + d.leper() + " stage=" + d.stage() + " hits=" + d.hits()
                + " blessed=" + d.blessed()
                + " rage=" + (d.rageUntil() > System.currentTimeMillis())
                + " umbrella=" + (umb < 0 ? "-" : umb + "s")), false);
        return 1;
    }

    private static int giveLeper(CommandSourceStack src, String item, ServerPlayer t, int count) {
        ItemStack stack = ItemStack.EMPTY;
        switch (item.toLowerCase()) {
            case "stick" -> stack = new ItemStack(LeperItems.PLAGUE_STICK, count);
            case "bomb" -> stack = new ItemStack(LeperItems.PLAGUE_BOMB, count);
            case "vaccine" -> stack = new ItemStack(LeperItems.VACCINE, count);
            case "blood" -> stack = new ItemStack(LeperItems.LEPER_BLOOD, count);
            case "thickblood", "thick", "thick_blood" -> stack = new ItemStack(LeperItems.THICK_BLOOD, count);
            case "sterileblood", "sterile", "sterile_blood" -> stack = new ItemStack(LeperItems.STERILE_BLOOD, count);
            case "knife" -> stack = new ItemStack(LeperItems.SACRIFICIAL_KNIFE, count);
            case "umbrella_tiny" -> stack = new ItemStack(LeperItems.UMBRELLA_TINY, count);
            case "umbrella_weak" -> stack = new ItemStack(LeperItems.UMBRELLA_WEAK, count);
            case "umbrella_normal" -> stack = new ItemStack(LeperItems.UMBRELLA_NORMAL, count);
            case "umbrella_strong" -> stack = new ItemStack(LeperItems.UMBRELLA_STRONG, count);
            case "lantern" -> stack = new ItemStack(ModBlocks.QUARANTINE_LANTERN, count);
            default -> {
                src.sendFailure(Component.literal("Unknown leper item: " + item));
                return 0;
            }
        }
        giveOrDrop(t, stack);
        src.sendSuccess(() -> Component.literal("Gave " + item + " x" + count + " to " + t.getScoreboardName()), false);
        return 1;
    }

    private static int givePale(CommandSourceStack src, String item, ServerPlayer t, int count, int extra) {
        ItemStack stack = ItemStack.EMPTY;
        switch (item.toLowerCase()) {
            case "salt" -> stack = new ItemStack(PaleItems.SALT, count);
            case "holywater", "holy", "holy_water" -> stack = new ItemStack(PaleItems.HOLY_WATER, count);
            case "ward" -> stack = new ItemStack(ModBlocks.WARD_BLOCK, count);
            case "greatward", "great", "great_ward" -> {
                // Extra = charge in minutes; default = full charge.
                int sec = extra > 0 ? extra * 60 : PlagueConfig.INSTANCE.pale.greatWardChargeMaxSec;
                if (count > 1) {
                    // Charged great wards do not stack; give them one by one.
                    for (int i = 0; i < count; i++) giveOrDrop(t, GreatWardBlock.chargedStack(sec));
                    final int c = count;
                    src.sendSuccess(() -> Component.literal("Gave greatward x" + c + " to " + t.getScoreboardName()), false);
                    return 1;
                }
                stack = GreatWardBlock.chargedStack(sec);
            }
            case "flint" -> stack = new ItemStack(PaleItems.PURIFIER_FLINT, count);
            case "purge" -> stack = new ItemStack(PaleItems.PURGE_WAND, count);
            case "map" -> stack = new ItemStack(PaleItems.PALE_MAP, count);
            case "wand", "infect", "infectwand" -> stack = new ItemStack(PaleItems.INFECT_WAND, count);
            default -> {
                src.sendFailure(Component.literal("Unknown pale item: " + item));
                return 0;
            }
        }
        giveOrDrop(t, stack);
        src.sendSuccess(() -> Component.literal("Gave " + item + " x" + count + " to " + t.getScoreboardName()), false);
        return 1;
    }

    private static int giveDark(CommandSourceStack src, String item, ServerPlayer t, int count) {
        ItemStack stack = ItemStack.EMPTY;
        switch (item.toLowerCase()) {
            case "darkblock", "dark", "block" -> {
                stack = new ItemStack(ModBlocks.DARKNESS_BLOCK, count);
                DarkBlockItem.setOwner(stack, t.getUUID().toString());
            }
            case "darkmap", "map" -> stack = new ItemStack(DarkItems.DARK_MAP, count);
            case "darkwand", "wand", "light", "lightwand" -> stack = new ItemStack(DarkItems.DARK_PURGE_WAND, count);
            default -> {
                src.sendFailure(Component.literal("Unknown dark item: " + item));
                return 0;
            }
        }
        giveOrDrop(t, stack);
        src.sendSuccess(() -> Component.literal("Gave " + item + " x" + count + " to " + t.getScoreboardName()), false);
        return 1;
    }

    private static void giveOrDrop(ServerPlayer p, ItemStack stack) {
        if (!p.getInventory().add(stack)) {
            ItemEntity drop = p.drop(stack, false);
            if (drop != null) drop.setNoPickUpDelay();
        }
    }
}
