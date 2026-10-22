package dev.nova.hudfabric.integration;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.nova.hudfabric.gui.HudFabricConfigScreen;

public final class HudFabricModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new HudFabricConfigScreen(parent);
    }
}
