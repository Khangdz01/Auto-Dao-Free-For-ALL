package com.automine.core;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/* JADX INFO: loaded from: AutoMinePlan.class */
@Environment(EnvType.CLIENT)
public final class AutoMinePlan {
    public final AutoMineSelection selection;
    public final int layerHeight;
    public final int passWidth;
    public final int minX;
    public final int minY;
    public final int minZ;
    public final int maxX;
    public final int maxY;
    public final int maxZ;
    public final Direction.Axis travelAxis;
    public final boolean crossMirrored;
    public final boolean travelMirrored;
    public final int travelMin;
    public final int travelMax;
    public final int crossMin;
    public final int crossMax;
    public final int bottomY;
    public final int topY;
    public final int layerCount;
    public final int passesPerLayer;
    public final int stepsPerPass;
    public int currentLayer;
    public int currentPass;
    public int currentStep;
    public int currentCell;
    public boolean layerDone;
    public boolean isDone;

    /* JADX WARN: Multi-variable type inference failed */
    public AutoMinePlan(AutoMineSelection autoMineSelection, int i, int i2) {
        this(autoMineSelection, i, i2, false, false);
    }

    public AutoMinePlan(AutoMineSelection autoMineSelection, int i, int i2, boolean z, boolean z2) {
        this.selection = autoMineSelection;
        this.layerHeight = Math.max(1, i);
        this.passWidth = Math.max(1, i2);
        this.crossMirrored = z;
        this.travelMirrored = z2;
        this.minX = autoMineSelection.minX();
        this.minY = autoMineSelection.minY();
        this.minZ = autoMineSelection.minZ();
        this.maxX = autoMineSelection.maxX();
        this.maxY = autoMineSelection.maxY();
        this.maxZ = autoMineSelection.maxZ();
        this.travelAxis = autoMineSelection.sizeX() >= autoMineSelection.sizeZ() ? Direction.Axis.X : Direction.Axis.Z;
        if (this.travelAxis == Direction.Axis.X) {
            this.travelMin = autoMineSelection.minX();
            this.travelMax = autoMineSelection.maxX();
            this.crossMin = autoMineSelection.minZ();
            this.crossMax = autoMineSelection.maxZ();
        } else {
            this.travelMin = autoMineSelection.minZ();
            this.travelMax = autoMineSelection.maxZ();
            this.crossMin = autoMineSelection.minX();
            this.crossMax = autoMineSelection.maxX();
        }
        this.bottomY = autoMineSelection.minY();
        this.topY = autoMineSelection.maxY();
        this.layerCount = ceilDiv((this.topY - this.bottomY) + (1), this.layerHeight);
        this.passesPerLayer = ceilDiv((this.crossMax - this.crossMin) + (1), this.passWidth);
        this.stepsPerPass = (this.travelMax - this.travelMin) + (1);
        if (isFullFace()) {
            return;
        }
        advance();
    }

    public static int ceilDiv(int i, int i2) {
        return ((i + i2) - (1)) / i2;
    }

    public int layerTop() {
        return this.topY - (this.currentLayer * this.layerHeight);
    }

    public int layerBottom() {
        return Math.max(this.bottomY, layerTop() - (this.layerHeight - (1)));
    }

    public int effectivePassIndex() {
        return ((this.currentLayer % 2 == 0) != this.crossMirrored) ? this.currentPass : (this.passesPerLayer - 1) - this.currentPass;
    }

    public int rowCenter() {
        return Math.min(this.crossMax, this.crossMin + (this.passWidth / (2)) + (effectivePassIndex() * this.passWidth));
    }

    public int rowMin() {
        return Math.max(this.crossMin, rowCenter() - (this.passWidth / (2)));
    }

    public int rowMax() {
        return Math.min(this.crossMax, (rowMin() + this.passWidth) - (1));
    }

    public int rowDirection() {
        int i = ((this.currentLayer * this.passesPerLayer) + this.currentPass) % (2) == 0 ? 1 : -1;
        return this.travelMirrored ? -i : i;
    }

    public int currentTravel() {
        return rowDirection() > 0 ? this.travelMin + this.currentStep : this.travelMax - this.currentStep;
    }

    public int faceWidth() {
        return (rowMax() - rowMin()) + (1);
    }

    public int faceHeight() {
        return (layerTop() - layerBottom()) + (1);
    }

