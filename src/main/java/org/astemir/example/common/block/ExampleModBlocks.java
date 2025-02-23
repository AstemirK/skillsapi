package org.astemir.example.common.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.astemir.example.SkillsAPI;

public class ExampleModBlocks {

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SkillsAPI.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> TILE_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, SkillsAPI.MOD_ID);

    public static RegistryObject<Block> COSMIC_BEACON = BLOCKS.register("cosmic_beacon", BlockExampleCosmicBeacon::new);
    public static RegistryObject<BlockEntityType<? extends BlockEntityExampleCosmicBeacon>> COSMIC_BEACON_ENTITY = TILE_ENTITIES.register("cosmic_beacon", ()->BlockEntityType.Builder.of(BlockEntityExampleCosmicBeacon::new,COSMIC_BEACON.get()).build(null));
}
