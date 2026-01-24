package net.dragonmounts.neo.common.client.model.dragon;

import static net.dragonmounts.neo.common.client.model.dragon.DragonModel.HORN_THICK;
import static net.dragonmounts.neo.common.entity.dragon.DragonModelContracts.TAIL_SIZE;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public interface ModelMagic {
    float HALF_TAIL_SIZE = 0.5F * TAIL_SIZE;
    float TAIL_HORN_OFFSET = -0.5F * HORN_THICK;
    float TAIL_HORN_ROT_X = -15F * DEG_TO_RAD;
    float TAIL_HORN_ROT_Y = 35F * DEG_TO_RAD;
}
