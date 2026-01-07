package net.dragonmounts.neo.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.dragonmounts.neo.common.capability.ArmorEffectManager.Provider;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.init.DMArmorEffects;
import net.dragonmounts.neo.common.item.DragonScaleShieldItem;
import net.dragonmounts.neo.common.network.s2c.ArmorRipostePayload;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl.SERIALIZATION_KEY;
import static net.dragonmounts.neo.common.util.EntityUtil.addOrMergeEffect;
import static net.minecraft.world.damagesource.DamageTypes.SONIC_BOOM;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends LivingEntity implements Provider {
    @Unique
    protected final ArmorEffectManagerImpl neodragonmounts$manager = new ArmorEffectManagerImpl(Player.class.cast(this));

    @Unique
    private boolean neodragonmounts$reflecting;

    @Inject(method = "tick", at = @At("HEAD"))
    public void tickManager(CallbackInfo info) {
        this.neodragonmounts$manager.tick();
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void saveCooldown(CompoundTag tag, CallbackInfo info) {
        var data = this.neodragonmounts$manager.saveNBT();
        if (data.isEmpty()) return;
        tag.put("ForgeCaps", data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCooldown(CompoundTag tag, CallbackInfo info) {
        this.neodragonmounts$manager.readNBT(tag.getCompound(SERIALIZATION_KEY));
    }

    @ModifyExpressionValue(method = "hurtCurrentlyUsedShield", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"))
    public boolean isShield(boolean original) {
        return original || this.useItem.getItem() instanceof DragonScaleShieldItem;
    }

    @Inject(method = "hurtServer", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;removeEntitiesOnShoulder()V",
            shift = At.Shift.AFTER
    ))
    public void handleSonicBoom(
            ServerLevel level,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> info,
            @Local(argsOnly = true) LocalFloatRef damage
    ) {
        if (damage.get() == 0.0F || !source.is(SONIC_BOOM)) return;
        int amplifier = this.neodragonmounts$manager.getLevel(DMArmorEffects.SCULK, true);
        if (amplifier < 2) return;
        if (amplifier > 3 && !this.neodragonmounts$reflecting && source.getEntity() instanceof LivingEntity attacker) {
            if (!attacker.closerThan(this, 24, 32)) return;
            this.neodragonmounts$reflecting = true;
            var start = this.position().add(this.getAttachments().get(EntityAttachment.WARDEN_CHEST, 0, this.getYRot()));
            var distance = attacker.getEyePosition().subtract(start);
            var direction = distance.normalize();
            for (int i = Mth.floor(distance.length()) + 7, j = 1; j < i; ++j) {
                var pos = start.add(direction.scale(j));
                level.sendParticles(ParticleTypes.SONIC_BOOM, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
            if (attacker.hurtServer(level, level.damageSources().sonicBoom(this), damage.get() * 0.75F)) {
                double resistance = attacker.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), horizontal = 2.5 - 2.5 * resistance;
                attacker.push(direction.x() * horizontal, direction.y() * (0.5 - 0.5 * resistance), direction.z() * horizontal);
            }
            this.neodragonmounts$reflecting = false;
        }
        damage.set(damage.get() * Math.max(1.0F / amplifier, 0.0F));
    }

    @Inject(method = "actuallyHurt", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getDamageAfterArmorAbsorb(Lnet/minecraft/world/damagesource/DamageSource;F)F"
    ))
    public void riposte(ServerLevel level, DamageSource source, float amount, CallbackInfo info) {
        var ice = DMArmorEffects.ICE;
        var nether = DMArmorEffects.NETHER;
        var manager = this.neodragonmounts$manager;
        var iceFlag = manager.isActive(ice) && manager.getCooldown(ice) <= 0;
        var netherFlag = manager.isActive(nether) && manager.getCooldown(nether) <= 0;
        int flag = (iceFlag ? 0b01 : 0b00) | (netherFlag ? 0b10 : 0b00);
        if (flag == 0) return;
        var entities = level.getEntities(this, this.getBoundingBox().inflate(5.0D), EntitySelector.NO_CREATIVE_OR_SPECTATOR);
        if (entities.isEmpty()) return;
        var freeze = level.damageSources().freeze();
        for (var entity : entities) {
            if (entity instanceof LivingEntity target) {
                target.knockback(0.4F, 1, 1);
                if (iceFlag) {
                    addOrMergeEffect(target, MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, true, true);
                    entity.invulnerableTime = 0;
                    entity.hurtServer(level, freeze, 1F);
                }
            } else if (iceFlag) {
                entity.invulnerableTime = 0;
                entity.hurtServer(level, freeze, 1F);
            }
            if (netherFlag) {
                int current = entity.getRemainingFireTicks();
                entity.setRemainingFireTicks(current > 0 ? current + 200 : 200);
            }
        }
        if (iceFlag) {
            manager.setCooldown(ice, ice.cooldown);
        }
        if (netherFlag) {
            manager.setCooldown(nether, nether.cooldown);
        }
        var payload = new ArmorRipostePayload(this.getId(), flag);
        for (var player : PlayerLookup.tracking(this)) {
            ServerPlayNetworking.send(player, payload);
        }
        ServerPlayNetworking.send(ServerPlayer.class.cast(this), payload);
    }

    @Override
    public ArmorEffectManagerImpl neodragonmounts$getManager() {
        return this.neodragonmounts$manager;
    }

    private PlayerEntityMixin(EntityType<? extends LivingEntity> a, Level b) {super(a, b);}
}
