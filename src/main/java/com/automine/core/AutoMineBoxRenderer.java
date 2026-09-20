package com.automine.core;

import com.automine.AutoMineClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.render.RenderLayers;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import org.joml.Matrix4f;

@Environment(EnvType.CLIENT)
public final class AutoMineBoxRenderer {
    public static final int COLOR_ORANGE = -32768; // 0xFFFF8000 (Orange)
    public static final int COLOR_AMBER = -50384;
    public static final int COLOR_WHITE = -1;
    public static RenderLayer cachedLinesLayer;

    public AutoMineBoxRenderer() {
    }

    private static boolean registered = false;
    public static synchronized void register() {
        if (registered) return;
        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(AutoMineBoxRenderer::renderBoxes);
    }

    public static RenderLayer getLinesLayer() {
        if (cachedLinesLayer != null) {
            return cachedLinesLayer;
        }
        try {
            cachedLinesLayer = RenderLayers.lines();
        } catch (Throwable t) {
            try {
                cachedLinesLayer = RenderLayers.LINES;
            } catch (Throwable ignored) {
            }
        }
        return cachedLinesLayer;
    }

    public static RenderLayer getFilledLayer() {
        try {
            return RenderLayers.debugFilledBox();
        } catch (Throwable t) {
            return null;
        }
    }

    public static void renderBoxes(WorldRenderContext worldRenderContext) {
        if (worldRenderContext == null) return;

        AutoMineSelection selection = AutoMineClient.SELECTION;
        if (selection == null) {
            selection = AutoMineCoreImpl.SELECTION;
        }
        if (selection == null) return;
        if (AutoMineClient.CONFIG != null && !AutoMineClient.CONFIG.renderSelection) return;

        VertexConsumerProvider consumers = worldRenderContext.consumers();
        if (consumers == null) return;

        MatrixStack matrices = worldRenderContext.matrices();
        if (matrices == null) return;

        Vec3d camPos = null;
        try {
            if (worldRenderContext.gameRenderer() != null && worldRenderContext.gameRenderer().getCamera() != null) {
                camPos = worldRenderContext.gameRenderer().getCamera().getCameraPos();
            }
        } catch (Throwable ignored) {
        }
        if (camPos == null) return;

        RenderLayer linesLayer = getLinesLayer();
        if (linesLayer == null) return;

        VertexConsumer vertexConsumer = null;
        try {
            vertexConsumer = consumers.getBuffer(linesLayer);
        } catch (Throwable ignored) {
        }
        if (vertexConsumer == null) return;

        if (selection.isComplete()) {
            int minX = selection.minX();
            int minY = selection.minY();
            int minZ = selection.minZ();
            int sizeX = selection.sizeX();
            int sizeY = selection.sizeY();
            int sizeZ = selection.sizeZ();

            // 1. Draw solid line box outline (color 0xFFFF8000 = orange/gold)
            drawBoxOutline(matrices, vertexConsumer, camPos, minX, minY, minZ, sizeX, sizeY, sizeZ, -32768, 2.5f);

            // 2. Corner markers for Pos1 and Pos2
            BlockPos p1 = selection.pos1();
            if (p1 != null) {
                drawBlockOutline(matrices, vertexConsumer, camPos, p1, 0xFF00FF00, 3.0f); // Bright green
            }
            BlockPos p2 = selection.pos2();
            if (p2 != null) {
                drawBlockOutline(matrices, vertexConsumer, camPos, p2, 0xFF00BFFF, 3.0f); // Bright blue
            }

            // 3. Draw subtle translucent fill so the 3D volume stands out
            try {
                RenderLayer filledLayer = getFilledLayer();
                if (filledLayer != null) {
                    VertexConsumer filledConsumer = consumers.getBuffer(filledLayer);
                    if (filledConsumer != null) {
                        drawFilledBox(matrices, filledConsumer, camPos, minX, minY, minZ, sizeX, sizeY, sizeZ, 1.0f, 0.55f, 0.0f, 0.18f);
                    }
                }
            } catch (Throwable ignored) {
            }
        } else {
            // Draw individual markers if only 1 pos is set
            BlockPos p1 = selection.pos1();
            if (p1 != null) {
                drawBlockOutline(matrices, vertexConsumer, camPos, p1, 0xFF00FF00, 3.0f); // Bright green
            }
            BlockPos p2 = selection.pos2();
            if (p2 != null) {
                drawBlockOutline(matrices, vertexConsumer, camPos, p2, 0xFF00BFFF, 3.0f); // Bright blue
            }
        }

        // Active mining highlights
        AutoMineEngine engine = AutoMineClient.ENGINE;
        if (engine == null) {
            engine = AutoMineCoreImpl.ENGINE;
        }
        if (engine != null && engine.state() == AutoMineState.RUNNING) {
            try {
                BlockPos faceCenter = engine.faceCenter();
                if (engine.faceCells() != null) {
                    for (BlockPos cell : engine.faceCells()) {
                        if (selection.contains(cell)) {
                            boolean isCenter = cell.equals(faceCenter);
                            drawBlockOutline(matrices, vertexConsumer, camPos, cell, isCenter ? 0xFFFF1155 : 0xFF00E5FF, isCenter ? 3.5f : 1.5f);
                        }
                    }
                }
            } catch (Throwable ignored) {
            }
        }

        // CRITICAL: Flush buffers immediately so vertices are dispatched to the GPU
        if (consumers instanceof VertexConsumerProvider.Immediate immediate) {
            try {
                immediate.draw(linesLayer);
            } catch (Throwable ignored) {
            }
            RenderLayer filledLayer = getFilledLayer();
            if (filledLayer != null) {
                try {
                    immediate.draw(filledLayer);
                } catch (Throwable ignored) {
                }
            }
        }
    }

