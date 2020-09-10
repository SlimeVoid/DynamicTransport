package net.slimevoid.dynamictransport.client.renderer;

import com.google.common.collect.ImmutableSet;
import net.slimevoid.dynamictransport.client.renderer.block.CamoModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.common.model.TRSRTransformation;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Function;

public class CamoIModel implements IModel {
    private final Map<String, String> textures;

    public CamoIModel(Map<String, String> textures) {
        this.textures = textures;
    }

    @Override
    public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter) {
        TextureAtlasSprite missing = bakedTextureGetter.apply(new ResourceLocation("missingno"));
        TextureAtlasSprite top = missing;
        TextureAtlasSprite overlay = null;
        if(textures.containsKey("top")){
            top = bakedTextureGetter.apply(new ResourceLocation(textures.get("top")));
        }
        if(textures.containsKey("overlay")){
            overlay = bakedTextureGetter.apply(new ResourceLocation(textures.get("overlay")));
        }

        return new CamoModel(format,top,overlay);
    }

    @Override
    public Collection<ResourceLocation> getDependencies() {
        return Collections.emptySet();
    }

    @Override
    public Collection<ResourceLocation> getTextures() {
        ImmutableSet.Builder<ResourceLocation> builder = ImmutableSet.builder();
        for(String s : textures.values())
        {
            if(!s.startsWith("#"))
            {
                builder.add(new ResourceLocation(s));
            }
        }
        return builder.build();
    }

    @Override
    public IModelState getDefaultState() {
        return TRSRTransformation.identity();
    }
}