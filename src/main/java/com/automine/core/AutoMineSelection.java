package com.automine.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/* JADX INFO: loaded from: AutoMineSelection.class */
@Environment(EnvType.CLIENT)
public final class AutoMineSelection {
    public BlockPos pos1;
    public BlockPos pos2;
    private Runnable onModified;

    public void setOnModified(Runnable onModified) {
        this.onModified = onModified;
    }

    private void notifyModified() {
        if (this.onModified != null) {
            try {
                this.onModified.run();
            } catch (Throwable ignored) {
            }
        }
    }

    public void setPos1(BlockPos pos) {
        this.pos1 = pos != null ? pos.toImmutable() : null;
        notifyModified();
    }

    public void setPos2(BlockPos pos) {
        this.pos2 = pos != null ? pos.toImmutable() : null;
        notifyModified();
    }

    public BlockPos pos1() {
        return this.pos1;
    }

    public BlockPos pos2() {
        return this.pos2;
    }

    public void clear() {
        this.pos1 = null;
        this.pos2 = null;
        notifyModified();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean isComplete() {
        return (this.pos1 == null || this.pos2 == null) ? false : true;
    }

    public int minX() {
        return Math.min(this.pos1.getX(), this.pos2.getX());
    }

    public int minY() {
        return Math.min(this.pos1.getY(), this.pos2.getY());
    }

    public int minZ() {
        return Math.min(this.pos1.getZ(), this.pos2.getZ());
    }

    public int maxX() {
        return Math.max(this.pos1.getX(), this.pos2.getX());
    }

    public int maxY() {
        return Math.max(this.pos1.getY(), this.pos2.getY());
    }

    public int maxZ() {
        return Math.max(this.pos1.getZ(), this.pos2.getZ());
    }

    public int sizeX() {
        return (maxX() - minX()) + (1);
    }

    public int sizeY() {
        return (maxY() - minY()) + (1);
    }

    public int sizeZ() {
        return (maxZ() - minZ()) + (1);
    }

    public long volume() {
        return ((long) sizeX()) * ((long) sizeY()) * ((long) sizeZ());
    }

    public boolean contains(BlockPos pos) {
        return contains(pos, 0);
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean contains(BlockPos pos, int i) {
        return (pos.getX() < minX() - i || pos.getX() > maxX() + i || pos.getY() < minY() - i || pos.getY() > maxY() + i || pos.getZ() < minZ() - i || pos.getZ() > maxZ() + i) ? false : true;
    }

    public Box renderBox() {
        return new Box(minX(), minY(), minZ(), ((double) maxX()) + 1.0d, ((double) maxY()) + 1.0d, ((double) maxZ()) + 1.0d);
    }

    public String describe() {
        if (isComplete()) {
            return sizeX() + "x" + sizeY() + "x" + sizeZ() + " (" + volume() + " block)";
        }
        return (this.pos1 == null && this.pos2 == null) ? "chưa chọn" : "mới có 1 điểm";
    }
}
