package net.slimevoid.dynamictransport.client.renderer;

import com.google.common.collect.ImmutableSet;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.resources.IResource;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.b3d.B3DLoader;
import net.minecraftforge.client.model.b3d.B3DModel;
import net.slimevoid.dynamictransport.core.DynamicTransportMod;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;
import org.apache.commons.io.IOUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BackedModelLoader implements ICustomModelLoader {

    private IResourceManager manager;
    private final Map<ResourceLocation, ModelBlock> cache = new HashMap<>();



    @Override
    public boolean accepts(ResourceLocation modelLocation) {
        return modelLocation.getResourceDomain().equals(DynamicTransportMod.MOD_ID) && modelLocation.getResourcePath().endsWith(".dt");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException {
        ResourceLocation file = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath());
        if(!cache.containsKey(file))
        {
            IResource resource = null;
            try
            {
                try
                {
                    resource = manager.getResource(file);
                }
                catch(FileNotFoundException e)
                {
                    if(modelLocation.getResourcePath().startsWith("models/block/"))
                        resource = manager.getResource(new ResourceLocation(file.getResourceDomain(), "models/item/" + file.getResourcePath().substring("models/block/".length())));
                    else if(modelLocation.getResourcePath().startsWith("models/item/"))
                        resource = manager.getResource(new ResourceLocation(file.getResourceDomain(), "models/block/" + file.getResourcePath().substring("models/item/".length())));
                    else throw e;
                }
                Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8);
                cache.put(file, ModelBlock.deserialize(reader));
            }
            catch(IOException e)
            {
                cache.put(file, null);
                throw e;
            }
            finally
            {
                IOUtils.closeQuietly(resource);
            }
        }
        ModelBlock model = cache.get(file);
        return new CamoIModel(model.textures);
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager) {
        this.manager = resourceManager;
    }
}
