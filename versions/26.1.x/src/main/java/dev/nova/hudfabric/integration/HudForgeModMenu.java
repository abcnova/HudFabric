package dev.nova.hudfabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.nova.hudfabric.gui.HudForgeConfigScreen;

public final class HudForgeModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new HudForgeConfigScreen(parent, true);
    }
}
