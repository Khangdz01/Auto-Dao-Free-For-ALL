package com.automine.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

/* JADX INFO: loaded from: AutoMinePathfinder.class */
@Environment(EnvType.CLIENT)
public final class AutoMinePathfinder {
    public static final Direction[] CARDINAL_DIRECTIONS;
    public static final int MAX_SEARCH_STEPS = 3000;

    public static List<BlockPos> find(World world, BlockPos pos, BlockPos pos2, int i) {
        if (pos.equals(pos2)) {
            return List.of();
        }
        if (!AutoMineBlockHelper.standable(world, pos2)) {
            return null;
        }
        HashMap map = new HashMap();
        ArrayDeque arrayDeque = new ArrayDeque();
        arrayDeque.add(pos);
        map.put(pos, null);
        int i2 = 0;
        while (!arrayDeque.isEmpty()) {
            int i3 = i2;
            i2++;
            if (i3 >= (3000)) {
                return null;
            }
            BlockPos pos3 = (BlockPos) arrayDeque.poll();
            if (pos3.equals(pos2)) {
                return reconstructPath(map, pos3);
            }
            for (BlockPos pos4 : findNeighbors(world, pos3, i)) {
                if (!map.containsKey(pos4)) {
                    map.put(pos4, pos3);
                    arrayDeque.add(pos4);
                }
            }
        }
        return null;
    }

    public static List<BlockPos> findNeighbors(World world, BlockPos pos, int i) {
        ArrayList arrayList = new ArrayList(8);
        Direction[] directions = CARDINAL_DIRECTIONS;
        int length = directions.length;
        for (int i2 = 0; i2 < length; i2++) {
            BlockPos offsetPos = pos.offset(directions[i2]);
            if (AutoMineBlockHelper.standable(world, offsetPos)) {
                arrayList.add(offsetPos);
            } else {
                BlockPos northPos = offsetPos.up();
                if (AutoMineBlockHelper.passable(world, pos.up().up()) && AutoMineBlockHelper.standable(world, northPos)) {
                    arrayList.add(northPos);
                } else if (AutoMineBlockHelper.passable(world, offsetPos) && AutoMineBlockHelper.passable(world, offsetPos.up())) {
                    BlockPos pos2 = offsetPos;
                    for (int i3 = 0; i3 < i; i3++) {
                        BlockPos upPos = pos2.down();
                        if (AutoMineBlockHelper.walkableOn(world, upPos)) {
                            break;
                        }
                        if (!AutoMineBlockHelper.passable(world, upPos)) {
                            pos2 = null;
                            break;
                        }
                        pos2 = upPos;
                    }
                    if (pos2 != null && !pos2.equals(offsetPos) && AutoMineBlockHelper.standable(world, pos2)) {
                        arrayList.add(pos2);
                    }
                }
            }
        }
        return arrayList;
    }

    public static List<BlockPos> reconstructPath(Map<BlockPos, BlockPos> map, BlockPos pos) {
        ArrayList arrayList = new ArrayList();
        BlockPos pos2 = pos;
        while (true) {
            BlockPos pos3 = pos2;
            if (pos3 == null || map.get(pos3) == null) {
                break;
            }
            arrayList.add(pos3);
            pos2 = map.get(pos3);
        }
        Collections.reverse(arrayList);
        return arrayList;
    }

    static {
        Direction[] directions = new Direction[4];
        directions[0] = Direction.NORTH;
        directions[1] = Direction.EAST;
        directions[2] = Direction.SOUTH;
        directions[3] = Direction.WEST;
        CARDINAL_DIRECTIONS = directions;
    }
}
