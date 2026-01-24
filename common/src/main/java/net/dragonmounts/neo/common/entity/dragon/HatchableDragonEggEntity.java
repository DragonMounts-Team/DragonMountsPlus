package net.dragonmounts.neo.common.entity.dragon;

import net.dragonmounts.neo.common.api.DragonTypified;
import net.dragonmounts.neo.common.api.DynamicAttributeEntity;
import net.dragonmounts.neo.common.api.ScoreboardAccessor;
import net.dragonmounts.neo.common.block.HatchableDragonEggBlock;
import net.dragonmounts.neo.common.init.DMBlocks;
import net.dragonmounts.neo.common.init.DMEntities;
import net.dragonmounts.neo.common.init.DMSounds;
import net.dragonmounts.neo.common.init.DragonTypes;
import net.dragonmounts.neo.common.item.DragonScalesItem;
import net.dragonmounts.neo.common.network.s2c.SyncEggAgePayload;
import net.dragonmounts.neo.common.network.s2c.WobbleEggPayload;
import net.dragonmounts.neo.compat.platform.ServerNetworkHandler;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonVariant;
import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.players.OldUsersConverter;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.entity.EntityTypeTest;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.UUID;

import static net.minecraft.resources.ResourceLocation.tryParse;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public class HatchableDragonEggEntity extends LivingEntity implements DynamicAttributeEntity, DragonTypified.Mutable {
    public static ServerDragonEntity hatch(ServerLevel world, HatchableDragonEggEntity egg, DragonLifeStage stage) {
        return new ServerDragonEntity(world, (level, dragon) -> {
            CompoundTag data = egg.saveWithoutId(new CompoundTag());
            data.remove(HatchableDragonEggEntity.SERIALIZATION_KEY_AGE);
            data.remove(DragonLifeStage.SERIALIZATION_KEY);
            dragon.load(data);
            dragon.overrideType(egg.getDragonType(), false);
            dragon.setLifeStage(stage, true, false);
            dragon.setHealth(dragon.getMaxHealth() + egg.getHealth() - egg.getMaxHealth());
        });
    }

    public static final String SERIALIZATION_KEY_AGE = "Age";
    protected static final EntityDataAccessor<DragonType> DATA_DRAGON_TYPE = SynchedEntityData.defineId(HatchableDragonEggEntity.class, DragonType.SERIALIZER);
    public static final float EGG_CRACK_THRESHOLD = 0.9F;
    public static final float EGG_WOBBLE_THRESHOLD = 0.75F;
    public static final float EGG_WOBBLE_BASE_CHANCE = 0.05F;
    public static final String SERIALIZATION_KEY_VANILLA = "IsVanilla";
    public static final String SERIALIZATION_KEY_SPAWNER = "FromSpawner";
    protected @Nullable String variant;
    protected @Nullable UUID owner;
    protected boolean hatched;
    protected boolean shatter;
    protected boolean isVanilla;
    protected float amplitude;
    protected float amplitudeO;
    protected float wobbleAxis;
    protected int wobbling;
    protected int age;

    public static HatchableDragonEggEntity construct(EntityType<? extends HatchableDragonEggEntity> type, Level level) {
        return new HatchableDragonEggEntity(type, level);
    }

    public HatchableDragonEggEntity(EntityType<? extends HatchableDragonEggEntity> type, Level level) {
        super(type, level);
    }

    public HatchableDragonEggEntity(Level level) {
        this(DMEntities.HATCHABLE_DRAGON_EGG.get(), level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, ServerConfig.INSTANCE.baseHealth.get())
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_DRAGON_TYPE, DragonTypes.ENDER);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString(DragonType.SERIALIZATION_KEY, this.getDragonType().identifier.toString());
        tag.putInt(SERIALIZATION_KEY_AGE, this.age);
        tag.putBoolean(SERIALIZATION_KEY_VANILLA, this.isVanilla);
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner);
        }
        if (this.variant != null) {
            tag.putString(DragonVariant.SERIALIZATION_KEY, this.variant);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains(DragonType.SERIALIZATION_KEY)) {
            this.overrideType(DragonType.REGISTRY.getValue(tryParse(tag.getString(DragonType.SERIALIZATION_KEY))), false);
        }
        if (tag.contains(DragonVariant.SERIALIZATION_KEY)) {
            this.variant = tag.getString(DragonVariant.SERIALIZATION_KEY);
        }
        if (tag.contains(SERIALIZATION_KEY_AGE)) {
            this.setAge(tag.getInt(SERIALIZATION_KEY_AGE), !this.firstTick);
        }
        if (tag.contains(SERIALIZATION_KEY_VANILLA)) {
            this.setVanilla(tag.getBoolean(SERIALIZATION_KEY_VANILLA));
        }
        if (tag.hasUUID("Owner")) {
            this.owner = tag.getUUID("Owner");
        } else if (tag.contains("Owner")) {
            var name = tag.getString("Owner");
            var server = this.getServer();
            this.owner = server == null
                    ? UUIDUtil.createOfflinePlayerUUID(name)
                    : OldUsersConverter.convertMobOwnerIfNecessary(server, name);
        } else {
            this.owner = null;
        }
        if (tag.getBoolean(SERIALIZATION_KEY_SPAWNER)) {
            this.hatched = true;
        }
    }

    public final void setVanilla(boolean vanilla) {
        this.isVanilla = vanilla;
    }

    protected void spawnScales(ServerLevel level, int amount) {
        if (amount > 0) {
            var scales = this.getDragonType().getInstance(DragonScalesItem.class, null);
            if (scales != null && level.getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
                this.spawnAtLocation(level, new ItemStack(scales, amount), 1.25F);
            }
        }
    }

    @Override
    public void onRemoval(RemovalReason reason) {
        if (this.hatched && this.level() instanceof ServerLevel level) {
            level.addFreshEntity(hatch(level, this, DragonLifeStage.HATCHLING));
            if (this.shatter) {
                this.spawnScales(level, this.random.nextInt(4) + 4);
                this.makeSound(DMSounds.DRAGON_EGG_SHATTER);
                level.levelEvent(2001, this.blockPosition(), Block.getId(
                        this.asBlock(DMBlocks.ENDER_DRAGON_EGG.get()).defaultBlockState()
                ));
            }
        }
    }

    public void hatch(boolean shatter) {
        if (this.level() instanceof ServerLevel level) {
            ((ScoreboardAccessor) level.getScoreboard()).neodragonmounts$preventRemoval(this);
            this.hatched = true;
            this.shatter = shatter;
        }
        this.discard();
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return Collections.singleton(ItemStack.EMPTY);
    }

    @Override
    public ItemStack getItemBySlot(EquipmentSlot slot) {return ItemStack.EMPTY;}

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {}

    @Override
    public HumanoidArm getMainArm() {return HumanoidArm.RIGHT;}

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (this.isAlive() && player.isShiftKeyDown()) {
            var block = this.asBlock(null);
            if (block == null) return InteractionResult.FAIL;
            if (this.level().isClientSide) return InteractionResult.SUCCESS;
            this.discard();
            this.level().setBlockAndUpdate(this.blockPosition(), block.defaultBlockState());
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.PASS;
    }

    @Override
    public void aiStep() {
        if (this.hatched) {
            this.discard();
            return;
        }
        super.aiStep();
        var random = this.random;
        var level = this.level();
        if (level instanceof ServerLevel server) {
            ++this.age;
            --this.wobbling;
            // play the egg wobble animation based on the time the eggs take to hatch
            if (this.wobbling < 0) {
                float progress = this.getIncubationProgress();
                if (progress > EGG_WOBBLE_THRESHOLD) {
                    // wait until the egg is nearly hatched
                    float chance = (progress - EGG_WOBBLE_THRESHOLD) * EGG_WOBBLE_BASE_CHANCE * (1 - EGG_WOBBLE_THRESHOLD);
                    if (progress >= 1.0F && random.nextFloat() * 2.0F < chance) {
                        this.hatch(true);
                    } else if (random.nextFloat() < chance) {
                        boolean crack = progress > EGG_CRACK_THRESHOLD;
                        int flag = crack ? 0b01 : 0b00;
                        ServerNetworkHandler.sendTracking(this, new WobbleEggPayload(
                                this.getId(),
                                this.wobbling = random.nextInt(21) + 10,//[10, 30]
                                random.nextInt(180),
                                random.nextBoolean() ? 0b10 | flag : flag
                        ));
                        if (crack) {
                            this.spawnScales(server, 1);
                        }
                    }

                }
            }
            return;
        }
        if (--this.wobbling > 0) {
            this.amplitudeO = this.amplitude;
            this.amplitude = Mth.sin(level.getGameTime() * 0.5F) * Math.min(this.wobbling, 15);
        }
        // spawn generic particles
        var type = this.getDragonType();
        double px = getX() + (random.nextDouble() - 0.5);
        double py = getY() + (random.nextDouble() - 0.3);
        double pz = getZ() + (random.nextDouble() - 0.5);
        double ox = (random.nextDouble() - 0.5) * 2;
        double oy = (random.nextDouble() - 0.3) * 2;
        double oz = (random.nextDouble() - 0.5) * 2;
        level.addParticle(type.eggParticle, px, py, pz, ox, oy, oz);
        if ((++this.age & 1) == 0 && type != DragonTypes.ENDER) {
            level.addParticle(new DustParticleOptions(type.color, 1.0F), px, py + 0.8, pz, ox, oy, oz);
        }
    }

    @Override
    protected void pushEntities() {
        if (!ServerConfig.INSTANCE.isEggPushable.get()) return;
        var box = this.getBoundingBox().inflate(0.125, -0.0625, 0.125);
        var level = this.level();
        (level.isClientSide
                ? level.getEntities(EntityTypeTest.forClass(Player.class), box, EntitySelector.pushableBy(this))
                : level.getEntities(this, box, EntitySelector.pushableBy(this))
        ).forEach(this::doPush);
    }

    public float getIncubationProgress() {
        int duration = ServerConfig.INSTANCE.minIncubationDuration.getAsInt();
        return duration > 0 ? this.age / (float) duration : 1.0F;
    }

    @Override
    public ItemStack getPickResult() {
        return new ItemStack(this.asBlock(DMBlocks.ENDER_DRAGON_EGG.get()));
    }

    @Override
    protected Component getTypeName() {
        return this.getDragonType().getFormattedName("entity.neodragonmounts.dragon_egg.name");
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return super.isInvulnerableTo(level, source) || this.getDragonType().isInvulnerableTo(source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        if (source.is(DamageTypes.MACE_SMASH)) {
            if (super.hurtServer(level, source, Math.max(20F, amount * 3F))) {
                this.spawnScales(level, 1);
                return true;
            }
            return false;
        } else {
            var weapon = source.getWeaponItem();
            if (weapon != null && (weapon.is(ItemTags.MACE_ENCHANTABLE))) {
                if (super.hurtServer(level, source, amount * 3F)) {
                    this.spawnScales(level, 1);
                    return true;
                }
                return false;
            }
        }
        return super.hurtServer(level, source, amount);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return Entity.MovementEmission.NONE;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return ServerConfig.INSTANCE.isEggPushable.get() && super.isPushable();
    }

    @Override
    public void push(Entity entity) {
        if (ServerConfig.INSTANCE.isEggPushable.get()) {
            super.push(entity);
        }
    }

    @Override
    protected boolean isImmobile() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void thunderHit(ServerLevel level, LightningBolt bolt) {
        super.thunderHit(level, bolt);
        this.getDragonType().onThunderHit(this, bolt);
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity entity) {
        return new ClientboundAddEntityPacket(this, entity, this.age);
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket packet) {
        super.recreateFromPacket(packet);
        this.age = packet.getData();
    }

    public void setAge(int age, boolean lazySync) {
        if (lazySync && this.age != age) {
            ServerNetworkHandler.sendTracking(this, new SyncEggAgePayload(this.getId(), age));
        }
        this.age = age;
    }

    public int getAge() {
        return this.age;
    }

    public float getWobbleAxis() {
        return this.wobbleAxis;
    }

    public float getAmplitude(float partialTicks) {
        return this.wobbling > 0 ? Mth.lerp(partialTicks, this.amplitudeO, this.amplitude) : 0;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return DMSounds.DRAGON_EGG_SHATTER;
    }

    @Override
    protected @Nullable SoundEvent getHurtSound(DamageSource source) {
        return source.is(DamageTypes.MACE_SMASH) ? DMSounds.DRAGON_EGG_SHATTER : DMSounds.DRAGON_EGG_CRACK;
    }

    public void applyWobble(int amplitude, int axis, boolean crack) {
        var level = this.level();
        this.wobbling = amplitude;
        this.wobbleAxis = axis * DEG_TO_RAD;
        // use game time to make amplitude consistent between clients
        float target = Mth.sin(level.getGameTime() * 0.5F) * Math.min(amplitude, 15);
        // multiply with a factor to make it smoother
        this.amplitudeO = target * 0.25F;
        this.amplitude = target * 0.75F;
        if (crack) {
            level.levelEvent(2001, this.blockPosition(), Block.getId(
                    this.asBlock(DMBlocks.ENDER_DRAGON_EGG.get()).defaultBlockState()
            ));
        }
        level.playLocalSound(this, DMSounds.DRAGON_EGG_CRACK, SoundSource.NEUTRAL, 1.0F, 1.0F);
    }

    @Contract("!null -> !null")
    public @Nullable Block asBlock(@Nullable HatchableDragonEggBlock fallback) {
        return this.isVanilla
                ? Blocks.DRAGON_EGG
                : this.getDragonType().getInstance(HatchableDragonEggBlock.class, fallback);
    }

    /// @deprecated use {@link #overrideType(DragonType, boolean)}
    @Deprecated
    @Override
    public final void convertTo(DragonType type, boolean reset) {
        this.overrideType(type, reset);
    }

    @Override
    public final void overrideType(DragonType type, boolean reset) {
        var manager = this.getAttributes();
        manager.removeAttributeModifiers(this.getDragonType().attributes);
        this.entityData.set(DATA_DRAGON_TYPE, type);
        manager.addTransientAttributeModifiers(type.attributes);
        if (type != DragonTypes.ENDER) {
            this.setVanilla(false);
        }
        if (reset) {
            this.setHealth(this.getMaxHealth());
        }
    }

    @Override
    public final DragonType getDragonType() {
        return this.entityData.get(DATA_DRAGON_TYPE);
    }

    @Override
    public AttributeSupplier getDynamicAttributes() {
        return ServerConfig.INSTANCE.getDragonEggAttributes();
    }
}
