package org.astemir.api.common.world.schematic;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.visitors.CollectToTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.StringUtils;
import org.astemir.api.math.components.Vector3;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class WESchematic {

    private Map<Integer, BlockState> blockStates = new HashMap<>();
    private byte[] blocks = new byte[]{};
    private int width = 0;
    private int height = 0;
    private int length = 0;
    private StateReplacement replacement = (state) -> {
        if (state.is(Blocks.STRUCTURE_VOID)){
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    };

    public WESchematic(InputStream stream) {
        if (stream != null) {
            CollectToTag visitor = new CollectToTag();
            try {
                NbtIo.parseCompressed(new DataInputStream(stream), visitor);
                CompoundTag tag = (CompoundTag) visitor.getResult();
                load(tag);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void load(CompoundTag tag){
        CompoundTag palette = tag.getCompound("Palette");
        this.blocks = tag.getByteArray("BlockData");
        this.width = tag.getShort("Width");
        this.height = tag.getShort("Height");
        this.length = tag.getShort("Length");
        for (String paletteKey : palette.getAllKeys()) {
            if (paletteKey != null && !paletteKey.isEmpty()) {
                String id = paletteKey;
                String[] statesArray = new String[]{};
                if (paletteKey.contains("[") && paletteKey.contains("]")){
                    String states = StringUtils.substringBetween(paletteKey, "[", "]");
                    id = paletteKey.replace("["+states+"]","");
                    statesArray = states.split(",");
                }
                Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
                BlockState blockState = block.defaultBlockState();
                if (statesArray.length > 0) {
                    StateDefinition<Block, BlockState> stateDefinition = block.getStateDefinition();
                    for (String state : statesArray) {
                        String[] stateKeyValue = state.split("=");
                        Property<?> property = stateDefinition.getProperty(stateKeyValue[0]);
                        if (property != null) {
                            blockState = setValueHelper(blockState, property, stateKeyValue[1]);
                        }
                    }
                }
                blockStates.put(palette.getInt(paletteKey), blockState);
            }
        }
    }

    private static <S extends StateHolder<?, S>, T extends Comparable<T>> S setValueHelper(S pStateHolder, Property<T> pProperty, String value) {
        Optional<T> optional = pProperty.getValue(value);
        if (optional.isPresent()) {
            return pStateHolder.setValue(pProperty, optional.get());
        } else {
            return pStateHolder;
        }
    }


    public boolean isEmptyForPlace(LevelAccessor level, BlockPos pos, Vector3 rotation, boolean centered, boolean skipAir){
        for (Map.Entry<Vector3, BlockState> entry : blocks(skipAir).entrySet()) {
            if (!entry.getValue().isAir()) {
                Vector3 point = entry.getKey();
                if (centered) {
                    point = point.add(-width / 2, 0, -length / 2);
                }
                point = point.rotateAroundZ(rotation.z).rotateAroundY(rotation.y).rotateAroundX(rotation.x);
                BlockPos newPos = pos.offset(point.x,point.y,point.z);
                BlockState oldState = level.getBlockState(newPos);
                if (oldState.isSolidRender(level, newPos)) {
                    return false;
                }
            }
        }
        return true;
    }

    public Map<Vector3,BlockState> blocks(boolean skipAir){
        Map<Vector3,BlockState> map = new HashMap<>();
        for (int x = 0; x < width; ++x) {
            for (int y = 0; y < height; ++y) {
                for (int z = 0; z < length; ++z) {
                    int index = y * width * length + z * width + x;
                    BlockState state = blockStates.get((int)blocks[index]);
                    if (state != null) {
                        Vector3 point = new Vector3(x, y, z);
                        Vector3 roundedPos = new Vector3(Math.round(point.x), Math.round(point.y), Math.round(point.z));
                        if (state.isAir()) {
                            if (!skipAir) {
                                map.put(roundedPos, state);
                            }
                        } else {
                            map.put(roundedPos, replacement.replaceState(state));
                        }
                    }
                }
            }
        }
        return map;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    public StateReplacement getReplacement() {
        return replacement;
    }


    public WESchematic replacement(StateReplacement replacement) {
        this.replacement = replacement;
        return this;
    }

    public interface StateReplacement{

        BlockState replaceState(BlockState state);
    }
}
