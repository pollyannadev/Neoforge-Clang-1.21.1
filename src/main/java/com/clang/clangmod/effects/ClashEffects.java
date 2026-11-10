package com.clang.clangmod.effects;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;

public class ClashEffects {

    // Am I okay to do "a & b"? Should I use attacker & target?
    public static void play(
            ServerLevel level,
            LivingEntity a,
            LivingEntity b
    ) {
        double x = (a.getX() + b.getX()) / 2.0;
        double y = (a.getY() + b.getY()) / 2.0 + 1.0;
        double z = (a.getZ() + b.getZ()) / 2.0;

        // noise... change this.! Anvil sound, maybe? Something more metallic.
        level.playSound(
                null,
                x, y, z,
                SoundEvents.SHIELD_BLOCK,
                SoundSource.PLAYERS,
                1.0F,
                1.15F
        );

        // sparks
        level.sendParticles(
                ParticleTypes.CRIT,
                x, y, z,
                11,
                0.25, 0.25, 0.25,
                0.15
        );
    }
}

