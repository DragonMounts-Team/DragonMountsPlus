package net.dragonmounts.neo.common.type;

import net.dragonmounts.neo.common.client.ClientDragonEntity;
import net.dragonmounts.neo.common.entity.ai.behavior.RangedAttack;
import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.dragonmounts.neo.compat.registry.DragonType;
import net.dragonmounts.neo.compat.registry.DragonTypeBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class SculkType extends DragonType {
    public SculkType(ResourceLocation identifier, DragonTypeBuilder builder) {
        super(identifier, builder);
    }

    @Override
    public @Nullable RangedAttack<? super ServerDragonEntity> initRangedAttack(ServerDragonEntity dragon) {
        return null;
    }

    @Override
    public @Nullable RangedAttack<? super ClientDragonEntity> initRangedAttack(ClientDragonEntity dragon) {
        return null;
    }
}
