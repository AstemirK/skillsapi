package org.astemir.example.common.entity;


import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.astemir.example.SkillsAPI;
import org.astemir.api.common.entity.EntityProperties;
import org.astemir.api.common.register.EntityRegistry;

public class ExampleModEntities extends EntityRegistry {

    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, SkillsAPI.MOD_ID);
    public static final RegistryObject<EntityType<EntityExampleMinotaur>> MINOTAUR = register(ENTITIES, SkillsAPI.MOD_ID,"minotaur",new EntityProperties(EntityExampleMinotaur::new,MobCategory.AMBIENT,3,3,()-> Zombie.createAttributes().build()));
    public static final RegistryObject<EntityType<EntityExampleSharkBoat>> SHARK_BOAT = ENTITIES.register("shark_boat",()->EntityType.Builder.<EntityExampleSharkBoat>of(EntityExampleSharkBoat::new,MobCategory.MISC).sized(1,1).build("shark_boat"));

}
