package net.dragonmounts.neo.common.client.model.dragon;

import net.dragonmounts.neo.common.client.ClientUtil;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDefinition;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static net.dragonmounts.neo.common.DragonMountsShared.makeId;
import static net.dragonmounts.neo.common.client.ClientUtil.scaledPose;
import static net.dragonmounts.neo.common.client.model.dragon.DragonModel.*;
import static net.dragonmounts.neo.common.client.model.dragon.ModelMagic.*;
import static net.dragonmounts.neo.common.entity.dragon.DragonModelContracts.*;
import static net.minecraft.client.model.geom.PartPose.*;
import static net.minecraft.util.Mth.DEG_TO_RAD;

public enum BuiltinFactory implements ModelFactory {
    NORMAL("normal"),
    @Deprecated
    COMPAT("compat") {
        @Override
        public CubeListBuilder applyWingUV(CubeListBuilder builder) {
            return builder.texOffs(-49, 176);
        }
    },
    @Deprecated
    COMPAT_TAIL_HORNED("compat_tail_horned") {
        @Override
        public CubeListBuilder applyWingUV(CubeListBuilder builder) {
            return builder.texOffs(-49, 176);
        }

        @Override
        public void makeTail(PartDefinition root) {
            makeHornedTail(root);
        }
    },
    TAIL_HORNED("tail_horned") {
        @Override
        public void makeTail(PartDefinition root) {
            makeHornedTail(root);
        }
    },
    TAIL_SCALE_INCLINED("tail_scale_inclined") {
        @Override
        public void makeTail(PartDefinition root) {
            var tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), offset(0.0F, 16.0F, 62.0F));
            var segment = CubeListBuilder.create()
                    .texOffs(152, 88)
                    .addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE)
                    .getCubes();
            var scale = CubeListBuilder.create()
                    .texOffs(0, 0)
                    .addBox(-1, -8, -3, 2, 4, 6, ATTACHED_TO_BOTTOM)
                    .getCubes();
            var left = new PartDefinition(scale, rotation(0.0F, 0.0F, 45F * DEG_TO_RAD));
            var right = new PartDefinition(scale, rotation(0.0F, 0.0F, -45F * DEG_TO_RAD));
            for (int i = 0; i < TAIL_SEGMENTS; ++i) {
                var part = tail.addOrReplaceChild(
                        ClientUtil.toString(i),
                        new PartDefinition(segment, scaledPose(calcTailSize(i)))
                );
                part.addOrReplaceChild("left_scale", left);
                part.addOrReplaceChild("right_scale", right);
            }
        }
    },
    SCALE_SHARPENED("scale_sharpened") {
        static final float TAN_10_DEG = (float) Math.tan(10 * DEG_TO_RAD);
        static final float TAN_15_DEG = (float) Math.tan(15 * DEG_TO_RAD);

        static CubeListBuilder buildBackScale(float offset) {
            return CubeListBuilder.create().texOffs(0, 27).addBox(0, -12, offset, 0, 12, 22);
        }

        static List<CubeDefinition> attachTailScale(CubeListBuilder builder) {
            return builder.texOffs(0, 29).addBox(0, -14, -5, 0, 9, 10).getCubes();
        }

        static void attachTailHorn(PartDefinition segment, float width, float length, float offset, int u, int v) {
            segment.addOrReplaceChild("left_horn", new PartDefinition(
                    CubeListBuilder.create().mirror()
                            .texOffs(u, v)
                            .addBox(TAIL_HORN_OFFSET - width, TAIL_HORN_OFFSET + 1.5F, offset, width, 0.0F, length, TOP_SURFACE)
                            .texOffs(0, 117)
                            .addBox(TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, HORN_THICK, HORN_THICK, TAIL_HORN_LENGTH, ATTACHED_TO_NORTH)
                            .getCubes(),
                    offsetAndRotation(0.0F, TAIL_HORN_OFFSET, -HALF_TAIL_SIZE, TAIL_HORN_ROT_X, TAIL_HORN_ROT_Y, 0.0F)
            ));
            segment.addOrReplaceChild("right_horn", new PartDefinition(
                    CubeListBuilder.create()
                            .texOffs(u, v)
                            .addBox(TAIL_HORN_OFFSET + 3.0F, TAIL_HORN_OFFSET + 1.5F, offset, width, 0.0F, length, TOP_SURFACE)
                            .texOffs(0, 117)
                            .addBox(TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, HORN_THICK, HORN_THICK, TAIL_HORN_LENGTH, ATTACHED_TO_NORTH)
                            .getCubes(),
                    offsetAndRotation(0.0F, TAIL_HORN_OFFSET, -HALF_TAIL_SIZE, TAIL_HORN_ROT_X, -TAIL_HORN_ROT_Y, 0.0F)
            ));
        }

        @Override
        public PartDefinition makeBody(PartDefinition root) {
            var body = root.addOrReplaceChild(
                    "body",
                    buildBackScale(5.0F)
                            .texOffs(0, 0)
                            .addBox(-12, 0, -16, 24, 24, 64),
                    offset(0, 4, 8)
            );
            body.addOrReplaceChild(
                    "back",
                    buildBackScale(-22.0F),
                    offsetAndRotation(0.0F, 0.0F, 5.0F + 9.0F * TAN_10_DEG, 10.0F * DEG_TO_RAD, 0.0F, 0.0F)
            );
            body.addOrReplaceChild(
                    "scale",
                    buildBackScale(0.0F),
                    offsetAndRotation(0.0F, 0.0F, 27.0F - 9.0F * TAN_15_DEG, -15.0F * DEG_TO_RAD, 0.0F, 0.0F)
            );
            return body;
        }

        @Override
        public void makeNeck(PartDefinition root) {
            var neck = root.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.ZERO);
            var base = CubeListBuilder.create()
                    .texOffs(112, 88)
                    .addBox(-5, -5, -5, NECK_SIZE, NECK_SIZE, NECK_SIZE)
                    .getCubes();
            for (int i = 0; i < NECK_SEGMENTS; ++i) {
                float scale = calcNeckSize(i);
                neck.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(base, scaledPose(scale, scale, 0.6F)));
            }
            var cubes = CubeListBuilder.create()
                    .texOffs(0, 29)
                    .addBox(0, -10, -5, 0, 9, NECK_SIZE)
                    .getCubes();
            var pose = new PartPose(0.0F, -4.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.0F, 0.6F, 1.0F);
            neck.getChild("3").addOrReplaceChild("scale", new PartDefinition(cubes, pose));
            neck.getChild("5").addOrReplaceChild("scale", new PartDefinition(cubes, pose));
        }

        @Override
        public void makeTail(PartDefinition root) {
            var builder = CubeListBuilder.create();
            var tail = root.addOrReplaceChild("tail", builder, offset(0.0F, 16.0F, 62.0F));
            var base = builder.texOffs(152, 88)
                    .addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE)
                    .getCubes();
            tail.addOrReplaceChild("0", new PartDefinition(base, scaledPose(calcTailSize(0)))).addOrReplaceChild("scale", new PartDefinition(
                    attachTailScale(CubeListBuilder.create()),
                    offsetAndRotation(0.0F, 4.0F, 0.0F, 12.5F * DEG_TO_RAD, 0.0F, 0.0F)
            ));
            tail.addOrReplaceChild("1", new PartDefinition(base, scaledPose(calcTailSize(1)))).addOrReplaceChild("scale", new PartDefinition(
                    attachTailScale(CubeListBuilder.create()),
                    offsetAndRotation(0.0F, 2.0F, 0.0F, 2.5F * DEG_TO_RAD, 0.0F, 0.0F)
            ));
            var segment = attachTailScale(builder);
            for (int i = 2; i < TAIL_SEGMENTS; ++i) {
                tail.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(segment, scaledPose(calcTailSize(i))));
            }
            attachTailHorn(tail.getChild("6"), 7.0F, 26.0F, TAIL_HORN_OFFSET + 9.0F, 142, 192);
            attachTailHorn(tail.getChild("7"), 5.0F, 22.0F, TAIL_HORN_OFFSET + 10.0F, 136, 192);
            attachTailHorn(tail.getChild("8"), 15.0F, 22.0F, TAIL_HORN_OFFSET + 10.0F, 106, 192);
        }
    },
    SCULK("sculk") {
        @Override
        public PartDefinition makeBody(PartDefinition root) {
            var body = root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create()
                            // body
                            .texOffs(0, 0)
                            .addBox(-12, 0, -16, 24, 24, 64)
                            .texOffs(130, 110)
                            .addBox(-4, 12, -5, 8, 6, 15)
                            // scales
                            .texOffs(0, 32)
                            .addBox(-1, -6, 10, 2, 6, 12, ATTACHED_TO_BOTTOM)
                            .addBox(-1, -6, 30, 2, 6, 12, ATTACHED_TO_BOTTOM),
                    offset(0, 4, 8)
            );
            body.addOrReplaceChild(
                    "back",
                    CubeListBuilder.create()
                            .texOffs(0, 32)
                            .addBox(-1, -6, -10, 2, 6, 12, ATTACHED_TO_BOTTOM),
                    PartPose.ZERO
            );
            return body;
        }

        @Override
        public void makeTail(PartDefinition root) {
            makeHornedTail(root);
        }

        @Override
        public void makeFrontLegs(PartDefinition root) {
            makeFrontLeg(root, "left_front_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, true, offset(11, 18, 4));
            makeFrontLeg(root, "right_front_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, false, offset(-11, 18, 4));
        }

        @Override
        public void makeHindLegs(PartDefinition root) {
            makeHindLeg(root, "left_hind_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, true, offset(11, 13, 46));
            makeHindLeg(root, "right_hind_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, false, offset(-11, 13, 46));
        }
    },
    SKELETON("skeleton") {
        static CubeListBuilder makeTail(CubeListBuilder builder) {
            return builder.texOffs(24, 108)
                    .addBox(-3, -4, -5, 6, 5, 10)
                    .texOffs(0, 0)
                    .addBox(-1, -8, -3, 2, 4, 6, ATTACHED_TO_BOTTOM);
        }

        static CubeListBuilder attachTailBone(CubeListBuilder builder, float offset) {
            return builder.mirror()
                    .texOffs(48, 110)
                    .addBox(-3 - offset, -2, -2, 2, 2, 4, ATTACHED_TO_WEST)
                    .mirror(false)
                    .addBox(1 + offset, -2, -2, 2, 2, 4, ATTACHED_TO_WEST);
        }

        @Override
        public PartDefinition makeBody(PartDefinition root) {
            var body = root.addOrReplaceChild(
                    "body",
                    CubeListBuilder.create()
                            // body
                            .texOffs(0, 0)
                            .addBox(-12, 0, -16, 24, 24, 64, LAYER_DEFORMATION)
                            // scales
                            .texOffs(0, 32)
                            .addBox(-1, -6, 10, 2, 6, 12, ATTACHED_TO_BOTTOM)
                            .addBox(-1, -6, 30, 2, 6, 12, ATTACHED_TO_BOTTOM)
                            // ribs
                            .texOffs(128, 112)
                            .addBox(-11.5F, 1.5F, -13, 23, 18, 40)
                            // spines
                            .texOffs(144, 176)
                            .addBox(-4, 0, -15.5F, 8, 8, 21, ATTACHED_TO_SOUTH)
                            .addBox(-4, 0, 5.5F, 8, 8, 21, SPINE_SURFACE)
                            .addBox(-4, 0, 26.5F, 8, 8, 21, ATTACHED_TO_NORTH)
                            // sternum
                            .texOffs(176, 192)
                            .addBox(-1.5F, 18, -15, 3, 5, 29)
                            .texOffs(176, 52)
                            .addBox(-3.5F, 17, -13, 7, 4, 29)
                            // heart
                            .texOffs(112, 128)
                            .addBox(-4, 12, -9, 8, 6, 15)
                            // shoulders
                            .texOffs(112, 112)
                            .addBox(-11, 0, -14, 7, 3, 13, ATTACHED_TO_EAST)
                            .mirror()
                            .addBox(4, 0, -14, 7, 3, 13, ATTACHED_TO_EAST)
                            .texOffs(72, 110)
                            .addBox(7, 1, -15, 5, 12, 10)
                            .mirror(false)
                            .addBox(-12, 1, -15, 5, 12, 10)
                            // hips
                            .texOffs(72, 132)
                            .addBox(-11, 0, 32, 7, 12, 13)
                            .mirror()
                            .addBox(4, 0, 32, 7, 12, 13),
                    offset(0, 4, 8)
            );
            body.addOrReplaceChild(
                    "back",
                    CubeListBuilder.create()
                            .texOffs(0, 32)
                            .addBox(-1, -6, -10, 2, 6, 12, ATTACHED_TO_BOTTOM),
                    PartPose.ZERO
            );
            return body;
        }

        @Override
        public void makeHead(PartDefinition root) {
            makeHead(root, CubeListBuilder.create());
        }

        @Override
        public void makeNeck(PartDefinition root) {
            var builder = CubeListBuilder.create();
            var neck = root.addOrReplaceChild("neck", builder, PartPose.ZERO);
            var base = builder
                    .texOffs(112, 88)
                    .addBox(-5, -5, -5, 10, 9, 6, LAYER_DEFORMATION)
                    .texOffs(0, 108)
                    .addBox(-3, -5, -5, 6, 7, 6)
                    .texOffs(0, 108)
                    .addBox(3, -2, -3, 1, 2, 2, ATTACHED_TO_WEST)
                    .mirror()
                    .addBox(-4, -2, -3, 1, 2, 2, ATTACHED_TO_WEST)
                    .getCubes();
            var scaled = builder.mirror(false)
                    .texOffs(0, 10)
                    .addBox(-1, -7, -3, 2, 2, 3, ATTACHED_TO_BOTTOM)
                    .getCubes();
            for (int i = 0; i < NECK_SEGMENTS; ++i) {
                float scale = calcNeckSize(i);
                neck.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(i == 2 || i == 4 ? scaled : base, scaledPose(scale, scale, 1.0F)));
            }
        }

        @Override
        public void makeTail(PartDefinition root) {
            var builder = CubeListBuilder.create();
            var tail = root.addOrReplaceChild("tail", builder, offset(0.0F, 16.0F, 60.0F));
            tail.addOrReplaceChild("11", makeTail(builder), scaledPose(calcTailSize(11)));
            var segment = attachTailBone(builder, 2.0F)
                    .texOffs(152, 88)
                    .addBox(-5.5F, -5, -5, 11, 9, 10, LAYER_DEFORMATION)
                    .getCubes();
            var bone = CubeListBuilder.create()
                    .texOffs(24, 108)
                    .addBox(-1, 0, -1, 2, 4, 2, ATTACHED_TO_TOP)
                    .getCubes();
            var pose = rotation(-10.0F * DEG_TO_RAD, 0.0F, 0.0F);
            for (int i = 0; i < 5; ++i) {
                tail.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(segment, scaledPose(calcTailSize(i))))
                        .addOrReplaceChild("bone", new PartDefinition(bone, pose));
            }
            tail.addOrReplaceChild("5", new PartDefinition(segment, scaledPose(calcTailSize(5))))
                    .addOrReplaceChild("bone", new PartDefinition(bone, pose.translated(0.0F, -0.5F, 0.0F)));
            tail.addOrReplaceChild(
                    "6",
                    attachTailBone(makeTail(CubeListBuilder.create()), 1.0F)
                            .texOffs(152, 88)
                            .addBox(-5.5F, -5, -5, 11, 9, 10, LAYER_DEFORMATION),
                    scaledPose(calcTailSize(6))
            ).addOrReplaceChild("bone", new PartDefinition(bone, pose.translated(0.0F, -1.0F, 0.0F)));
            segment = attachTailBone(makeTail(CubeListBuilder.create()), 1.0F)
                    .texOffs(0, 123)
                    .addBox(-4.5F, -5, -5, 9, 7, 10, LAYER_DEFORMATION)
                    .getCubes();
            tail.addOrReplaceChild("7", new PartDefinition(segment, scaledPose(calcTailSize(7))))
                    .addOrReplaceChild("bone", new PartDefinition(bone, pose.translated(0.0F, -1.5F, 0.0F)));
            tail.addOrReplaceChild("8", new PartDefinition(segment, scaledPose(calcTailSize(8))))
                    .addOrReplaceChild("bone", new PartDefinition(bone, pose.translated(0.0F, -2.0F, 0.0F)));
            tail.addOrReplaceChild("9", new PartDefinition(segment, scaledPose(calcTailSize(9))));
            tail.addOrReplaceChild(
                    "10",
                    makeTail(CubeListBuilder.create())
                            .texOffs(38, 123)
                            .addBox(-3.5F, -5, -5, 7, 6, 10, LAYER_DEFORMATION),
                    scaledPose(calcTailSize(10))
            );
            assert tail.getChildren().size() == TAIL_SEGMENTS;
        }

        @Override
        public void makeFrontLegs(PartDefinition root) {
            makeFrontLeg(root, "left_front_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, true, offset(11, 18, 4));
            makeFrontLeg(root, "right_front_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, false, offset(-11, 18, 4));
        }

        @Override
        public void makeHindLegs(PartDefinition root) {
            makeHindLeg(root, "left_hind_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, true, offset(11, 13, 46));
            makeHindLeg(root, "right_hind_leg", SKELETON_LEG_WIDTH, LEG_LENGTH, false, offset(-11, 13, 46));
        }

        @Override
        public void makeChest(PartDefinition body) {
            body.addOrReplaceChild(
                    "chest",
                    CubeListBuilder.create()
                            .texOffs(192, 132)
                            .addBox(12, 0, 21, 4, 12, 12)
                            .texOffs(224, 132)
                            .addBox(-16, 0, 21, 4, 12, 12),
                    PartPose.ZERO
            );
        }

        @Override
        public void makeSaddle(PartDefinition body) {
            body.addOrReplaceChild(
                    "saddle",
                    CubeListBuilder.create()
                            .texOffs(184, 98)
                            .addBox(-7, -2, -15, 15, 3, 20)
                            .texOffs(214, 120)
                            .addBox(-3, -3, -14, 6, 1, 2, ATTACHED_TO_BOTTOM)
                            .addBox(-6, -4, 2, 13, 2, 2, ATTACHED_TO_BOTTOM)
                            .texOffs(220, 100)
                            .addBox(12, 0, -14, 1, 14, 2, ATTACHED_TO_BOTTOM)
                            .addBox(-13, 0, -14, 1, 10, 2, ATTACHED_TO_BOTTOM)
                            .texOffs(224, 132)
                            .addBox(12, 14, -15, 1, 5, 4)
                            .addBox(-13, 10, -15, 1, 5, 4),
                    PartPose.ZERO
            );
        }
    };
    public static final int NORMAL_LEG_WIDTH = 9;
    public static final int SKELETON_LEG_WIDTH = 7;
    public static final Set<Direction> TOP_SURFACE = Collections.singleton(Direction.DOWN);
    public static final Set<Direction> EAST_STRAP_SURFACE = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST);
    public static final Set<Direction> WEST_STRAP_SURFACE = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static final Set<Direction> SPINE_SURFACE = EnumSet.of(Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_BOTTOM = EnumSet.of(Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_TOP = EnumSet.of(Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_NORTH = EnumSet.of(Direction.DOWN, Direction.UP, Direction.SOUTH, Direction.WEST, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_SOUTH = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.WEST, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_WEST = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.EAST);
    public static final Set<Direction> ATTACHED_TO_EAST = EnumSet.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST);
    public static final CubeDeformation LAYER_DEFORMATION = new CubeDeformation(0.1F);
    public final ModelLayerLocation location;

    BuiltinFactory(String name) {
        this.location = new ModelLayerLocation(makeId("dragon"), name);
    }

    public static float calcNeckSize(int index) {
        return Mth.lerp((index + 1) / (float) NECK_SEGMENTS, 1.6F, 1.0F);
    }

    public static float calcTailSize(int index) {
        return Mth.lerp((index + 1) / (float) TAIL_SEGMENTS, 1.5F, 0.3F);
    }

    public static void makeHead(PartDefinition root, CubeListBuilder head) {
        var part = root.addOrReplaceChild(
                "head",
                head.texOffs(0, 0)
                        .addBox(-8.0F, -8.0F, 6.0F + HEAD_OFS, HEAD_SIZE, HEAD_SIZE, HEAD_SIZE)
                        // upper jaw
                        .texOffs(56, 88)
                        .addBox(-6.0F, -1.0F, -8.0F + HEAD_OFS, JAW_WIDTH, JAW_HEIGHT, JAW_LENGTH, ATTACHED_TO_SOUTH),
                PartPose.ZERO
        );
        part.addOrReplaceChild(
                "left_horn",
                CubeListBuilder.create().mirror()
                        .addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HEAD_HORN_LENGTH, 28, 32),
                offsetAndRotation(-5, -8, 0, 30.0F * DEG_TO_RAD, -30.0F * DEG_TO_RAD, 0)
        );
        part.addOrReplaceChild(
                "right_horn",
                CubeListBuilder.create()
                        .addBox("horn", HORN_OFS, HORN_OFS, HORN_OFS, HORN_THICK, HORN_THICK, HEAD_HORN_LENGTH, 28, 32)
                        .mirror(),
                offsetAndRotation(5, -8, 0, 30.0F * DEG_TO_RAD, 30.0F * DEG_TO_RAD, 0)
        );
        part.addOrReplaceChild(
                "jaw",
                CubeListBuilder.create()
                        .texOffs(0, 88)
                        // lower jaw
                        .addBox(-6.0F, 0.0F, -16.0F, 12, 4, 16, ATTACHED_TO_SOUTH),
                offset(0.0F, 4.0F, 8.0F + HEAD_OFS)
        );
    }

    public static void makeHornedTail(PartDefinition root) {
        var tail = root.addOrReplaceChild("tail", CubeListBuilder.create(), offset(0.0F, 16.0F, 62.0F));
        var segment = CubeListBuilder.create()
                .texOffs(152, 88)
                .addBox(-5, -5, -5, TAIL_SIZE, TAIL_SIZE, TAIL_SIZE)
                .getCubes();
        var left = new PartDefinition(
                CubeListBuilder.create().mirror()
                        .texOffs(0, 117)
                        .addBox(TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, HORN_THICK, HORN_THICK, TAIL_HORN_LENGTH, ATTACHED_TO_NORTH)
                        .getCubes(),
                offsetAndRotation(0.0F, TAIL_HORN_OFFSET, -HALF_TAIL_SIZE, TAIL_HORN_ROT_X, TAIL_HORN_ROT_Y, 0.0F)
        );
        var right = new PartDefinition(
                CubeListBuilder.create()
                        .texOffs(0, 117)
                        .addBox(TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, TAIL_HORN_OFFSET, HORN_THICK, HORN_THICK, TAIL_HORN_LENGTH, ATTACHED_TO_NORTH)
                        .getCubes(),
                offsetAndRotation(0.0F, TAIL_HORN_OFFSET, -HALF_TAIL_SIZE, TAIL_HORN_ROT_X, -TAIL_HORN_ROT_Y, 0.0F)
        );
        for (int i = 0; i < TAIL_SEGMENTS; ++i) {
            var part = tail.addOrReplaceChild(ClientUtil.toString(i), new PartDefinition(segment, scaledPose(calcTailSize(i))));
            if (i + 7 > TAIL_SEGMENTS && i + 3 < TAIL_SEGMENTS) {
                part.addOrReplaceChild("left_horn", left);
                part.addOrReplaceChild("right_horn", right);
            }
        }
    }

    public static void makeFrontLeg(
            PartDefinition root,
            String name,
            int width,
            int length,
            boolean mirror,
            PartPose pose
    ) {
        int thighLength = (int) (length * 0.77F);
        int shankLength = (int) (length * 0.80F);
        int footLength = (int) (length * 0.34F);
        int toeLength = (int) (length * 0.33F);
        int shankWidth = width - 2;
        float thighOffset = width * -0.5F;
        float shankOffset = shankWidth * -0.5F;
        float footOffsetY = FOOT_HEIGHT * -0.5F;
        float footOffsetZ = (int) (length * 0.34F) * -0.75F;
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().mirror(mirror).texOffs(112, 0).addBox(
                        thighOffset,
                        thighOffset,
                        thighOffset,
                        width,
                        thighLength,
                        width
                ),
                pose
        ).addOrReplaceChild(
                "shank",
                CubeListBuilder.create().mirror(mirror).texOffs(148, 0).addBox(
                        shankOffset,
                        shankOffset,
                        shankOffset,
                        shankWidth,
                        shankLength,
                        shankWidth
                ),
                offset(0.0F, thighLength + thighOffset, 0.0F)
        ).addOrReplaceChild(
                "foot",
                CubeListBuilder.create().mirror(mirror).texOffs(210, 0).addBox(
                        thighOffset,
                        footOffsetY,
                        footOffsetZ,
                        width,
                        FOOT_HEIGHT,
                        footLength
                ),
                offset(0.0F, shankLength + shankOffset * 0.5F, 0.0F)
        ).addOrReplaceChild(
                "toe",
                CubeListBuilder.create().mirror(mirror).texOffs(176, 0).addBox(
                        thighOffset,
                        footOffsetY,
                        -toeLength,
                        width,
                        FOOT_HEIGHT,
                        toeLength
                ),
                offset(0.0F, 0.0F, footOffsetZ - footOffsetY * 0.5F)
        );
    }

    public static void makeHindLeg(
            PartDefinition root,
            String name,
            int width,
            int length,
            boolean mirror,
            PartPose pose
    ) {
        int thighLength = (int) (length * 0.90F);
        int shankLength = (int) (length * 0.70F) - 2;
        int footLength = (int) (length * 0.67F);
        int toeLength = (int) (length * 0.27F);
        int thighWidth = width + 1;
        int shankWidth = width - 2;
        float thighOffset = thighWidth * -0.5F;
        float shankOffset = shankWidth * -0.5F;
        float footOffsetY = FOOT_HEIGHT * -0.5F;
        float footOffsetZ = footLength * -0.75F;
        root.addOrReplaceChild(
                name,
                CubeListBuilder.create().mirror(mirror).texOffs(112, 29).addBox(
                        thighOffset,
                        thighOffset,
                        thighOffset,
                        thighWidth,
                        thighLength,
                        thighWidth
                ),
                pose
        ).addOrReplaceChild(
                "shank",
                CubeListBuilder.create().mirror(mirror).texOffs(152, 29).addBox(
                        shankOffset,
                        shankOffset,
                        shankOffset,
                        shankWidth,
                        shankLength,
                        shankWidth
                ),
                offset(0.0F, thighLength + thighOffset, 0.0F)
        ).addOrReplaceChild(
                "foot",
                CubeListBuilder.create().mirror(mirror).texOffs(180, 29).addBox(
                        thighOffset,
                        footOffsetY,
                        footOffsetZ,
                        width,
                        FOOT_HEIGHT,
                        footLength
                ),
                offset(0.0F, shankLength + shankOffset * 0.5F, 0.0F)
        ).addOrReplaceChild(
                "toe",
                CubeListBuilder.create().mirror(mirror).texOffs(215, 29).addBox(
                        thighOffset,
                        footOffsetY,
                        -toeLength,
                        width,
                        FOOT_HEIGHT,
                        toeLength
                ),
                offset(0.0F, 0.0F, footOffsetZ - footOffsetY * 0.5F)
        );
    }
}
