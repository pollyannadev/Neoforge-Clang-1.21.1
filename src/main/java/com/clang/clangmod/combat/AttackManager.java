package com.clang.clangmod.combat;

import com.clang.clangmod.effects.ClashEffects;
// This should allow to use the code from Effects here, yeah?

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AttackManager {

    private static final int CLASH_WINDOW = 5;

    private static final List<PendingAttack> PENDING =
            new ArrayList<>();

    // prevent replayed damage from being intercepted and sent to the PendingAttack again.
    private static final ThreadLocal<Boolean> REPLAYING =
            ThreadLocal.withInitial(() -> false);

    // Check for sword / axes (Is there a better way for compat? mods should use these tags, yeah?)
    private static boolean hasEligibleWeapon(LivingEntity entity) {
        ItemStack weapon = entity.getMainHandItem();

        return weapon.is(ItemTags.SWORDS)
                || weapon.is(ItemTags.AXES);
    }

    public static void onIncomingDamage(
            LivingIncomingDamageEvent event
    ) {
        if (REPLAYING.get()) return;

        LivingEntity target = event.getEntity();

        if (!(target.level() instanceof ServerLevel level)) {
            return;
        }

        DamageSource source = event.getContainer().getSource();

        // Identify attacker
        if (!(source.getEntity() instanceof LivingEntity attacker)) {
            return;
        }

        // Only direct melee attacks
        if (source.getDirectEntity() != attacker) {
            return;
        }

        if (attacker == target || !hasEligibleWeapon(attacker)) {
            return;
        }

        float damage = event.getContainer().getOriginalDamage();

        if (damage <= 0) return;

        long currentTick = level.getServer()
                .overworld()
                .getGameTime();

        // Search for a reversed attack.
        for (Iterator<PendingAttack> iterator =
             PENDING.iterator(); iterator.hasNext();) {

            PendingAttack previous = iterator.next();

            boolean reversed =
                    previous.attacker().getUUID().equals(target.getUUID())
                            && previous.target().getUUID().equals(attacker.getUUID());

            boolean sameLevel = previous.level() == level;

            boolean withinWindow =
                    currentTick - previous.tick() <= CLASH_WINDOW;

            if (reversed && sameLevel && withinWindow) {

                // Remove the first attack.
                iterator.remove();

                // Cancel the second attack.
                event.setCanceled(true);

                // Calculate the difference.
                float difference =
                        Math.abs(previous.damage() - damage);

                if (difference > 0.001F) {

                    if (previous.damage() > damage) {
                        // First attacker wins.
                        applyDamage(
                                previous.target(),
                                previous.source(),
                                difference
                        );

                    } else {
                        // Second attacker wins.
                        applyDamage(
                                target,
                                source,
                                difference
                        );
                    }
                }

                // Play the clash effects.
                ClashEffects.play(
                        level,
                        attacker,
                        target
                );

                return;
            }
        }

        // No counterattack found.
        // Temporarily prevent the damage.
        event.setCanceled(true);

        PENDING.add(
                new PendingAttack(
                        attacker,
                        target,
                        source,
                        damage,
                        currentTick,
                        level
                )
        );
    }

    private static void applyDamage(
            LivingEntity target,
            DamageSource source,
            float damage
    ) {
        if (!target.isAlive() || target.isRemoved()) {
            return;
        }

        REPLAYING.set(true);

        try {
            target.hurt(source, damage);
        } finally {
            REPLAYING.set(false);
        }
    }

    public static void tick(MinecraftServer server) {

        long currentTick = server.overworld().getGameTime();

        Iterator<PendingAttack> iterator =
                PENDING.iterator();

        while (iterator.hasNext()) {

            PendingAttack attack = iterator.next();

            if (currentTick - attack.tick() > CLASH_WINDOW) {

                iterator.remove();

                // No counterattack: apply the original damage.
                applyDamage(
                        attack.target(),
                        attack.source(),
                        attack.damage()
                );
            }
        }
    }
}
