package com.clang.clangmod;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(clangmod.MODID)

package com.clang.clangmod;

import com.clang.clangmod.combat.AttackManager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

@Mod(ClangMod.MOD_ID)
public class ClangMod {

    public static final String MOD_ID = "Clangmod";

    public ClangMod(IEventBus modEventBus) {

        NeoForge.EVENT_BUS.addListener(
                AttackManager::onIncomingDamage
        );

        NeoForge.EVENT_BUS.addListener(
                Clangmod::onServerTick
        );
    }

    private static void onServerTick(ServerTickEvent.Post event) {
        AttackManager.tick(event.getServer());
    }
}



