package org.astemir.example.client.wrapper;


import net.minecraft.resources.ResourceLocation;
import org.astemir.api.client.model.AdvancedModel;
import org.astemir.api.client.wrapper.EntityModelWrapper;
import org.astemir.api.utils.ResourceUtils;
import org.astemir.example.SkillsAPIMod;
import org.astemir.example.client.model.ModelSharkBoat;
import org.astemir.example.common.entity.EntitySharkBoat;

public class ModelWrapperSharkBoat extends EntityModelWrapper<EntitySharkBoat> {

    public final ResourceLocation TEXTURE = ResourceUtils.loadTexture(SkillsAPIMod.MOD_ID, "entity/shark_boat.png");
    private final ModelSharkBoat MODEL = new ModelSharkBoat();


    @Override
    public AdvancedModel<EntitySharkBoat> getModel(EntitySharkBoat target) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTexture(EntitySharkBoat target) {
        return TEXTURE;
    }
}
