package dev.sejtam.mineschem.core.utils;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

import java.util.*;

public class Region implements Iterable<Region.RegionBlock>, Cloneable {

    private BlockPosition pos1;
    private BlockPosition pos2;

    private Instance instance;

    public Region(Position pos1, Position pos2) {
        this(new BlockPosition(pos1.getX(), pos1.getY(), pos1.getZ())
            ,new BlockPosition(pos2.getX(), pos2.getY(), pos2.getZ()));
    }
    public Region(BlockPosition position1, BlockPosition position2) {
        this.pos1 = new BlockPosition(Math.min(position1.getX(), position2.getX()), Math.min(position1.getY(), position2.getY()), Math.min(position1.getZ(), position2.getZ()));
        this.pos2 = new BlockPosition(Math.max(position1.getX(), position2.getX()), Math.max(position1.getY(), position2.getY()), Math.max(position1.getZ(), position2.getZ()));
        this.instance = new ArrayList<>(MinecraftServer.getInstanceManager().getInstances()).get(0);
    }

    // Width
    public int getSizeX() {
        return (this.pos2.getX() - this.pos1.getX()) + 1;
    }
    // Height
    public int getSizeY() {
        return (this.pos2.getY() - this.pos1.getY()) + 1;
    }
    // Length
    public int getSizeZ() {
        return (this.pos2.getZ() - this.pos1.getZ()) + 1;
    }

    public int getLowerX() {
        return this.pos1.getX();
    }
    public int getLowerY() {
        return this.pos1.getY();
    }
    public int getLowerZ() {
        return this.pos1.getZ();
    }
    public BlockPosition getLower() {
        return new BlockPosition(this.pos1.getX(), this.pos1.getY(), this.pos1.getZ());
    }

    public int getUpperX() {
        return this.pos2.getX();
    }
    public int getUpperY() {
        return this.pos2.getY();
    }
    public int getUpperZ() {
        return this.pos2.getZ();
    }
    public BlockPosition getUpper() {
        return new BlockPosition(this.pos2.getX(), this.pos2.getY(), this.pos2.getZ());
    }

    public short[] cornersStateId() {
        short[] corners = new short[8];

        corners[0] = instance.getBlockStateId(this.pos1.getX(), this.pos1.getY(), this.pos1.getZ());
        corners[1] = instance.getBlockStateId(this.pos1.getX(), this.pos1.getY(), this.pos2.getZ());
        corners[2] = instance.getBlockStateId(this.pos1.getX(), this.pos2.getY(), this.pos1.getZ());
        corners[3] = instance.getBlockStateId(this.pos1.getX(), this.pos2.getY(), this.pos2.getZ());
        corners[4] = instance.getBlockStateId(this.pos2.getX(), this.pos1.getY(), this.pos1.getZ());
        corners[5] = instance.getBlockStateId(this.pos2.getX(), this.pos1.getY(), this.pos2.getZ());
        corners[6] = instance.getBlockStateId(this.pos2.getX(), this.pos2.getY(), this.pos1.getZ());
        corners[7] = instance.getBlockStateId(this.pos2.getX(), this.pos2.getY(), this.pos2.getZ());

        return corners;
    }
    public BlockPosition[] cornersBlockPosition() {
        BlockPosition[] corners = new BlockPosition[8];

        corners[0] = new BlockPosition(this.pos1.getX(), this.pos1.getY(), this.pos1.getZ());
        corners[1] = new BlockPosition(this.pos1.getX(), this.pos1.getY(), this.pos2.getZ());
        corners[2] = new BlockPosition(this.pos1.getX(), this.pos2.getY(), this.pos1.getZ());
        corners[3] = new BlockPosition(this.pos1.getX(), this.pos2.getY(), this.pos2.getZ());
        corners[4] = new BlockPosition(this.pos2.getX(), this.pos1.getY(), this.pos1.getZ());
        corners[5] = new BlockPosition(this.pos2.getX(), this.pos1.getY(), this.pos2.getZ());
        corners[6] = new BlockPosition(this.pos2.getX(), this.pos2.getY(), this.pos1.getZ());
        corners[7] = new BlockPosition(this.pos2.getX(), this.pos2.getY(), this.pos2.getZ());

        return corners;
    }

    public Iterator<RegionBlock> iterator() {
        return new RegionIterator(this.pos1.getX(), this.pos1.getY(), pos1.getZ(), this.pos2.getX(), pos2.getY(), pos2.getZ(), this.instance);
    }
    public List<Short> getStateIds() {
        Iterator<RegionBlock> iterator = this.iterator();
        List<Short> list = new ArrayList<>();

        while (iterator.hasNext())
            list.add(iterator.next().getStateId());

        return list;
    }
    public Map<BlockPosition, Short> getStateIdsMap() {
        Iterator<RegionBlock> iterator = this.iterator();
        Map<BlockPosition, Short> map = new HashMap<>();

        while (iterator.hasNext()) {
            RegionBlock block = iterator.next();
            map.put(block.getPosition(), block.getStateId());
        }

        return map;
    }

    public String toString() {
        return "{Pos1:" + pos1.toString() + ", Pos2:" + pos2.toString() + "}";
    }

    public class RegionIterator implements Iterator<RegionBlock> {
        private Instance instance;
        private int baseX, baseY, baseZ;
        private int x, y, z;
        private int sizeX, sizeY, sizeZ;

        public RegionIterator(int x1, int y1, int z1, int x2, int y2, int z2, Instance instance) {
            this.instance = instance;
            this.baseX = x1;
            this.baseY = y1;
            this.baseZ = z1;
            this.sizeX = Math.abs(x2 - x1) + 1;
            this.sizeY = Math.abs(y2 - y1) + 1;
            this.sizeZ = Math.abs(z2 - z1) + 1;
            this.x = this.y = this.z = 0;
        }

        public boolean hasNext() {
            return this.x < this.sizeX && this.y < this.sizeY && this.z < this.sizeZ;
        }

        public RegionBlock next() {
            BlockPosition blockPosition = new BlockPosition(this.baseX + this.x, this.baseY + this.y, this.baseZ + this.z);
            short state = this.instance.getBlockStateId(blockPosition);
            RegionBlock regionBlock = new RegionBlock(blockPosition, state);

            if (++x >= this.sizeX) {
                this.x = 0;
                if (++this.z >= this.sizeZ) {
                    this.z = 0;
                    ++this.y;
                }
            }

            return regionBlock;
        }

        public void remove() {}
    }

    public static class RegionBlock {

        private BlockPosition blockPosition;
        private short stateId;

        public RegionBlock(BlockPosition blockPosition, short stateId) {
            this.blockPosition = blockPosition;
            this.stateId = stateId;
        }

        public BlockPosition getPosition() {
            return this.blockPosition;
        }

        public short getStateId() {
            return this.stateId;
        }
    }

}
