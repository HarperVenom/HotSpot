package me.harpervenom.hotspot.game.vault.loot;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.Consumable;
import io.papermc.paper.datacomponent.item.consumable.ItemUseAnimation;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static me.harpervenom.hotspot.HotSpot.plugin;
import static me.harpervenom.hotspot.utils.Utils.*;

public class CustomItems {

    private final static String breakableKeyword = "breakable";

    public static ItemStack blockBomb;
    public static String blockBombId = "blockBomb";
    public static Material blockBombMaterial = Material.CLAY;

    public static ItemStack vacuumBomb;
    public static String vacuumBombId = "vacuumBomb";

    public static ItemStack thunderRelic;
    public final static int thunderDuration = 45;

    public static ItemStack pillarRelic;

    public static ItemStack reflectionRelic;

    public static ItemStack rocket;

    public static ItemStack tnt;
    public static String tntId = "tnt";

    public static ItemStack survivorJacket;
    public static String survivorJacketId = "survivorJacket";

    public static ItemStack sunPlate;
    public static String sunPlateId = "sunPlate";
    public static int sunPlateCooldown = 20;
    public static double sunPlateAbsorption = 10;
    public static int sunPlateDuration = 3;

    public static ItemStack explosionPlate;
    public static String explosionPlateId = "explosionPlate";

    public static ItemStack chainPlate;
    public static String chainPlateId = "chainPlate";
    public static int chainPlateCooldown = 10;
    public static int chainPlateDuration = 4;

    public static ItemStack ironPlate;
    public static String ironPlateId = "ironPlate";
    public static int ironPlateCooldown = 10;
    public static int ironPlateDuration = 6;
    public static int ironPlateDurationNegative = 5;

    public static ItemStack diamondPlate;
    public static String diamondPlateId = "diamondPlate";
    public static int diamondPlateCooldown = 10;
    public static int diamondPlateDuration = 4;

    public static ItemStack tankPlate;
    public static String tankPlateId = "tankPlate";
    public static double damageReduction = 0.6;

    public static ItemStack horseEgg;
    public static String horseEggId = "horseEgg";

    public static ItemStack camelEgg;
    public static String camelEggId = "camelEgg";

    public static ItemStack shield;

    public static ItemStack turboShovel;