    public static void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d camPos, BlockPos pos, int color, float lineWidth) {
        if (pos == null) return;
        drawBoxOutline(matrices, vertexConsumer, camPos, pos.getX(), pos.getY(), pos.getZ(), 1, 1, 1, color, lineWidth);
    }

    public static void drawBoxOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Vec3d camPos, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ, int color, float lineWidth) {
        if (matrices == null || vertexConsumer == null || camPos == null) return;
        try {
            VertexRendering.drawOutline(
                matrices,
                vertexConsumer,
                VoxelShapes.cuboidUnchecked(0.0d, 0.0d, 0.0d, (double) sizeX, (double) sizeY, (double) sizeZ),
                ((double) minX) - camPos.x,
                ((double) minY) - camPos.y,
                ((double) minZ) - camPos.z,
                color,
                lineWidth
            );
        } catch (Throwable ignored) {
        }
    }

    public static void drawFilledBox(MatrixStack matrices, VertexConsumer consumer, Vec3d camPos, int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ, float r, float g, float b, float a) {
        if (matrices == null || consumer == null || camPos == null) return;
        try {
            Matrix4f mat = matrices.peek().getPositionMatrix();
            float x1 = (float) ((double) minX - camPos.x);
            float y1 = (float) ((double) minY - camPos.y);
            float z1 = (float) ((double) minZ - camPos.z);
            float x2 = x1 + (float) sizeX;
            float y2 = y1 + (float) sizeY;
            float z2 = z1 + (float) sizeZ;

            // Down face
            consumer.vertex(mat, x1, y1, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y1, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y1, z2).color(r, g, b, a);
            consumer.vertex(mat, x1, y1, z2).color(r, g, b, a);

            // Up face
            consumer.vertex(mat, x1, y2, z1).color(r, g, b, a);
            consumer.vertex(mat, x1, y2, z2).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z2).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z1).color(r, g, b, a);

            // North face
            consumer.vertex(mat, x1, y1, z1).color(r, g, b, a);
            consumer.vertex(mat, x1, y2, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y1, z1).color(r, g, b, a);

            // South face
            consumer.vertex(mat, x1, y1, z2).color(r, g, b, a);
            consumer.vertex(mat, x2, y1, z2).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z2).color(r, g, b, a);
            consumer.vertex(mat, x1, y2, z2).color(r, g, b, a);

            // West face
            consumer.vertex(mat, x1, y1, z1).color(r, g, b, a);
            consumer.vertex(mat, x1, y1, z2).color(r, g, b, a);
            consumer.vertex(mat, x1, y2, z2).color(r, g, b, a);
            consumer.vertex(mat, x1, y2, z1).color(r, g, b, a);

            // East face
            consumer.vertex(mat, x2, y1, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z1).color(r, g, b, a);
            consumer.vertex(mat, x2, y2, z2).color(r, g, b, a);
            consumer.vertex(mat, x2, y1, z2).color(r, g, b, a);
        } catch (Throwable ignored) {
        }
    }

    static {
        cachedLinesLayer = getLinesLayer();
    }
}
