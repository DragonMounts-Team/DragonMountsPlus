package net.dragonmounts.neo.mixin;

import net.dragonmounts.neo.config.ServerConfig;
import net.minecraft.network.protocol.common.custom.BrainDebugPayload;
import net.minecraft.network.protocol.game.DebugPackets;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.BehaviorControl;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Target;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

import static net.dragonmounts.neo.compat.platform.ServerNetworkHandler.sendTracking;

@Mixin(DebugPackets.class)
public class DebugPacketsMixin {
    @Shadow
    private static List<String> getMemoryDescriptions(LivingEntity entity, long gameTime) {
        return Collections.emptyList();
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "sendEntityBrain", at = @At("HEAD"))
    private static void sendEntityBrain(LivingEntity entity, CallbackInfo info) {
        if (ServerConfig.INSTANCE.debug.get()) {
            var brain = entity.getBrain();
            sendTracking(entity, new BrainDebugPayload(new BrainDebugPayload.BrainDump(
                    entity.getUUID(),
                    entity.getId(),
                    entity.getScoreboardName(),
                    "",
                    0,
                    entity.getHealth(),
                    entity.getMaxHealth(),
                    entity.position().add(0, -2.4, 0),
                    "",
                    brain.getMemory(MemoryModuleType.PATH).map(path -> {
                        var temp = new Node[0];
                        var target = path.getEndNode();
                        path.setDebug(temp, temp, target == null
                                ? Collections.emptySet()
                                : Collections.singleton(new Target(target))
                        );
                        return path;
                    }).orElse(null),
                    false,
                    -1,
                    brain.getActiveActivities().stream().map(Activity::getName).toList(),
                    brain.getRunningBehaviors().stream().map(BehaviorControl::debugString).toList(),
                    getMemoryDescriptions(entity, entity.level().getGameTime()),
                    Collections.emptyList(),
                    Collections.emptySet(),
                    Collections.emptySet()
            )));
        }
    }
}