    public int cellsInFace() {
        return faceWidth() * faceHeight();
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean isFullFace() {
        return (faceWidth() == this.passWidth && faceHeight() == this.layerHeight) ? true : false;
    }

    public List<BlockPos> faceCells() {
        BlockPos faceCenterPos = faceCenter();
        int iCurrentTravel = currentTravel();
        int coordZ = this.travelAxis == Direction.Axis.X ? faceCenterPos.getZ() : faceCenterPos.getX();
        int coordY = faceCenterPos.getY();
        ArrayList arrayList = new ArrayList(9);
        arrayList.add(posAt(iCurrentTravel, coordZ, coordY));
        arrayList.add(posAt(iCurrentTravel, coordZ, coordY + (1)));
        arrayList.add(posAt(iCurrentTravel, coordZ, coordY - (1)));
        arrayList.add(posAt(iCurrentTravel, coordZ - (1), coordY));
        arrayList.add(posAt(iCurrentTravel, coordZ + (1), coordY));
        arrayList.add(posAt(iCurrentTravel, coordZ - (1), coordY + (1)));
        arrayList.add(posAt(iCurrentTravel, coordZ + (1), coordY + (1)));
        arrayList.add(posAt(iCurrentTravel, coordZ - (1), coordY - (1)));
        arrayList.add(posAt(iCurrentTravel, coordZ + (1), coordY - (1)));
        return arrayList;
    }

    public BlockPos faceCenter() {
        int centerY = faceCenterY();
        return posAt(currentTravel(), aimCross(), faceHeight() == (1) ? centerY : clampInner(centerY, this.bottomY, this.topY));
    }

    public int aimCross() {
        int iRowCenter = rowCenter();
        return faceWidth() == (1) ? iRowCenter : clampInner(iRowCenter, this.crossMin, this.crossMax);
    }

    public static int clampInner(int i, int i2, int i3) {
        return i3 - i2 < (2) ? i : Math.max(i2 + (1), Math.min(i3 - (1), i));
    }

    public int faceCenterY() {
        return layerBottom() + ((layerTop() - layerBottom()) / (2));
    }

    public BlockPos standPos() {
        int iCurrentTravel = currentTravel() - rowDirection();
        if (iCurrentTravel < this.travelMin) {
            iCurrentTravel = this.travelMin;
        } else if (iCurrentTravel > this.travelMax) {
            iCurrentTravel = this.travelMax;
        }
        return posAt(iCurrentTravel, aimCross(), layerBottom());
    }

    public BlockPos entryPos() {
        return posAt(currentTravel(), aimCross(), layerBottom());
    }

    public BlockPos posAt(int i, int i2, int i3) {
        return this.travelAxis == Direction.Axis.X ? new BlockPos(i, i3, i2) : new BlockPos(i2, i3, i);
    }

    public boolean isDone() {
        return this.isDone;
    }

    public boolean areLayerFacesDone() {
        return this.layerDone;
    }

    public void advance() {
        if (this.layerDone) {
            return;
        }
        this.currentStep++;
        if (this.currentStep >= this.stepsPerPass) {
            this.currentStep = 0;
            this.currentPass++;
            if (this.currentPass >= this.passesPerLayer) {
                this.layerDone = true;
            }
        }
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean nextLayer() {
        this.currentLayer += 1;
        if (this.currentLayer >= this.layerCount) {
            this.isDone = true;
            return false;
        }
        this.currentPass = 0;
        this.currentStep = 0;
        this.currentCell = 0;
        this.layerDone = false;
        if (!isFullFace()) {
            advance();
        }
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void restartLayer() {
        this.currentPass = 0;
        this.currentStep = 0;
        this.currentCell = 0;
        this.layerDone = false;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public void restartFromTop() {
        this.currentLayer = 0;
        this.isDone = false;
        restartLayer();
        if (isFullFace()) {
            return;
        }
        advance();
    }

    public int minTravel() {
        return this.travelMin;
    }

    public int maxTravel() {
        return this.travelMax;
    }

    public int minCross() {
        return this.crossMin;
    }

    public int maxCross() {
        return this.crossMax;
    }

    public int layerIndex() {
        return this.currentLayer;
    }

    public int layerCount() {
        return this.layerCount;
    }

    public int rowIndex() {
        return this.currentPass;
    }

    public float progress() {
        if (this.isDone) {
            return 1.0f;
        }
        return (float) Math.min(1.0d, (((double) this.currentLayer) + (this.layerDone ? 1.0d : (((double) this.currentPass) + (((double) this.currentStep) / ((double) Math.max(1, this.stepsPerPass)))) / ((double) Math.max(1, this.passesPerLayer)))) / ((double) this.layerCount));
    }

    public String describe() {
        if (this.layerDone) {
            return "tầng " + (this.currentLayer + (1)) + "/" + this.layerCount + " · đang kiểm tra sót";
        }
        return "tầng " + (this.currentLayer + (1)) + "/" + this.layerCount + " · hàng " + (effectivePassIndex() + (1)) + "/" + this.passesPerLayer + " · " + (this.currentStep + (1)) + "/" + this.stepsPerPass + " · mặt " + faceWidth() + "x" + faceHeight() + "=" + cellsInFace() + " ô · trục " + (this.travelAxis == Direction.Axis.X ? "X" : "Z") + ((this.crossMirrored || this.travelMirrored) ? " · từ góc gần mình" : "") + (this.currentCell == (1) ? " · vét rìa" : "");
    }

    public AutoMineSelection selection() {
        return this.selection;
    }

    /* JADX WARN: Multi-variable type inference failed */
    public boolean matchesSelection(AutoMineSelection autoMineSelection) {
        if (autoMineSelection == null || !autoMineSelection.isComplete()) {
            return false;
        }
        return (this.minX == autoMineSelection.minX() && this.minY == autoMineSelection.minY() && this.minZ == autoMineSelection.minZ() && this.maxX == autoMineSelection.maxX() && this.maxY == autoMineSelection.maxY() && this.maxZ == autoMineSelection.maxZ()) ? true : false;
    }
}
