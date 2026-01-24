package net.dragonmounts.neo.common.client;

import net.dragonmounts.neo.common.client.gui.FluteScreen;
import net.dragonmounts.neo.common.entity.dragon.DragonModelContracts;
import net.dragonmounts.neo.common.util.Segment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Range;

import java.util.UUID;

public class ClientUtil {
    private static final int CAPACITY = DragonModelContracts.TAIL_SEGMENTS;
    private static final String[] STRING_OF_INT;

    static {
        var cache = new String[CAPACITY];
        for (int i = 0; i < cache.length; ++i) {
            cache[i] = Integer.toString(i);
        }
        STRING_OF_INT = cache;
    }

    public static Level getLevel() {
        return Minecraft.getInstance().level;
    }

    public static void openFluteScreen(UUID uuid) {
        Minecraft.getInstance().setScreen(new FluteScreen(uuid));
    }

    public static PartPose scaledPose(float scaleX, float scaleY, float scaleZ) {
        return new PartPose(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, scaleX, scaleY, scaleZ);
    }

    public static PartPose scaledPose(float scale) {
        return scaledPose(scale, scale, scale);
    }

    public static String toString(@Range(from = 0, to = CAPACITY - 1) int i) {
        return STRING_OF_INT[i];
    }

    public static ModelPart[] getChildren(ModelPart parent, @Range(from = 0, to = CAPACITY) int length) {
        var children = new ModelPart[length];
        for (int i = 0; i < length; ++i) {
            children[i] = parent.getChild(STRING_OF_INT[i]);
        }
        return children;
    }

    public static float takeIfValid(float neo, float old) {
        return Float.isNaN(neo) ? old : neo;
    }

    public static void loadSegment(ModelPart part, Segment segment) {
        part.x = takeIfValid(segment.posX, part.x);
        part.y = takeIfValid(segment.posY, part.y);
        part.z = takeIfValid(segment.posZ, part.z);
        part.xRot = takeIfValid(segment.rotX, part.xRot);
        part.yRot = takeIfValid(segment.rotY, part.yRot);
        part.zRot = takeIfValid(segment.rotZ, part.zRot);
    }

    public static boolean isRemoteServer() {
        var minecraft = Minecraft.getInstance();
        return minecraft.getCurrentServer() != null && !minecraft.isSingleplayer();
    }
}
