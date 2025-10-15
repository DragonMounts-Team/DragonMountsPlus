package net.dragonmounts.neo.common.entity.ai.navigation;

import net.dragonmounts.neo.common.entity.dragon.ServerDragonEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.UnknownNullability;

/**
 * @see net.minecraft.world.level.pathfinder.FlyNodeEvaluator
 * @see net.minecraft.world.level.pathfinder.SwimNodeEvaluator
 */
public class DragonNodeEvaluator extends WalkNodeEvaluator {
    private @UnknownNullability ServerDragonEntity dragon;

    @Override
    public void prepare(PathNavigationRegion level, Mob mob) {
        if (!(mob instanceof ServerDragonEntity entity)) throw new IllegalArgumentException();
        this.dragon = entity;
        this.pathTypesByPosCacheByMob.clear();
        super.prepare(level, mob);
    }

    @Override
    public Node getStart() {
        if (this.dragon.onGround()) return super.getStart();
        var pos = this.dragon.position();
        return this.getStartNode(BlockPos.containing(pos.x, pos.y + 0.5, pos.z));
    }

    @Override
    protected boolean canStartAt(BlockPos pos) {
        return this.mob.getPathfindingMalus(this.getCachedPathType(pos.getX(), pos.getY(), pos.getZ())) >= 0.0F;
    }

    @Override
    public int getNeighbors(Node[] neighbors, Node center) {
        int nodes = 0, height = 0;
        var type = this.getCachedPathType(center.x, center.y, center.z);
        if (this.mob.getPathfindingMalus(this.getCachedPathType(center.x, center.y + 1, center.z)) >= 0.0F && type != PathType.STICKY_HONEY) {
            height = Mth.floor(Math.max(1.0F, this.mob.maxUpStep()));
        }

        double floor = this.getFloorLevel(new BlockPos(center.x, center.y, center.z));

        for (var direction : Direction.Plane.HORIZONTAL) {
            var neighbor = this.findAcceptedNode(center.x + direction.getStepX(), center.y, center.z + direction.getStepZ(), height, floor, direction, type);
            this.reusableNeighbors[direction.get2DDataValue()] = neighbor;
            if (this.isNeighborValid(neighbor, center)) {
                assert neighbor != null;
                neighbors[nodes++] = neighbor;
            }
        }

        Node neighbor; // injected

        for (var direction : Direction.Plane.HORIZONTAL) {
            var clockwise = direction.getClockWise();
            if (this.isDiagonalValid(center, this.reusableNeighbors[direction.get2DDataValue()], this.reusableNeighbors[clockwise.get2DDataValue()])) {
                neighbor = this.findAcceptedNode(
                        center.x + direction.getStepX() + clockwise.getStepX(), center.y, center.z + direction.getStepZ() + clockwise.getStepZ(), height, floor, direction, type
                );
                if (this.isDiagonalValid(neighbor)) {
                    assert neighbor != null;
                    neighbors[nodes++] = neighbor;
                }
            }
        }

        // modification start
        neighbor = this.findAcceptedNode(center.x, center.y + 1, center.z, height - 1, floor, Direction.UP, type);
        if (this.isVerticalNeighborValid(neighbor, center)) {
            assert neighbor != null;
            neighbors[nodes++] = neighbor;
        }
        neighbor = this.findAcceptedNode(center.x, center.y - 1, center.z, height, floor, Direction.DOWN, type);
        if (this.isVerticalNeighborValid(neighbor, center)) {
            assert neighbor != null;
            neighbors[nodes++] = neighbor;
        }
        // modification end
        return nodes;
    }

    @Override
    @Nullable
    protected Node findAcceptedNode(int x, int y, int z, int height, double floor, Direction direction, PathType from) {
        Node node = null;
        var pos = new BlockPos.MutableBlockPos();
        if (this.isInvalidHeight(this.getFloorLevel(pos.set(x, y, z)) - floor)) return null;
        var type = this.getCachedPathType(x, y, z);
        float cost = this.mob.getPathfindingMalus(type);
        if (cost >= 0.0F) {
            node = this.getNodeAndUpdateCostToMax(x, y, z, type, cost);
        }

        if (node != null
                && node.costMalus >= 0.0F
                && doesBlockHavePartialCollision(from)
                && !this.canReachWithoutCollision(node)
        ) { // adjusted order to simplify population
            node = null;
        }

        if (type != PathType.WALKABLE && (!this.isAmphibious() || type != PathType.WATER)) {
            if (height > 0 && (node == null || node.costMalus < 0.0F)
                    && type != PathType.UNPASSABLE_RAIL
                    && type != PathType.TRAPDOOR
                    && type != PathType.POWDER_SNOW
                    && (type != PathType.FENCE || this.canWalkOverFences())
            ) { // adjusted order to simplify population
                node = this.findAcceptedNode(x, y + 1, z, height - 1, floor, direction, from);
                if (node != null && (
                        // adjusted order to simplify population
                        node.type == PathType.OPEN || node.type == PathType.WALKABLE || this.mob.getBbWidth() < 1.0F
                )) {
                    // inline super.tryJumpOn :
                    double offsetX = x - direction.getStepX() + 0.5, offsetZ = z - direction.getStepZ() + 0.5;
                    double halfWidth = this.mob.getBbWidth() * 0.5;
                    if (this.hasCollisions(new AABB(
                            offsetX - halfWidth,
                            this.getFloorLevel(pos.set(offsetX, y + 1, offsetZ)) + 0.001,
                            offsetZ - halfWidth,
                            offsetX + halfWidth,
                            this.mob.getBbHeight() + this.getFloorLevel(
                                    pos.set(node.x, node.y, node.z)
                            ) - 0.002,
                            offsetZ + halfWidth
                    ))) {
                        node = null;
                    }
                }
            } else if (type == PathType.WATER && !this.isAmphibious() && !this.canFloat()) { // adjusted order to simplify population
                node = this.tryFindFirstNonWaterBelow(x, y, z, node);
            } else if (type == PathType.OPEN /* Addition: */ && this.mob.isBaby()) {
                node = this.tryFindFirstGroundNodeBelow(x, y, z);
            } else if (node == null && doesBlockHavePartialCollision(type)) { // adjusted order to simplify population
                node = this.getClosedNode(x, y, z, type);
            }
        }
        return node;
    }

