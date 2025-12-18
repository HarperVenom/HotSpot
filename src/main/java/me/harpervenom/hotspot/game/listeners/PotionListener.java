package me.harpervenom.hotspot.game.listeners;

import me.harpervenom.hotspot.game.Game;
import me.harpervenom.hotspot.game.GameManager;
import me.harpervenom.hotspot.game.profile.GameProfile;
import org.apache.maven.model.Profile;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.AreaEffectCloudApplyEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.entity.PotionSplashEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

import static me.harpervenom.hotspot.HotSpot.plugin;

public class PotionListener implements Listener {

    private final GameManager gameManager;

    public PotionListener(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    private static final Set<PotionEffectType> POSITIVE_EFFECTS = Set.of(
            PotionEffectType.SPEED,
            PotionEffectType.HASTE,
            PotionEffectType.REGENERATION,
            PotionEffectType.STRENGTH,
            PotionEffectType.HEALTH_BOOST,
            PotionEffectType.ABSORPTION,
            PotionEffectType.FIRE_RESISTANCE,
            PotionEffectType.RESISTANCE,
            PotionEffectType.WATER_BREATHING,
            PotionEffectType.INVISIBILITY,
            PotionEffectType.NIGHT_VISION,
            PotionEffectType.SATURATION,
            PotionEffectType.SLOW_FALLING,
            PotionEffectType.LUCK,
            PotionEffectType.INSTANT_HEALTH,
            PotionEffectType.JUMP_BOOST
    );

    private static final Set<PotionEffectType> NEGATIVE_EFFECTS = Set.of(
            PotionEffectType.SLOWNESS,
            PotionEffectType.WEAKNESS,
            PotionEffectType.INSTANT_DAMAGE,
            PotionEffectType.POISON,
            PotionEffectType.UNLUCK,
            PotionEffectType.BLINDNESS,
            PotionEffectType.WITHER,
            PotionEffectType.HUNGER,
            PotionEffectType.MINING_FATIGUE
    );

    @EventHandler
    public void onPotionSplash(PotionSplashEvent e) {
        ThrownPotion potion = e.getPotion();

        if (!(potion.getShooter() instanceof Player thrower)) return;

        Game game = gameManager.getGame(thrower.getWorld());
        if (game == null) return;

        for (LivingEntity entity : e.getAffectedEntities()) {

            boolean sameTeam = game.getPlayerManager().areSameTeam(thrower, entity);

            double intensity = e.getIntensity(entity);

            // Cancel potion effects for this entity
            e.setIntensity(entity, 0);

            for (PotionEffect effect : potion.getEffects()) {
                PotionEffectType type = effect.getType();

                Player player = entity instanceof Player p ? p : null;
                boolean isProtected = false;
                if (player != null) {
                    GameProfile profile = game.getPlayerManager().getProfile(player);
                    isProtected = profile != null && profile.isProtected();
                }
                if (!effectShouldApply(type, sameTeam, isProtected)) continue;

                if (type.equals(PotionEffectType.INSTANT_HEALTH)) {
                    double amount = 4 * (effect.getAmplifier() + 1) * intensity;

                    AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
                    double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

                    double newHealth = Math.min(entity.getHealth() + amount, maxHealth);

                    entity.setHealth(newHealth);
                } else if (type.equals(PotionEffectType.INSTANT_DAMAGE)) {
                    double amount = 6 * (effect.getAmplifier() + 1) * intensity;
                    entity.damage(amount, thrower); // trigger damage event with thrower as damager
                    game.getDamageManager().assignLastDamager(entity, thrower);
                } else {
                    PotionEffect adjusted = new PotionEffect(
                            type,
                            (int) (effect.getDuration() * intensity),
                            effect.getAmplifier(),
                            false,
                            true,
                            effect.hasIcon()
                    );
                    entity.addPotionEffect(adjusted);

                    if (!sameTeam && NEGATIVE_EFFECTS.contains(type)) {
                        game.getDamageManager().assignLastDamager(entity, thrower);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPotionHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof ThrownPotion potion)) return;
        if (potion.getItem().getType() != Material.LINGERING_POTION) return;

        Game game = gameManager.getGame(potion.getWorld());
        if (game == null) return;

        PotionMeta meta = (PotionMeta) potion.getItem().getItemMeta();
        if (meta == null) return;

        Entity thrower = null;
        if (potion.getShooter() instanceof Entity entity) thrower = entity;

        Collection<PotionEffect> effects = potion.getEffects();

        // Immediately apply effects to nearby players
        Collection<Entity> immediateNearby = potion.getWorld().getNearbyEntities(potion.getLocation(), 3, 1, 3);
        for (Entity entity : immediateNearby) {
            if (entity instanceof LivingEntity livingEntity) {
                for (PotionEffect effect : effects) {

                    Player player = entity instanceof Player p ? p : null;
                    boolean isProtected = false;
                    if (player != null) {
                        GameProfile profile = game.getPlayerManager().getProfile(player);
                        isProtected = profile != null && profile.isProtected();
                    }
                    if (!effectShouldApply(effect.getType(), game.getPlayerManager().areSameTeam(thrower, entity), isProtected)) continue;

                    livingEntity.addPotionEffect(effect);
                }
            }
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Collection<Entity> nearby = potion.getWorld().getNearbyEntities(potion.getLocation(), 2, 2, 2);
            for (Entity entity : nearby) {
                if (entity instanceof AreaEffectCloud cloud) {
                    for (PotionEffect effect : potion.getEffects()) {
                        cloud.addCustomEffect(effect, true);
                    }
                    break;
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onLingeringPotionCloud(AreaEffectCloudApplyEvent e) {
        AreaEffectCloud cloud = e.getEntity();

        if (!(cloud.getSource() instanceof Player thrower)) return;

        Game game = gameManager.getGame(thrower.getWorld());
        if (game == null) return;

        List<LivingEntity> affectedEntities = e.getAffectedEntities();
        Iterator<LivingEntity> iterator = affectedEntities.iterator();

        while (iterator.hasNext()) {
            LivingEntity entity = iterator.next();

            boolean sameTeam = false;

            if (entity instanceof Player targetPlayer) {
                GameProfile targetProfile = game.getPlayerManager().getProfile(targetPlayer);
                if (targetProfile == null) {
                    iterator.remove();
                    continue;
                }
                sameTeam = game.getPlayerManager().areSameTeam(thrower, targetPlayer);
            }

            // Remove the entity from affected list to cancel default potion applying
            iterator.remove();

            for (PotionEffect effect : cloud.getCustomEffects()) {
                PotionEffectType type = effect.getType();

                Player player = entity instanceof Player p ? p : null;
                boolean isProtected = false;
                if (player != null) {
                    GameProfile profile = game.getPlayerManager().getProfile(player);
                    isProtected = profile != null && profile.isProtected();
                }
                if (!effectShouldApply(effect.getType(), game.getPlayerManager().areSameTeam(thrower, entity), isProtected)) continue;

                if (type.equals(PotionEffectType.INSTANT_HEALTH)) {
                    double amount = 4 * (effect.getAmplifier() + 1);

                    AttributeInstance maxHealthAttr = entity.getAttribute(Attribute.MAX_HEALTH);
                    double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;

                    double newHealth = Math.min(entity.getHealth() + amount, maxHealth);
                    entity.setHealth(newHealth);
                } else if (type.equals(PotionEffectType.INSTANT_DAMAGE)) {
                    double amount = 6 * (effect.getAmplifier() + 1);
                    entity.damage(amount, thrower); // trigger damage event with thrower as damager
                    game.getDamageManager().assignLastDamager(entity, thrower);
                } else {
                    PotionEffect adjusted = new PotionEffect(
                            type,
                            effect.getDuration(),
                            effect.getAmplifier(),
                            false,
                            true,
                            effect.hasIcon()
                    );
                    entity.addPotionEffect(adjusted);

                    if (!sameTeam && NEGATIVE_EFFECTS.contains(type)) {
                        game.getDamageManager().assignLastDamager(entity, thrower);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onSlownessFromArrow(EntityPotionEffectEvent e) {
        if (e.getNewEffect() == null) return;

        if (e.getNewEffect().getType() != PotionEffectType.SLOWNESS) return;
        if (!(e.getEntity() instanceof LivingEntity livingEntity)) return;

        slowEntity(livingEntity);
    }

    static void slowEntity(LivingEntity entity) {
        slowed.add(entity);
        entity.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(0);
    }

    static Set<LivingEntity> slowed = new HashSet<>();

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        p.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(
                p.getAttribute(Attribute.JUMP_STRENGTH).getDefaultValue());
    }

    static {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            Set<LivingEntity> slowedCopy = new HashSet<>(slowed);
            for (LivingEntity entity : slowedCopy) {
                if (!entity.hasPotionEffect(PotionEffectType.SLOWNESS)) {
                    slowed.remove(entity);
                    entity.getAttribute(Attribute.JUMP_STRENGTH).setBaseValue(
                            entity.getAttribute(Attribute.JUMP_STRENGTH).getDefaultValue());
                }
            }
        }, 0, 10);
    }

    private static boolean effectShouldApply(PotionEffectType type, boolean isAlly, boolean isProtected) {
        return ((POSITIVE_EFFECTS.contains(type) && isAlly)
                || (NEGATIVE_EFFECTS.contains(type) && !isAlly && !isProtected))
                || (!POSITIVE_EFFECTS.contains(type) && !NEGATIVE_EFFECTS.contains(type));
    }
}
