package dev.shadowclient;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class ShadowEspClient implements ClientModInitializer {
    static boolean blockEsp = true;
    static boolean playerEsp = true;
    static boolean vanishDiagnostics = true;
    private static final int BLOCK_RADIUS = 12;
    private static final Set<BlockPos> BLOCKS_TO_DRAW = new HashSet<>();
    private static final Set<UUID> WARNED = new HashSet<>();

    private static KeyBinding toggleBlocks;
    private static KeyBinding togglePlayers;
    private static KeyBinding toggleVanish;
    private static KeyBinding openGui;

    @Override
    public void onInitializeClient() {
        toggleBlocks = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.block_esp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B,
                KeyBinding.Category.MISC));
        togglePlayers = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.player_esp", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_P,
                KeyBinding.Category.MISC));
        toggleVanish = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.vanish_diagnostics", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V,
                KeyBinding.Category.MISC));
        openGui = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.goonclient.open_gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
                KeyBinding.Category.MISC));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openGui.wasPressed()) {
                client.setScreen(new GoonClientScreen());
            }
            while (toggleBlocks.wasPressed()) {
                blockEsp = !blockEsp;
                message(client, "Block ESP: " + state(blockEsp));
            }
            while (togglePlayers.wasPressed()) {
                playerEsp = !playerEsp;
                message(client, "Player ESP: " + state(playerEsp));
            }
            while (toggleVanish.wasPressed()) {
                vanishDiagnostics = !vanishDiagnostics;
                WARNED.clear();
                message(client, "Vanish-Diagnose: " + state(vanishDiagnostics));
            }
            if (client.world != null && client.player != null && vanishDiagnostics) {
                checkLoadedPlayersAgainstTab(client);
            } else {
                WARNED.clear();
            }
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> renderEsp(context));
    }

    private static void renderEsp(net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext context) {
        MinecraftClient client = MinecraftClient.getInstance();
        World world = client.world;
        if (world == null || client.player == null || context.matrixStack() == null || context.consumers() == null) return;

        Vec3d cameraPos = context.camera().getPos();
        var matrices = context.matrixStack();
        VertexConsumer lines = context.consumers().getBuffer(RenderLayer.getLines());

        if (blockEsp) {
            BLOCKS_TO_DRAW.clear();
            BlockPos origin = client.player.getBlockPos();
            BlockPos.Mutable pos = new BlockPos.Mutable();
            for (int x = -BLOCK_RADIUS; x <= BLOCK_RADIUS; x++) {
                for (int y = -BLOCK_RADIUS; y <= BLOCK_RADIUS; y++) {
                    for (int z = -BLOCK_RADIUS; z <= BLOCK_RADIUS; z++) {
                        pos.set(origin.getX() + x, origin.getY() + y, origin.getZ() + z);
                        BlockState state = world.getBlockState(pos);
                        if (isTargetBlock(state.getBlock())) BLOCKS_TO_DRAW.add(pos.toImmutable());
                    }
                }
            }
            for (BlockPos blockPos : BLOCKS_TO_DRAW) {
                Box box = new Box(blockPos).offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                float[] color = blockColor(world.getBlockState(blockPos).getBlock());
                WorldRenderer.drawBox(matrices, lines, box, color[0], color[1], color[2], 0.95f);
            }
        }

        if (playerEsp) {
            for (PlayerEntity target : world.getPlayers()) {
                if (target == client.player || !target.isAlive()) continue;
                Box box = target.getBoundingBox().expand(0.05)
                        .offset(-cameraPos.x, -cameraPos.y, -cameraPos.z);
                WorldRenderer.drawBox(matrices, lines, box, 0.2f, 1.0f, 0.35f, 0.95f);
            }
        }
    }

    static void toggleBlockEsp() { blockEsp = !blockEsp; }
    static void togglePlayerEsp() { playerEsp = !playerEsp; }
    static void toggleVanishDiagnostics() { vanishDiagnostics = !vanishDiagnostics; WARNED.clear(); }
    static boolean isBlockEspEnabled() { return blockEsp; }
    static boolean isPlayerEspEnabled() { return playerEsp; }
    static boolean isVanishDiagnosticsEnabled() { return vanishDiagnostics; }

    private static boolean isTargetBlock(Block block) {
        return block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                || block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE
                || block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE
                || block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE
                || block == Blocks.ANCIENT_DEBRIS;
    }

    private static float[] blockColor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return new float[]{0.1f, 0.9f, 1.0f};
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return new float[]{0.1f, 1.0f, 0.2f};
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return new float[]{1.0f, 0.75f, 0.1f};
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return new float[]{0.85f, 0.85f, 0.85f};
        return new float[]{1.0f, 0.25f, 0.1f};
    }

    private static void checkLoadedPlayersAgainstTab(MinecraftClient client) {
        if (client.world == null || client.getNetworkHandler() == null) return;
        Set<UUID> listed = new HashSet<>();
        client.getNetworkHandler().getPlayerList().forEach(entry -> listed.add(entry.getProfile().getId()));
        for (PlayerEntity player : client.world.getPlayers()) {
            if (player == client.player) continue;
            UUID id = player.getUuid();
            // A loaded entity missing from the tab list is only a clue; plugins/mods may alter tab behavior.
            if (!listed.contains(id) && WARNED.add(id)) {
                message(client, "Hinweis: Geladener Spieler " + player.getName().getString()
                        + " fehlt in der Spielerliste. Das beweist keinen Vanish-Status.");
            } else if (listed.contains(id)) {
                WARNED.remove(id);
            }
        }
    }

    private static String state(boolean enabled) { return enabled ? "AN" : "AUS"; }

    private static void message(MinecraftClient client, String text) {
        if (client.player != null) client.player.sendMessage(Text.literal("[GoonClient] " + text), true);
    }
}
