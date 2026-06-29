package me.wega.wegapolls;

import com.destroystokyo.paper.inventory.meta.ArmorStandMeta;
import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Axolotl;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;


// TAKEN FROM https://github.com/OpenBukkitUtils/ItemBuilder

/**
 * Class to ease the creation of {@link ItemStack}s
 */
/*
Missing builders:

BlockData,
BlockState,
Compass,
Map,
MusicInstrument,
OminousBottle,
Repairable,
SpawnEgg,
TropicalFish
 */
public class ItemBuilder {

    protected ItemStack stack;
    protected ItemMeta meta;

    public ItemBuilder(Material type) {
        stack = new ItemStack(type);
        meta = stack.getItemMeta();
    }

    public static ItemBuilder item(Material type) {
        return new ItemBuilder(type);
    }

    public static ItemBuilder item(Material type, int amount) {
        return new ItemBuilder(type, amount);
    }

    public static ItemBuilder editItem(ItemStack itemStackToEdit) {
        return new ItemBuilder(itemStackToEdit);
    }

    public static ItemBuilder fromTemplate(ItemStack itemStack) {
        return new ItemBuilder(itemStack.clone());
    }

    public ItemBuilder(Material type, int amount) {
        stack = new ItemStack(type, amount);
        meta = stack.getItemMeta();
    }

    public ItemBuilder(Material type, Component... loreLines) {
        stack = new ItemStack(type);
        meta = stack.getItemMeta();
        meta.lore(List.of(loreLines));
    }

    private ItemBuilder(ItemStack stack) {
        this.stack = stack;
        this.meta = stack.getItemMeta();
    }

    public ItemBuilder() {
        this(Material.AIR);
    }

    public ItemBuilder enchant(Enchantment enchantment, int level) {
        meta.addEnchant(enchantment, level, true);
        return this;
    }

    /**
     * Adds the specified enchantment to this item meta.
     *
     * @param enchantment            Enchantment to add
     * @param level                  Level for the enchantment
     * @param ignoreLevelRestriction this indicates the enchantment should be
     *                               applied, allowing to enchant beyond "vanilla" levels.
     */
    public ItemBuilder enchant(Enchantment enchantment, int level, boolean ignoreLevelRestriction) {
        meta.addEnchant(enchantment, level, ignoreLevelRestriction);
        return this;
    }

