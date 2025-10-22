package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.client.breath.impl.ClientBreathAdapter;
import net.dragonmounts.neo.common.entity.ai.behavior.RangedAttack;
import net.dragonmounts.neo.common.entity.breath.DragonBreathSpec;
import net.dragonmounts.neo.common.entity.breath.impl.FireBreath;
import net.dragonmounts.neo.common.entity.breath.impl.ServerBreathAdapter;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.common.entity.dragon.TameableDragonEntity;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;

public class BreathableType extends DragonType {
    public BreathableType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    public DragonBreathSpec initBreath(TameableDragonEntity dragon) {
        return new FireBreath(dragon, 0.7F);
    }

    @Override
    public RangedAttack<? super ServerDragonEntity> initRangedAttack(ServerDragonEntity dragon) {
        return new ServerBreathAdapter(this.initBreath(dragon));
    }

    @Override
    public RangedAttack<? super ClientDragonEntity> initRangedAttack(ClientDragonEntity dragon) {
        return new ClientBreathAdapter(this.initBreath(dragon));
    }
}
