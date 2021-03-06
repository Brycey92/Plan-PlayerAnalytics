package com.djrapitops.plan.system.listeners.bukkit;

import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.store.mutators.formatting.Formatters;
import com.djrapitops.plan.system.cache.SessionCache;
import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.processing.processors.player.KillProcessor;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.utilities.Format;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.projectiles.ProjectileSource;

/**
 * Event Listener for EntityDeathEvents.
 *
 * @author Rsl1122
 */
public class DeathEventListener implements Listener {

    /**
     * Command use listener.
     *
     * @param event Fired event.
     */
    @SuppressWarnings("deprecation")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(EntityDeathEvent event) {
        long time = System.currentTimeMillis();
        LivingEntity dead = event.getEntity();

        if (dead instanceof Player) {
            // Process Death
            Processing.submitCritical(() -> SessionCache.getCachedSession(dead.getUniqueId()).ifPresent(Session::died));
        }

        try {
            EntityDamageEvent entityDamageEvent = dead.getLastDamageCause();
            if (!(entityDamageEvent instanceof EntityDamageByEntityEvent)) {
                return;
            }

            EntityDamageByEntityEvent entityDamageByEntityEvent = (EntityDamageByEntityEvent) entityDamageEvent;
            Entity killerEntity = entityDamageByEntityEvent.getDamager();

            handleKill(time, dead, killerEntity);
        } catch (Exception e) {
            Log.toLog(this.getClass(), e);
        }
    }

    private void handleKill(long time, LivingEntity dead, Entity killerEntity) {
        KillProcessor processor = null;
        if (killerEntity instanceof Player) {
            processor = handlePlayerKill(time, dead, (Player) killerEntity);
        } else if (killerEntity instanceof Tameable) {
            processor = handlePetKill(time, dead, (Tameable) killerEntity);
        } else if (killerEntity instanceof Projectile) {
            processor = handleProjectileKill(time, dead, (Projectile) killerEntity);
        }
        if (processor != null) {
            Processing.submit(processor);
        }
    }

    private KillProcessor handlePlayerKill(long time, LivingEntity dead, Player killer) {
        Material itemInHand;
        try {
            itemInHand = killer.getInventory().getItemInMainHand().getType();
        } catch (NoSuchMethodError e) {
            try {
                itemInHand = killer.getInventory().getItemInHand().getType(); // Support for non dual wielding versions.
            } catch (Exception | NoSuchMethodError | NoSuchFieldError e2) {
                itemInHand = Material.AIR;
            }
        }

        return new KillProcessor(killer.getUniqueId(), time, dead, Formatters.itemName().apply(itemInHand.name()));
    }

    private KillProcessor handlePetKill(long time, LivingEntity dead, Tameable tameable) {
        if (!tameable.isTamed()) {
            return null;
        }

        AnimalTamer owner = tameable.getOwner();
        if (!(owner instanceof Player)) {
            return null;
        }

        String name;
        try {
            name = tameable.getType().name();
        } catch (NoSuchMethodError oldVersionNoTypesError) {
            // getType introduced in 1.9
            name = tameable.getClass().getSimpleName();
        }

        return new KillProcessor(owner.getUniqueId(), time, dead,
                new Format(name).removeNumbers().removeSymbols().capitalize().toString()
        );
    }

    private KillProcessor handleProjectileKill(long time, LivingEntity dead, Projectile projectile) {
        ProjectileSource source = projectile.getShooter();
        if (!(source instanceof Player)) {
            return null;
        }

        Player player = (Player) source;

        return new KillProcessor(player.getUniqueId(), time, dead,
                new Format(projectile.getType().name()).capitalize().toString()
        );
    }
}

