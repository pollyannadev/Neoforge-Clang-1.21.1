package com.clang.clangmod.combat;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerLevel;

// Record is the one to use here, right? Not class? I'm correct, yeah?
// To catch the attack and judge against it.  Living entity should disable any happenings with armor stands & the like?
public record PendingAttack(
        LivingEntity attacker,
        LivingEntity target,
        DamageSource source,
        float damage,
        long tick,
        ServerLevel level
) {}


public class PendingAttack {


}
