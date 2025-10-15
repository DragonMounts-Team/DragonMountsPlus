package net.dragonmounts.neo.common.client.debug;

import com.mojang.blaze3d.vertex.PoseStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public enum DebugInfoRenderer implements DebugRenderer.SimpleDebugRenderer {
    INSTANCE;

    private List<AABB> boxes;
    private List<Vec3> points;
    private List<VoxelShape> boxShapes = Collections.emptyList();
    private List<VoxelShape> pointShapes = Collections.emptyList();

    @Override
    public void render(@NotNull PoseStack matrices, @NotNull MultiBufferSource buffers, double camX, double camY, double camZ) {
        var boxes = DebugInfo.DEBUG_BOXES;
        if (this.boxes != boxes) {
            this.boxes = boxes;
            if (boxes == null) {
                this.boxShapes = Collections.emptyList();
            } else {
                var list = new ObjectArrayList<VoxelShape>(boxes.size());
                for (var box : boxes) {
                    list.add(Shapes.create(box));
                }
                this.boxShapes = list;
            }
        }
        var points = DebugInfo.DEBUG_POINTS;
        if (this.points != points) {
            this.points = points;
            if (points == null) {
                this.pointShapes = Collections.emptyList();
            } else {
                var list = new ObjectArrayList<VoxelShape>(points.size());
                for (var point : points) {
                    list.add(Shapes.create(point.x - 0.1, point.y - 0.1, point.z - 0.1, point.x + 0.1, point.y + 0.1, point.z + 0.1));
                }
                this.pointShapes = list;
            }
        }
        var buffer = buffers.getBuffer(RenderType.lines());
        for (var shape : this.boxShapes) {
            DebugRenderer.renderVoxelShape(matrices, buffer, shape, -camX, -camY, -camZ, 1.0F, 1.0F, 1.0F, 1.0F, true);
        }
        for (var shape : this.pointShapes) {
            DebugRenderer.renderVoxelShape(matrices, buffer, shape, -camX, -camY, -camZ, 1.0F, 1.0F, 1.0F, 1.0F, true);
        }
    }
}