    public static void createCustomItems() {
        blockBomb = new ItemStack(Material.EGG);
        ItemMeta blockBombMeta = blockBomb.getItemMeta();

        if (blockBombMeta != null) {
            blockBombMeta.displayName(text("Блочная Бомба", TextColor.color(162, 165, 179)));
            blockBomb.setItemMeta(blockBombMeta);
        }
        // id hard repeated in the listener
        setItemId(blockBomb, blockBombId);


        vacuumBomb = new ItemStack(Material.SNOWBALL);
        ItemMeta vacuumBombMeta = vacuumBomb.getItemMeta();

        if (vacuumBombMeta != null) {
            vacuumBombMeta.displayName(text("Вакуумная Бомба", TextColor.color(144, 128, 255)));
            vacuumBomb.setItemMeta(vacuumBombMeta);
        }
        // id hard repeated in the listener
        setItemId(vacuumBomb, vacuumBombId);

        // Relics
        thunderRelic = createItemStack(Material.LIGHT_BLUE_DYE, text("Шторм", TextColor.color(59, 157, 255)), null);
        addLoreLine(thunderRelic, text("Призывает грозовую погоду на " + thunderDuration + " секунд"));
        makeConsumable(thunderRelic);

        pillarRelic = createItemStack(Material.BROWN_DYE, text("Вознесение", TextColor.color(204, 134, 63)), null);
        addLoreLine(pillarRelic, text("Поднимает под тобой столб грязи"));
        makeConsumable(pillarRelic);

        reflectionRelic = createItemStack(Material.PURPLE_DYE, text("Отражение", TextColor.color(
                187, 51, 255
        )), null);
        addLoreLine(reflectionRelic, text("Откидывает игрока который наносит тебе урон,"));
        addLoreLine(reflectionRelic, text("если держать в руках"));
//        makeConsumable(reflectionRelic);

        rocket = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta fireworkMeta = (FireworkMeta) rocket.getItemMeta();
        fireworkMeta.setPower(3);
        FireworkEffect effect = FireworkEffect.builder()
                .with(FireworkEffect.Type.BALL_LARGE)  // Burst type will simulate a spherical explosion
                .withColor(Color.RED, Color.YELLOW, Color.ORANGE)
                .flicker(true) // Enable flickering for effect
                .trail(true) // Enable trail for effect
                .build();
        fireworkMeta.displayName(text("Ракета", NamedTextColor.RED));
        fireworkMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        fireworkMeta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        fireworkMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        fireworkMeta.addEffect(effect);
        rocket.setItemMeta(fireworkMeta);

        tnt = createItemStack(Material.TNT_MINECART, text("ТNT", NamedTextColor.RED), null);
        setItemId(tnt, tntId);
        makeConsumable(tnt);
        tnt.setData(DataComponentTypes.MAX_STACK_SIZE, 3);

        survivorJacket = new ItemStack(Material.LEATHER_CHESTPLATE);
        setItemId(survivorJacket, survivorJacketId);
        setCustomName(survivorJacket, text("Куртка Выжившего", NamedTextColor.RED));
        addLoreLine(survivorJacket, text("+3❤", NamedTextColor.RED));
        addLoreLine(survivorJacket, text("Регенерация I"));
        LeatherArmorMeta jacketMeta = (LeatherArmorMeta) survivorJacket.getItemMeta();
        if (jacketMeta != null) {
            jacketMeta.setColor(Color.fromARGB(255, 43, 22, 14));

            addAttributeModifier(jacketMeta, "max_health", Attribute.MAX_HEALTH, 6.0,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            addAttributeModifier(jacketMeta, "armor", Attribute.ARMOR, 3.0,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);

            survivorJacket.setItemMeta(jacketMeta);
        }
        applyArmorTrim(survivorJacket, TrimPattern.RIB, TrimMaterial.REDSTONE);
        hideArmorTrim(survivorJacket);

        sunPlate = new ItemStack(Material.GOLDEN_CHESTPLATE);
        setItemId(sunPlate, sunPlateId);
        setCustomName(sunPlate, text("Солнечный Нагрудник", NamedTextColor.YELLOW));
        addLoreLine(sunPlate, text("Получая урон: ").append(text("+5❤", NamedTextColor.YELLOW))
                .append(text(" на " + sunPlateDuration + " сек.")));
        addLoreLine(sunPlate, text("Перезарядка: " + sunPlateCooldown + " сек."));
        applyArmorTrim(sunPlate, TrimPattern.DUNE, TrimMaterial.GOLD);
        hideArmorTrim(sunPlate);

        explosionPlate = new ItemStack(Material.COPPER_CHESTPLATE);
        setItemId(explosionPlate, explosionPlateId);
        setCustomName(explosionPlate, text("Взрывной Нагрудник", TextColor.color(255, 85, 0)));
        addLoreLine(explosionPlate, text("Взрывается при смерти"));
        addLoreLine(explosionPlate, text("Получая урон сила взрыва увеличивается"));
        addLoreLine(explosionPlate, text("Заряд: 2", NamedTextColor.RED));
        applyArmorTrim(explosionPlate, TrimPattern.FLOW, TrimMaterial.RESIN);
        hideArmorTrim(explosionPlate);

        chainPlate = new ItemStack(Material.CHAINMAIL_CHESTPLATE);
        setItemId(chainPlate, chainPlateId);
        setCustomName(chainPlate, text("Кольчуга", TextColor.color(255, 149, 0)));
        addLoreLine(chainPlate, text("Поджигает атакующего на " + chainPlateDuration + " сек."));
        addLoreLine(chainPlate, text("Перезарядка: " + chainPlateCooldown + " сек."));
        applyArmorTrim(chainPlate, TrimPattern.WARD, TrimMaterial.COPPER);
        hideArmorTrim(chainPlate);

        ironPlate = new ItemStack(Material.IRON_CHESTPLATE);
        setItemId(ironPlate, ironPlateId);
        setCustomName(ironPlate, text("Железный Нагрудник", TextColor.color(163, 227, 255)));
        addLoreLine(ironPlate, text("Получая урон от игрока: Спешка II на " + ironPlateDuration + " сек."));
        addLoreLine(ironPlate, text("Врагу: Утомление I на " + ironPlateDurationNegative + " сек."));
        addLoreLine(ironPlate, text("Перезарядка: " + ironPlateCooldown + " сек."));
        applyArmorTrim(ironPlate, TrimPattern.BOLT, TrimMaterial.NETHERITE);
        hideArmorTrim(ironPlate);

        diamondPlate = new ItemStack(Material.DIAMOND_CHESTPLATE);
        setItemId(diamondPlate, diamondPlateId);
        setCustomName(diamondPlate, text("Алмазный Нагрудник", TextColor.color(0, 255, 200)));
        addLoreLine(diamondPlate, text("Получая урон от игрока: Скорость III на " + diamondPlateDuration + " сек."));
        addLoreLine(diamondPlate, text("Перезарядка: " + diamondPlateCooldown + " сек."));
        applyArmorTrim(diamondPlate, TrimPattern.RAISER, TrimMaterial.DIAMOND);
        hideArmorTrim(diamondPlate);

        tankPlate = new ItemStack(Material.NETHERITE_CHESTPLATE);
        setItemId(tankPlate, tankPlateId);
        setCustomName(tankPlate, text("Танковый Нагрудник", NamedTextColor.LIGHT_PURPLE));
        addLoreLine(tankPlate, text("Урон -" + (damageReduction*100) + "%"));
        addLoreLine(tankPlate, text("Сопротивление II"));
        addLoreLine(tankPlate, text("Увеличенная отдача от ударов"));
        addLoreLine(tankPlate, text("При здоровье ").append(text("<4❤", NamedTextColor.RED)).
                append(text(": Сопротивление IV и Медлительность II")));
        addLoreLine(tankPlate, text("Снаряды оглушают врагов"));

        applyArmorTrim(tankPlate, TrimPattern.SENTRY, TrimMaterial.NETHERITE);
        hideArmorTrim(tankPlate);
        ItemMeta tankPlateMeta = tankPlate.getItemMeta();
        if (tankPlateMeta != null) {

            addAttributeModifier(tankPlateMeta, "armor", Attribute.ARMOR, 8,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            addAttributeModifier(tankPlateMeta, "toughness", Attribute.ARMOR_TOUGHNESS, 3,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);
            addAttributeModifier(tankPlateMeta, "knockback_resistance", Attribute.KNOCKBACK_RESISTANCE, 0.1,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);

            addAttributeModifier(tankPlateMeta, "knockback", Attribute.ATTACK_KNOCKBACK, 0.5,
                    AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.CHEST);

            tankPlate.setItemMeta(tankPlateMeta);
        }

        horseEgg = new ItemStack(Material.HORSE_SPAWN_EGG);
        setItemId(horseEgg, horseEggId);
        setCustomName(horseEgg, text("Конь", TextColor.color(175, 96, 50)));

        camelEgg = new ItemStack(Material.CAMEL_SPAWN_EGG);
        setItemId(camelEgg, camelEggId);
        setCustomName(camelEgg, text("Верблюд", TextColor.color(255, 179, 71)));

        shield = new ItemStack(Material.SHIELD);
        shield.setData(DataComponentTypes.MAX_DAMAGE, 100);
        addItemKeyword(shield, breakableKeyword);

        turboShovel = new ItemStack(Material.IRON_SHOVEL);
        addItemKeyword(turboShovel, breakableKeyword);
        ItemMeta shovelMeta = turboShovel.getItemMeta();
        if (shovelMeta != null) {
            shovelMeta.addEnchant(Enchantment.EFFICIENCY, 5, true);
            turboShovel.setItemMeta(shovelMeta);
        }

        createPotions();
        createArrows();
    }

    public static ItemStack strengthPotion, speedPotion, resistancePotion, jumpPotion, jump5Potion, healingPotion, invisibilityPotion,
    hastePotion, fallingPotion, fireResistancePotion,

    slownessPotion, weaknessPotion, levitationPotion, fatiguePotion, poisonPotion, hungerPotion;

    private static void createPotions() {
        // Positive
        NamedTextColor color = NamedTextColor.GREEN;
        strengthPotion = createPotion(false, false,
                new PotionEffect(PotionEffectType.STRENGTH, 30*20, 0),
                text("Сила I (0:30)", color));
        hideTooltip(strengthPotion);

        speedPotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.SPEED, 45*20, 1),
                text("Скорость II (0:45)", color));
        hideTooltip(speedPotion);

        resistancePotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.RESISTANCE, 25*20, 1),
                text("Сопротивление II (0:25)", color));
        hideTooltip(resistancePotion);

        jumpPotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.JUMP_BOOST, 45*20, 1),
                text("Прыгучесть II (0:45)", color));
        hideTooltip(jumpPotion);

        jump5Potion = createPotion(false, false,
                new PotionEffect(PotionEffectType.JUMP_BOOST, 20*20, 4),
                text("Прыгучесть V (0:20)", color));
        hideTooltip(jump5Potion);

        healingPotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.INSTANT_HEALTH, 0, 1),
                text("Здоровье II", color));
        hideTooltip(healingPotion);

        hastePotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.HASTE, 45*20, 0),
                text("Спешка I (0:45)", color));
        hideTooltip(hastePotion);

        fallingPotion = createPotion(false, false,
                new PotionEffect(PotionEffectType.SLOW_FALLING, 45*20, 0),
                text("Медленное Падение (0:45)", color));
        hideTooltip(fallingPotion);

        fireResistancePotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 45*20, 0),
                text("Огнестойкость (0:45)", color));
        hideTooltip(fireResistancePotion);

        // Negative
        color = NamedTextColor.RED;
        slownessPotion = createPotion(true, true,
                new PotionEffect(PotionEffectType.SLOWNESS, 8*20, 2),
                text("Замедление III (0:08)", color));
        hideTooltip(slownessPotion);

        weaknessPotion = createPotion(true, true,
                new PotionEffect(PotionEffectType.WEAKNESS, 15*20, 0),
                text("Слабость I (0:15)", color));
        hideTooltip(weaknessPotion);

        fatiguePotion = createPotion(true, true,
                new PotionEffect(PotionEffectType.MINING_FATIGUE, 30*20, 0),
                text("Утомление I (0:30)", color));
        hideTooltip(fatiguePotion);

        poisonPotion = createPotion(true, true,
                new PotionEffect(PotionEffectType.POISON, 10*20, 1),
                text("Отравление II (0:10)", color));
        hideTooltip(poisonPotion);

        hungerPotion = createPotion(true, true,
                new PotionEffect(PotionEffectType.HUNGER, 15*20, 49),
                text("Голод L (0:15)", color));
        hideTooltip(hungerPotion);


        // Neutral
        color = NamedTextColor.WHITE;
        invisibilityPotion = createPotion(false, false,
                new PotionEffect(PotionEffectType.INVISIBILITY, 60*20, 0),
                text("Невидимость (1:00)", color));
        hideTooltip(invisibilityPotion);

        levitationPotion = createPotion(true, false,
                new PotionEffect(PotionEffectType.LEVITATION, 8*20, 2),
                text("Левитация III (0:08)", color));
        hideTooltip(levitationPotion);
    }

    public static ItemStack slownessArrow, weaknessArrow, poisonArrow, levitationArrow, darknessArrow, fatigueArrow,
            hungerArrow, witherArrow;

    private static void createArrows() {
        NamedTextColor color = NamedTextColor.RED;
        slownessArrow = createTippedArrow(new PotionEffect(PotionEffectType.SLOWNESS, 5*20, 3),
                text("Замедление VI (0:05)", color));
        hideTooltip(slownessArrow);

        weaknessArrow = createTippedArrow(new PotionEffect(PotionEffectType.WEAKNESS, 5*20, 0),
                text("Слабость I (0:05)", color));
        hideTooltip(weaknessArrow);

        poisonArrow = createTippedArrow(new PotionEffect(PotionEffectType.POISON, 5*20, 1),
                text("Отравление II (0:05)", color));
        hideTooltip(poisonArrow);

        levitationArrow = createTippedArrow(new PotionEffect(PotionEffectType.LEVITATION, 5*20, 1),
                text("Левитация II (0:05)", color));
        hideTooltip(levitationArrow);

        darknessArrow = createTippedArrow(new PotionEffect(PotionEffectType.DARKNESS, 5*20, 0),
                text("Тьма (0:05)", color));
        hideTooltip(darknessArrow);

        fatigueArrow = createTippedArrow(new PotionEffect(PotionEffectType.MINING_FATIGUE, 10*20, 0),
                text("Утомление I (0:10)", color));
        hideTooltip(fatigueArrow);

        hungerArrow = createTippedArrow(new PotionEffect(PotionEffectType.HUNGER, 10*20, 49),
                text("Голод L (0:10)", color));
        hideTooltip(hungerArrow);

        witherArrow = createTippedArrow(new PotionEffect(PotionEffectType.WITHER, 5*20, 0),
                text("Иссушение I (0:05)", color));
        hideTooltip(witherArrow);
    }

    private static void hideTooltip(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
            item.setItemMeta(meta);
        }
    }

    public static boolean isBreakable(ItemStack item) {
        return hasItemKeyword(item, breakableKeyword);
    }

    private static void addAttributeModifier(ItemMeta meta, String keyName, Attribute attribute,
                                     double value, AttributeModifier.Operation operation, EquipmentSlotGroup slot) {
        NamespacedKey key = new NamespacedKey(plugin, keyName);
        AttributeModifier modifier = new AttributeModifier(key, value, operation, slot);
        meta.addAttributeModifier(attribute, modifier);
    }

    private static ItemStack createPotion(boolean splash, boolean lingering,
                                          PotionEffect customEffect, Component customName) {
        // Determine the potion material (normal, splash, or lingering)
        Material potionMaterial = Material.POTION;
        if (splash) potionMaterial = Material.SPLASH_POTION;
        if (lingering) potionMaterial = Material.LINGERING_POTION;

        // Create the potion ItemStack
        ItemStack potion = new ItemStack(potionMaterial);
        PotionMeta meta = (PotionMeta) potion.getItemMeta();

        if (meta != null) {

            // Apply custom potion effects
            if (customEffect != null) {
                meta.addCustomEffect(customEffect, true);
            }

            // Set custom name if provided
            if (customName != null) {
                meta.displayName(customName);
            }

            potion.setItemMeta(meta);
        }

        return potion;
    }

    private static ItemStack createTippedArrow(PotionEffect customEffect, Component customName) {
        ItemStack arrow = new ItemStack(Material.TIPPED_ARROW);
        PotionMeta meta = (PotionMeta) arrow.getItemMeta();

        if (meta != null) {
            // Apply custom effects if provided
            if (customEffect != null) {
                meta.addCustomEffect(customEffect, true);
            }

            if (customName != null) {
                meta.displayName(customName);
            }

            arrow.setItemMeta(meta);
        }

        return arrow;
    }

    public static void applyArmorTrim(ItemStack armor, TrimPattern pattern, TrimMaterial material) {
        if (material != null && armor.getItemMeta() instanceof ArmorMeta armorMeta) {
            armorMeta.setTrim(new ArmorTrim(material, pattern)); // Adjust pattern as needed
            armor.setItemMeta(armorMeta);
        }
    }

    public static void hideArmorTrim(ItemStack item) {
        if (item == null) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        meta.addItemFlags(ItemFlag.HIDE_ARMOR_TRIM);
        meta.addItemFlags(ItemFlag.HIDE_DYE);
        item.setItemMeta(meta);
    }

    public static void makeConsumable(ItemStack itemStack) {
        itemStack.setData(DataComponentTypes.CONSUMABLE,
                Consumable.consumable().consumeSeconds(0)
                        .animation(ItemUseAnimation.NONE)
                        .hasConsumeParticles(false)
                        .sound(Key.key(""))
                        .build());
    }
}
