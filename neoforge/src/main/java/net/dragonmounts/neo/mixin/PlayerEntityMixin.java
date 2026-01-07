package net.dragonmounts.neo.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalFloatRef;
import net.dragonmounts.neo.common.capability.ArmorEffectManager.Provider;
import net.dragonmounts.neo.common.capability.ArmorEffectManagerImpl;
import net.dragonmounts.neo.common.init.DMArmorEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityAttachment;
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
        tag.put(SERIALIZATION_KEY, data);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readCooldown(CompoundTag tag, CallbackInfo info) {
        this.neodragonmounts$manager.readNBT(tag.getCompound(SERIALIZATION_KEY));
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

    @Override
    public ArmorEffectManagerImpl neodragonmounts$getManager() {
        return this.neodragonmounts$manager;
    }

    private PlayerEntityMixin(EntityType<? extends LivingEntity> a, Level b) {super(a, b);}
}
