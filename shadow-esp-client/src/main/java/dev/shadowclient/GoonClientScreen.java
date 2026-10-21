package dev.shadowclient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

/** Simple in-game settings menu for GoonClient. */
public final class GoonClientScreen extends Screen {
    public GoonClientScreen() {
        super(Text.literal("GoonClient"));
    }

    @Override
    protected void init() {
        int buttonWidth = 220;
        int buttonHeight = 22;
        int left = (this.width - buttonWidth) / 2;
        int top = this.height / 2 - 48;

        addDrawableChild(ButtonWidget.builder(moduleLabel("Block ESP", GoonClientState.BLOCK), button -> {
            ShadowEspClient.toggleBlockEsp();
            refresh();
        }).dimensions(left, top, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("Player ESP", GoonClientState.PLAYER), button -> {
            ShadowEspClient.togglePlayerEsp();
            refresh();
        }).dimensions(left, top + 28, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(moduleLabel("Vanish-Diagnose", GoonClientState.VANISH), button -> {
            ShadowEspClient.toggleVanishDiagnostics();
            refresh();
        }).dimensions(left, top + 56, buttonWidth, buttonHeight).build());

        addDrawableChild(ButtonWidget.builder(Text.literal("Schließen"), button -> close())
                .dimensions(left, top + 92, buttonWidth, buttonHeight).build());
    }

    private Text moduleLabel(String name, GoonClientState state) {
        boolean enabled = switch (state) {
            case BLOCK -> ShadowEspClient.isBlockEspEnabled();
            case PLAYER -> ShadowEspClient.isPlayerEspEnabled();
            case VANISH -> ShadowEspClient.isVanishDiagnosticsEnabled();
        };
        return Text.literal(name + ": " + (enabled ? "AN" : "AUS"));
    }

    private void refresh() {
        if (this.client != null) this.client.setScreen(new GoonClientScreen());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, "GoonClient", this.width / 2, this.height / 2 - 82, 0xFFB56BFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Fabric  |  Minecraft 1.21.5", this.width / 2, this.height / 2 - 66, 0xFFCCCCCC);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    private enum GoonClientState { BLOCK, PLAYER, VANISH }
}