    public ItemBuilder addEnchantments(Map<Enchantment, Integer> enchantments) {
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            meta.addEnchant(entry.getKey(), entry.getValue(), true);
        }
        return this;
    }

    public ItemBuilder hideEnchants() {
        flag(ItemFlag.HIDE_ENCHANTS);
        return this;
    }

    public ItemBuilder removeEnchantment(Enchantment enchantment) {
        meta.removeEnchant(enchantment);
        return this;
    }

    public ItemBuilder clearEnchantments() {
        meta.getEnchants().forEach((enchantment, integer) -> meta.removeEnchant(enchantment));
        return this;
    }

    public ItemBuilder amount(int amount) {
        stack.setAmount(amount);
        return this;
    }

    public ItemBuilder setLore(List<Component> loreLines) {
        meta.lore(loreLines);
        return this;
    }

    public ItemBuilder setLore(Component... loreLines) {
        meta.lore(Arrays.asList(loreLines));
        return this;
    }

    /**
     * Sets the lore but applies a formatter function to each lore line.
     *
     * @param formatter The formatter function that will be applied to each line.
     * @param loreLines The array of "raw" lore lines.
     * @return The ItemBuilder itself.
     */
    public ItemBuilder setLore(Function<Component, Component> formatter, Component... loreLines) {
        setLore(formatter, Arrays.asList(loreLines));
        return this;
    }

    /**
     * Sets the lore but applies a formatter function to each lore line.
     *
     * @param formatter The formatter function that will be applied to each line.
     * @param loreLines The list of "raw" lore lines.
     * @return The ItemBuilder itself.
     */
    public ItemBuilder setLore(Function<Component, Component> formatter, List<Component> loreLines) {
        List<Component> formattedLore = new ArrayList<>(loreLines.size());
        for (Component s : loreLines) {
            formattedLore.add(formatter.apply(s));
        }
        meta.lore(formattedLore);
        return this;
    }

    public ItemBuilder appendLore(Component... loreLines) {
        if (meta.hasLore()) {
            List<Component> lore = meta.lore();
            ArrayList<Component> newLore = new ArrayList<>(lore);
            newLore.addAll(Arrays.asList(loreLines));
            meta.lore(newLore);
        }
        return this;
    }

    public ItemBuilder damage(short damage) {
        if (stack instanceof Damageable damageable) {
            damageable.setDamage(damage);
        }
        return this;
    }

    public ItemBuilder damage(int damage) {
        damage((short) damage);
        return this;
    }

    public ItemBuilder name(Component name) {
        meta.itemName(name);
        return this;
    }

    public ItemBuilder unbreakable() {
        meta.setUnbreakable(true);
        return this;
    }

    public ItemBuilder hideAttributes() {
        return flag(ItemFlag.HIDE_ATTRIBUTES);
    }

    public ItemBuilder flag(ItemFlag flag) {
        meta.addItemFlags(flag);
        return this;
    }

    /**
     * Often the final method used with an ItemBuilder.
     * Applies the internal {@link ItemMeta} to the {@link ItemStack} and returns it.
     * You can technically still use the ItemBuilder object after using this method.
     *
     * @return Returns the itemStack with everything applied.
     */
    public ItemStack build() {
        stack.setItemMeta(meta);
        return stack;
    }

    /**
     * @return Returns a clone of the item meta without applying anything. Should only be used to read information from the item meta
     */
    public ItemMeta getItemMeta() {
        return meta.clone();
    }

    /**
     * @return Returns a clone of the item stack without applying anything. Should only be used to read information from the item stack.
     */
    public ItemStack getItemStack() {
        return stack.clone();
    }

    public static class BundleBuilder extends ItemBuilder {
        protected BundleMeta bundleMeta;
        protected List<ItemStack> items = null;

        public BundleBuilder() {
            super(Material.BUNDLE);
        }

        public BundleBuilder setItems(List<ItemStack> items) {
            this.items = items;
            return this;
        }

        public BundleBuilder addItem(ItemStack item) {
            if (this.items == null) {
                items = new ArrayList<>();
            }
            this.items.add(item);
            return this;
        }

        public BundleBuilder addItems(ItemStack... items) {
            if (this.items == null) {
                this.items = new ArrayList<>();
            }
            Collections.addAll(this.items, items);
            return this;
        }

        @Override
        public ItemStack build() {
            bundleMeta.setItems(this.items);
            return super.build();
        }
    }

    public static BundleBuilder bundle() {
        return new BundleBuilder();
    }

    public static class DamageableItemBuilder extends ItemBuilder {
        protected Damageable damageableMeta;

        public DamageableItemBuilder(Material type) {
            super(type);
            checkMeta();
        }

        public DamageableItemBuilder(ItemStack stack) {
            super(stack);
            checkMeta();
        }

        private void checkMeta() {
            if (this.meta instanceof Damageable) {
                damageableMeta = (Damageable) this.meta;
            } else
                throw new IllegalArgumentException("Cannot instantiate DamageableItemBuilder with non-damageable item material.");
        }

        public DamageableItemBuilder damage(int damage) {
            this.damageableMeta.setDamage(damage);
            return this;
        }

        public DamageableItemBuilder maxDamage(@Nullable Integer damage) {
            this.damageableMeta.setMaxDamage(damage);
            return this;
        }
    }

    public static DamageableItemBuilder damageable(Material type) {
        return new DamageableItemBuilder(type);
    }

    public static DamageableItemBuilder editDamageable(ItemStack itemStackToEdit) {
        return new DamageableItemBuilder(itemStackToEdit);
    }

    public static class ArmorBuilder extends DamageableItemBuilder {
        protected ArmorMeta armorMeta;

        public ArmorBuilder(Material armorMaterial) {
            super(armorMaterial);
            checkMeta(armorMaterial);
        }

        public ArmorBuilder(ItemStack stack) {
            super(stack);
            checkMeta(stack.getType());
        }

        private void checkMeta(Material armorMaterial) {
            if (meta instanceof ArmorMeta) {
                this.armorMeta = (ArmorMeta) meta;
            } else
                throw new IllegalArgumentException("Cannot instantiate TrimmedArmorBuilder with non-armor item material: " + armorMaterial + " (you need to be able to add armor trims to it)");
        }

        public ArmorBuilder setTrim(ArmorTrim armorTrim) {
            this.armorMeta.setTrim(armorTrim);
            return this;
        }
    }

    public static ArmorBuilder armor(Material armorMaterial) {
        return new ArmorBuilder(armorMaterial);
    }

    public static ArmorBuilder armor(Material armorMaterial, ArmorTrim armorTrim) {
        ArmorBuilder armorBuilder = new ArmorBuilder(armorMaterial);
        armorBuilder.setTrim(armorTrim);
        return armorBuilder;
    }

    public static ArmorBuilder editArmor(ItemStack armorItemStackToEdit) {
        return new ArmorBuilder(armorItemStackToEdit);
    }

    public static class AxolotlBucketBuilder extends ItemBuilder {
        protected AxolotlBucketMeta axolotlBucketMeta;

        public AxolotlBucketBuilder(Axolotl.Variant variant) {
            super(Material.AXOLOTL_BUCKET);
            this.axolotlBucketMeta = (AxolotlBucketMeta) this.meta;
            this.axolotlBucketMeta.setVariant(variant);
        }
    }

    public static AxolotlBucketBuilder axolotlBucket(Axolotl.Variant variant) {
        return new AxolotlBucketBuilder(variant);
    }

    public static class ArmorStandBuilder extends ItemBuilder {
        protected ArmorStandMeta armorStandMeta;

        public ArmorStandBuilder() {
            super(Material.ARMOR_STAND);
            this.armorStandMeta = (ArmorStandMeta) meta;
        }

        public ArmorStandBuilder(ItemStack itemStack) {
            super(itemStack);
            if (this.meta instanceof ArmorStandMeta) {
                this.armorStandMeta = (ArmorStandMeta) meta;
            } else {
                throw new IllegalArgumentException("Cannot instantiate ArmorStandBuilder with non-'armor stand' item material: " + itemStack.getType());
            }
        }

        public ArmorStandBuilder showArms() {
            this.armorStandMeta.setShowArms(true);
            return this;
        }

        public ArmorStandBuilder invisible() {
            this.armorStandMeta.setInvisible(true);
            return this;
        }

        public ArmorStandBuilder noBasePlate() {
            this.armorStandMeta.setNoBasePlate(true);
            return this;
        }

        public ArmorStandBuilder small() {
            this.armorStandMeta.setSmall(true);
            return this;
        }

        public ArmorStandBuilder marker() {
            this.armorStandMeta.setMarker(true);
            return this;
        }
    }

    public static ArmorStandBuilder armorStand() {
        return new ArmorStandBuilder();
    }

    public static ArmorStandBuilder editArmorStand(ItemStack itemStack) {
        return new ArmorStandBuilder(itemStack);
    }

    public static class BannerBuilder extends ItemBuilder {
        private final BannerMeta bannerMeta;

        public BannerBuilder(DyeColor color) {
            this.stack = new ItemStack(colorToMaterial(color));
            this.meta = stack.getItemMeta();
            this.bannerMeta = (BannerMeta) meta;
        }

        public BannerBuilder(ItemStack itemStack) {
            super(itemStack);
            if (this.meta instanceof BannerMeta) {
                this.bannerMeta = (BannerMeta) meta;
            } else {
                throw new IllegalArgumentException("Cannot instantiate BannerBuilder with non-banner material " + itemStack.getType());
            }
        }

        private Material colorToMaterial(DyeColor color) {
            return switch (color) {
                case WHITE -> Material.WHITE_BANNER;
                case ORANGE -> Material.ORANGE_BANNER;
                case MAGENTA -> Material.MAGENTA_BANNER;
                case LIGHT_BLUE -> Material.LIGHT_BLUE_BANNER;
                case YELLOW -> Material.YELLOW_BANNER;
                case LIME -> Material.LIME_BANNER;
                case PINK -> Material.PINK_BANNER;
                case GRAY -> Material.GRAY_BANNER;
                case LIGHT_GRAY -> Material.LIGHT_GRAY_BANNER;
                case CYAN -> Material.CYAN_BANNER;
                case PURPLE -> Material.PURPLE_BANNER;
                case BLACK -> Material.BLACK_BANNER;
                case BROWN -> Material.BROWN_BANNER;
                case GREEN -> Material.GREEN_BANNER;
                case RED -> Material.RED_BANNER;
                case BLUE -> Material.BLUE_BANNER;
                case null -> Material.WHITE_BANNER;
            };
        }

        @Contract(value = "_ -> this")
        public BannerBuilder patterns(List<Pattern> patterns) {
            this.bannerMeta.setPatterns(patterns);
            return this;
        }

        @Contract(value = "_ -> this")
        public BannerBuilder pattern(Pattern pattern) {
            this.bannerMeta.addPattern(pattern);
            return this;
        }

        @Contract(value = "_, _ -> this")
        public BannerBuilder pattern(int i, Pattern pattern) {
            this.bannerMeta.setPattern(i, pattern);
            return this;
        }

        @Contract(value = "_ -> this")
        public BannerBuilder backgroundColor(DyeColor color) {
            this.stack = this.stack.withType(colorToMaterial(color));
            return this;
        }
    }

    public static BannerBuilder banner(DyeColor backgroundColor) {
        return new BannerBuilder(backgroundColor);
    }

    public static BannerBuilder banner(DyeColor backgroundColor, Pattern... patterns) {
        return banner(backgroundColor).patterns(List.of(patterns));
    }

    public static BannerBuilder editBanner(ItemStack itemStack) {
        return new BannerBuilder(itemStack);
    }

    public static class ShieldBuilder extends BannerBuilder {
        public ShieldBuilder(DyeColor color) {
            super(color);
        }

        public ShieldBuilder(ItemStack itemStack) {
            super(itemStack);
        }

        @Override
        public ShieldBuilder pattern(Pattern pattern) {
            return (ShieldBuilder) super.pattern(pattern);
        }

        @Override
        public ShieldBuilder patterns(List<Pattern> patterns) {
            return (ShieldBuilder) super.patterns(patterns);
        }

        @Override
        public ShieldBuilder pattern(int i, Pattern pattern) {
            return (ShieldBuilder) super.pattern(i, pattern);
        }
    }

    public static ShieldBuilder shield(DyeColor backgroundColor) {
        return new ShieldBuilder(backgroundColor);
    }

    public static ShieldBuilder shield(DyeColor backgroundColor, Pattern... patterns) {
        return shield(backgroundColor).patterns(List.of(patterns));
    }

    public static ShieldBuilder editShield(ItemStack itemStack) {
        return new ShieldBuilder(itemStack);
    }

    public static class LeatherArmorBuilder extends ArmorBuilder {
        public LeatherArmorBuilder(Material type, Color color) {
            super(type);
            if (!Bukkit.getItemFactory()
                    .isApplicable(Bukkit.getItemFactory().getItemMeta(Material.LEATHER_BOOTS), type)) {
                throw new IllegalArgumentException(
                        "The provided type is not applicable for a leather armor");
            }
            ((LeatherArmorMeta) meta).setColor(color);
        }
    }

    public static LeatherArmorBuilder leatherArmor(Material type, Color color) {
        return new LeatherArmorBuilder(type, color);
    }

    public static class PlayerHeadBuilder extends ItemBuilder {

        private SkullMeta skullMeta;

        public PlayerHeadBuilder(OfflinePlayer player) {
            super(Material.PLAYER_HEAD);
            this.skullMeta = (SkullMeta) meta;
            this.skullMeta.setOwningPlayer(player);
        }

        public PlayerHeadBuilder(ItemStack itemStack) {
            this.stack = itemStack;
            this.meta = itemStack.getItemMeta();
            if (this.meta instanceof SkullMeta) {
                this.skullMeta = (SkullMeta) this.meta;
            }
        }

        public PlayerHeadBuilder owningPlayer(UUID uuid) {
            this.owningPlayer(Bukkit.getOfflinePlayer(uuid));
            return this;
        }

        public PlayerHeadBuilder owningPlayer(String playerName) {
            this.owningPlayer(Bukkit.getOfflinePlayer(playerName));
            return this;
        }

        public PlayerHeadBuilder owningPlayer(OfflinePlayer player) {
            this.skullMeta.setOwningPlayer(player);
            return this;
        }
    }

    /**
     * Creates a new {@link PlayerHeadBuilder} with an existing player head item stack.
     * Used if you want to edit attributes of the item after it has already been created.
     *
     * @param itemStack Item stack to edit
     */
    public static PlayerHeadBuilder editPlayerHead(ItemStack itemStack) {
        return new PlayerHeadBuilder(itemStack);
    }

    /**
     * Creates a new {@link PlayerHeadBuilder}. Gets the player by the given UUID, regardless if they are offline or
     * online.
     * <p>
     *
     * @param uuid the UUID of the player that the head belongs to.
     */
    public static PlayerHeadBuilder playerHead(UUID uuid) {
        return new PlayerHeadBuilder(Bukkit.getOfflinePlayer(uuid));
    }

    /**
     * Creates a new {@link PlayerHeadBuilder}. May involve a network call to figure out the player from its name
     *
     * @param playerName The name of the player that the head belongs to
     */
    public static PlayerHeadBuilder playerHead(String playerName) {
        return new PlayerHeadBuilder(Bukkit.getOfflinePlayer(playerName));
    }

    /**
     * Creates a new {@link PlayerHeadBuilder}.
     * Gets the player by the given name, regardless if they are offline or
     * online.
     * <p>
     * This will not make a web request to get the UUID for the given name,
     * thus this method will not block. However, this method will set the owning player of the head to
     * {@code null} if the player is not cached.
     * </p>
     *
     * @param playerName The name of the player that the head belongs to
     */
    public static PlayerHeadBuilder playerHeadIfCached(String playerName) {
        return new PlayerHeadBuilder(Bukkit.getOfflinePlayerIfCached(playerName));
    }

    /**
     * Creates a PlayerHeadBuilder
     *
     * @param player A player or offline player to get the head from (Player extends OfflinePlayer)
     */
    public static PlayerHeadBuilder playerHead(OfflinePlayer player) {
        return new PlayerHeadBuilder(player);
    }

    public static class FireworkEffectBuilder extends ItemBuilder {

    }

    public static class FireworkRocketBuilder extends ItemBuilder {
        private final FireworkMeta fireworkMeta;

        public FireworkRocketBuilder() {
            super(Material.FIREWORK_ROCKET);
            this.fireworkMeta = (FireworkMeta) this.meta;
        }

        public FireworkRocketBuilder(ItemStack itemStack) {
            this.stack = itemStack;
            this.meta = itemStack.getItemMeta();
            if (this.meta instanceof FireworkMeta) {
                this.fireworkMeta = (FireworkMeta) this.meta;
            } else throw new IllegalArgumentException("Has to be a stack with material type firework rocket");
        }

        public FireworkRocketBuilder addEffects(FireworkEffect... fireworkEffects) {
            this.fireworkMeta.addEffects(fireworkEffects);
            return this;
        }

        public FireworkRocketBuilder addEffect(FireworkEffect fireworkEffect) {
            this.fireworkMeta.addEffect(fireworkEffect);
            return this;
        }
    }

    public static FireworkRocketBuilder fireworkRocket() {
        return new FireworkRocketBuilder();
    }

    public static FireworkRocketBuilder editFireworkRocket(ItemStack fireworkRocket) {
        return new FireworkRocketBuilder(fireworkRocket);
    }

    public static class WrittenBookBuilder extends ItemBuilder {

        protected BookMeta bm;

        public WrittenBookBuilder() {
            super(Material.WRITTEN_BOOK);
            this.bm = (BookMeta) meta;
        }

        public WrittenBookBuilder pages(Component... pages) {
            //noinspection ResultOfMethodCallIgnored
            this.bm.pages(pages);
            return this;
        }

        public WrittenBookBuilder pages(List<Component> pages) {
            //noinspection ResultOfMethodCallIgnored
            this.bm.pages(pages);
            return this;
        }

        public WrittenBookBuilder author(Component author) {
            this.bm.author(author);
            return this;
        }

        public WrittenBookBuilder title(Component title) {
            this.bm.title(title);
            return this;
        }

    }

    public static WrittenBookBuilder writtenBook() {
        return new WrittenBookBuilder();
    }

    public static class EnchantedBookBuilder extends ItemBuilder {

        protected EnchantmentStorageMeta enchantmentStorageMeta;

        public EnchantedBookBuilder() {
            super(Material.ENCHANTED_BOOK);
            this.enchantmentStorageMeta = (EnchantmentStorageMeta) meta;
        }

        public EnchantedBookBuilder storeEnchant(Enchantment enchantment, int level) {
            this.enchantmentStorageMeta.addStoredEnchant(enchantment, level, true);
            return this;
        }

    }

    public static EnchantedBookBuilder enchantedBook() {
        return new EnchantedBookBuilder();
    }

    public static class PotionBuilder extends ItemBuilder {

        private PotionMeta potionMeta;

        public PotionBuilder() {
            this.stack = new ItemStack(Material.POTION);
            this.potionMeta = (PotionMeta) stack.getItemMeta();
        }

        public PotionBuilder splash() {
            this.stack = this.stack.withType(Material.SPLASH_POTION);
            this.potionMeta = (PotionMeta) stack.getItemMeta();
            return this;
        }

        public PotionBuilder drinkable() {
            this.stack = this.stack.withType(Material.POTION);
            this.potionMeta = (PotionMeta) stack.getItemMeta();
            return this;
        }

        public PotionBuilder lingering() {
            this.stack = this.stack.withType(Material.LINGERING_POTION);
            this.potionMeta = (PotionMeta) stack.getItemMeta();
            return this;
        }

        public PotionBuilder tippedArrow() {
            this.stack = this.stack.withType(Material.TIPPED_ARROW);
            this.potionMeta = (PotionMeta) stack.getItemMeta();
            return this;
        }

        /**
         * Adds a custom potion effect to this potion.
         *
         * @param effect the potion effect to add
         */
        public PotionBuilder effect(PotionEffect effect) {
            this.potionMeta.addCustomEffect(effect, false);
            return this;
        }

        /**
         * Adds a custom potion effect to this potion.
         *
         * @param effect    the potion effect to add
         * @param overwrite true if any existing effect of the same type should be
         */
        public PotionBuilder effect(PotionEffect effect, boolean overwrite) {
            this.potionMeta.addCustomEffect(effect, overwrite);
            return this;
        }
    }

    public static PotionBuilder potion() {
        return new PotionBuilder();
    }
}