    protected final boolean isVerticalNeighborValid(@Nullable Node neighbor, Node center) {
        //noinspection DataFlowIssue
        return this.isNeighborValid(neighbor, center) && switch (neighbor.type) {
            case WALKABLE, WATER -> true;
            case OPEN -> !this.dragon.isBaby();
            default -> false;
        };
    }

    @Override
    public PathType getPathType(PathfindingContext context, int x, int y, int z) {
        var type = context.getPathTypeFromState(x, y, z);
        if (type == PathType.OPEN && y >= context.level().getMinY() + 1) {
            switch (context.getPathTypeFromState(x, y - 1, z)) {
                case WALKABLE, OPEN, WATER -> type = PathType.WALKABLE;
                case DANGER_FIRE, LAVA -> {
                    return PathType.DAMAGE_FIRE;
                }
                case DAMAGE_OTHER -> {
                    return PathType.DAMAGE_OTHER;
                }
                case COCOA -> {
                    return PathType.COCOA;
                }
                case FENCE -> {
                    var pos = context.mobPosition();
                    if (pos.getX() != x || pos.getZ() != z || pos.getY() + 1 != y) return PathType.FENCE;
                }
            }
        }
        return type == PathType.WALKABLE || type == PathType.OPEN ? checkNeighbourBlocks(context, x, y, z, type) : type;
    }

    /// <pre>{@code return this.mob.isBaby() && height > super.getMobJumpHeight()}</pre>
    protected final boolean isInvalidHeight(double height) {
        return this.mob.isBaby() && height > Math.max(1.125, this.mob.maxUpStep());
    }

    /// form super
    protected final Node getNodeAndUpdateCostToMax(int x, int y, int z, PathType type, float malus) {
        var node = this.getNode(x, y, z);
        node.type = type;
        node.costMalus = Math.max(node.costMalus, malus);
        return node;
    }

    /// form super
    protected final Node getBlockedNode(int x, int y, int z) {
        var node = this.getNode(x, y, z);
        node.type = PathType.BLOCKED;
        node.costMalus = -1.0F;
        return node;
    }

    /// form super
    protected final Node getClosedNode(int x, int y, int z, PathType type) {
        var node = this.getNode(x, y, z);
        node.closed = true;
        node.type = type;
        node.costMalus = type.getMalus();
        return node;
    }

    /// form super
    protected final boolean canReachWithoutCollision(Node node) {
        var box = this.mob.getBoundingBox();
        var pos = this.mob.position();
        var direction = new Vec3(
                node.x - pos.x + box.getXsize() / 2.0,
                node.y - pos.y + box.getYsize() / 2.0,
                node.z - pos.y + box.getZsize() / 2.0
        );
        int size = Mth.ceil(direction.length() / box.getSize());
        direction = direction.scale(1.0F / size);
        for (int j = 1; j <= size; j++) {
            box = box.move(direction);
            if (this.hasCollisions(box)) return false;
        }
        return true;
    }

    /// form super
    protected final boolean hasCollisions(AABB box) {
        return this.collisionCache.computeIfAbsent(box, this::hasCollisionsImpl);
    }

    private boolean hasCollisionsImpl(AABB box) {
        return !this.currentContext.level().noCollision(this.mob, box);
    }

    /// form super
    protected final @Nullable Node tryFindFirstNonWaterBelow(int x, int y, int z, @Nullable Node node) {
        int min = this.mob.level().getMinY();
        while (--y > min) {
            var type = this.getCachedPathType(x, y, z);
            if (type != PathType.WATER) return node;
            node = this.getNodeAndUpdateCostToMax(x, y, z, type, this.mob.getPathfindingMalus(type));
        }
        return node;
    }

    /// form super
    protected final Node tryFindFirstGroundNodeBelow(int x, int y, int z) {
        var mob = this.mob;
        for (int i = y - 1, min = mob.level().getMinY(); i >= min; i--) {
            if (y - i > mob.getMaxFallDistance()) return this.getBlockedNode(x, i, z);
            var type = this.getCachedPathType(x, i, z);
            if (type != PathType.OPEN) {
                float cost = mob.getPathfindingMalus(type);
                return cost < 0.0F
                        ? this.getBlockedNode(x, i, z)
                        : this.getNodeAndUpdateCostToMax(x, i, z, type, cost);
            }
        }
        return this.getBlockedNode(x, y, z);
    }
}
