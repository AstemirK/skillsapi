package org.astemir.api.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.compress.utils.Sets;
import org.astemir.api.client.animation.AnimationBone;
import org.astemir.api.client.animation.AnimationFrame;
import org.astemir.api.client.animation.AnimationTrack;
import org.astemir.api.client.misc.AdvancedCubeRenderer;
import org.astemir.api.common.animation.Animation;
import org.astemir.api.math.Vector2;
import org.astemir.api.math.Vector3;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class JsonUtils {


    public static Set<AdvancedCubeRenderer> getModelRenderers(ResourceLocation resourceLocation){
        Map<String,AdvancedCubeRenderer> renderers = new HashMap<>();
        JsonParser parser = new JsonParser();
        InputStream stream = null;
        try {
            stream = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).get().open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonElement parsed = parser.parse(new InputStreamReader(stream));
        JsonElement geometryJson = parsed.getAsJsonObject().get("minecraft:geometry").getAsJsonArray().get(0);
        JsonObject descriptionJson = geometryJson.getAsJsonObject().get("description").getAsJsonObject();
        Vector2 textureSize = new Vector2(JsonUtils.getIntOrDefault(descriptionJson,"texture_width",32),JsonUtils.getIntOrDefault(descriptionJson,"texture_height",32));
        JsonArray bonesJson = geometryJson.getAsJsonObject().get("bones").getAsJsonArray();
        for (JsonElement bone : bonesJson) {
            JsonObject boneJson = bone.getAsJsonObject();
            String name = boneJson.get("name").getAsString();
            boolean isRoot = !boneJson.has("parent");
            Vector3 rotationPoint = getBedrockPivot(bonesJson,boneJson,isRoot);
            Vector3 rotation = JsonUtils.getVec3OrDefault(boneJson,"rotation",true,new Vector3(0,0,0));
            AdvancedCubeRenderer cubeRenderer = new AdvancedCubeRenderer(name,(int)textureSize.x,(int)textureSize.y,0,0);
            if (isRoot){
                cubeRenderer = cubeRenderer.root();
            }
            cubeRenderer = cubeRenderer.rotationPoint(rotationPoint.x,rotationPoint.y,rotationPoint.z).defaultRotation(rotation.x,rotation.y,rotation.z);
            if (boneJson.has("cubes")) {
                for (JsonElement cubeElement : boneJson.get("cubes").getAsJsonArray()) {
                    JsonObject cubeJson = cubeElement.getAsJsonObject();
                    Vector3 pos = getBedrockOrigin(boneJson,cubeJson);
                    Vector3 size = JsonUtils.getVec3OrDefault(cubeJson, "size", false, new Vector3(0, 0, 0));
                    Vector2 uv = JsonUtils.getVec2OrDefault(cubeJson, "uv", false, new Vector2(0, 0));
                    double inflate = JsonUtils.getDoubleOrDefault(cubeJson, "inflate", 0);
                    boolean mirror = JsonUtils.getBoolOrDefault(cubeJson, "mirror", false);
                    cubeRenderer.textureOffset((int) uv.x, (int) uv.y).cube(pos,size, (float) inflate,mirror);
                }
            }
            renderers.put(name,cubeRenderer);
        }
        for (JsonElement bone : bonesJson){
            JsonObject boneJson = bone.getAsJsonObject();
            String name = boneJson.get("name").getAsString();
            if (boneJson.has("parent")) {
                String parentName = boneJson.get("parent").getAsString();
                AdvancedCubeRenderer parent = renderers.get(parentName);
                AdvancedCubeRenderer child = renderers.get(name).parent(parent);
                parent.addChild(child);
            }
        }
        return new HashSet<>(renderers.values());
    }


    public static Set<AnimationTrack> getAnimationTracks(ResourceLocation resourceLocation) {
        Set<AnimationTrack> animationTracks = new HashSet<>();
        InputStream stream = null;
        try {
            stream = Minecraft.getInstance().getResourceManager().getResource(resourceLocation).get().open();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JsonElement parsed = JsonParser.parseReader(new InputStreamReader(stream));
        for (Map.Entry<String, JsonElement> animationEntry : JsonUtils.getEntries(parsed, "animations")) {
            String animationName = animationEntry.getKey();
            JsonObject animationJson = animationEntry.getValue().getAsJsonObject();
            AnimationTrack track = new AnimationTrack(animationName, Animation.Loop.parse(JsonUtils.getStringOrDefault(animationJson, "loop", "false")), JsonUtils.getDoubleOrDefault(animationJson, "animation_length", 0));
            for (Map.Entry<String, JsonElement> bonesEntry : JsonUtils.getEntries(animationJson, "bones")) {
                String boneName = bonesEntry.getKey();
                JsonObject boneJsonObject = bonesEntry.getValue().getAsJsonObject();
                AnimationBone bone = new AnimationBone(boneName, getAnimationProperty(boneJsonObject, "rotation", true), getAnimationProperty(boneJsonObject, "scale", false), getAnimationProperty(boneJsonObject, "position", false));
                track.addBone(bone);
            }
            animationTracks.add(track);
        }
        return animationTracks;
    }

    public static AnimationFrame[] getAnimationProperty(JsonObject bone, String name, boolean rad){
        if (bone.has(name)) {
            List<AnimationFrame> result = new ArrayList<>();
            if (bone.get(name).isJsonArray()) {
                JsonArray vectorArray = bone.get(name).getAsJsonArray();
                result.add(new AnimationFrame(0.0f, JsonUtils.getVec3(vectorArray, rad)));
                return result.toArray(new AnimationFrame[result.size()]);
            }else
            if (bone.get(name).isJsonPrimitive()){
                float f = bone.get(name).getAsFloat();
                result.add(new AnimationFrame(0.0f,new Vector3(f,f,f)));
                return result.toArray(new AnimationFrame[result.size()]);
            }

            JsonObject referenceJson = bone.get(name).getAsJsonObject();
            for (Map.Entry<String, JsonElement> timeCodes : referenceJson.entrySet()) {
                if (timeCodes.getKey().equals("vector")){
                    result.add(new AnimationFrame(0,JsonUtils.getVec3(timeCodes.getValue().getAsJsonArray(),rad)));
                }else {
                    double time = Double.parseDouble(timeCodes.getKey());
                    JsonElement value = timeCodes.getValue();
                    if (value.isJsonArray()) {
                        result.add(new AnimationFrame((float) time, JsonUtils.getVec3(value.getAsJsonArray(), rad)));
                    } else if (value.isJsonPrimitive()) {
                        float f = value.getAsFloat();
                        result.add(new AnimationFrame((float) time, new Vector3(f, f, f)));
                    } else if (value.isJsonObject()) {
                        JsonObject valueObject = value.getAsJsonObject();
                        if (valueObject.has("post")) {
                            if (valueObject.get("post").isJsonArray()){
                                result.add(new AnimationFrame((float) time, JsonUtils.getVec3(valueObject.get("post").getAsJsonArray(), rad)));
                            }else {
                                JsonObject postObject = valueObject.get("post").getAsJsonObject();
                                if (postObject.has("vector")) {
                                    result.add(new AnimationFrame((float) time, JsonUtils.getVec3(postObject.getAsJsonArray("vector"), rad)));
                                }
                            }
                        } else if (valueObject.has("vector")) {
                            JsonArray vector = valueObject.getAsJsonArray("vector");
                            result.add(new AnimationFrame((float) time, JsonUtils.getVec3(vector, rad)));
                        }
                    }
                }
            }
            return result.toArray(new AnimationFrame[result.size()]);
        }
        return null;
    }


    public static Vector3 getBedrockOrigin(JsonObject bone,JsonObject cube){
        Vector3 myPivot = JsonUtils.getVec3OrDefault(bone,"pivot",false,new Vector3(0,0,0));
        Vector3 myPos = JsonUtils.getVec3OrDefault(cube, "origin", false, new Vector3(0, 0, 0));
        Vector3 mySize = JsonUtils.getVec3OrDefault(cube, "size", false, new Vector3(0, 0, 0));
        return new Vector3(myPos.x-myPivot.x,-myPos.y-mySize.y+myPivot.y, myPos.z-myPivot.z);
    }


    public static Vector3 getBedrockPivot(JsonArray bones,JsonObject bone,boolean isRoot){
        Vector3 myPivot = JsonUtils.getVec3OrDefault(bone,"pivot",false,new Vector3(0,0,0));
        if (!isRoot) {
            String parent = bone.get("parent").getAsString();
            for (JsonElement otherBone : bones) {
                JsonObject otherBoneJson = otherBone.getAsJsonObject();
                String otherBoneName = otherBoneJson.get("name").getAsString();
                if (otherBoneName.equals(parent)){
                    Vector3 parentPivot = JsonUtils.getVec3OrDefault(otherBoneJson,"pivot",false,new Vector3(0,0,0));
                    return new Vector3(myPivot.x-parentPivot.x,-(myPivot.y-parentPivot.y),myPivot.z-parentPivot.z);
                }
            }
        }
        return new Vector3(myPivot.x, 24-myPivot.y,myPivot.z);
    }

    public static boolean getBoolOrDefault(JsonObject object,String name,boolean defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return object.get(name).getAsBoolean();
    }

    public static String getStringOrDefault(JsonObject object,String name,String defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return object.get(name).getAsString();
    }

    public static double getDoubleOrDefault(JsonObject object,String name,double defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return object.get(name).getAsDouble();
    }

    public static int getIntOrDefault(JsonObject object,String name,int defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return object.get(name).getAsInt();
    }

    public static Vector3 getVec3OrDefault(JsonObject object,String name,boolean rad,Vector3 defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return getVec3(object.get(name).getAsJsonArray(),rad);
    }

    public static Vector2 getVec2OrDefault(JsonObject object,String name,boolean rad,Vector2 defaultValue){
        if (!object.has(name)){
            return defaultValue;
        }
        return getVec2(object.get(name).getAsJsonArray(),rad);
    }

    public static Set<Map.Entry<String, JsonElement>> getEntries(JsonElement element, String name){
        return element.getAsJsonObject().get(name).getAsJsonObject().entrySet();
    }

    public static Vector2 getVec2(JsonArray vectorArray, boolean rad){
        double x = vectorArray.get(0).getAsDouble();
        double y = vectorArray.get(1).getAsDouble();
        if (rad){
            x = Math.toRadians(x);
            y = Math.toRadians(y);
        }
        return new Vector2((float)x,(float)y);
    }

    public static Vector3 getVec3(JsonArray vectorArray, boolean rad){
        double x = vectorArray.get(0).getAsDouble();
        double y = vectorArray.get(1).getAsDouble();
        double z = vectorArray.get(2).getAsDouble();
        if (rad){
            x = Math.toRadians(x);
            y = Math.toRadians(y);
            z = Math.toRadians(z);
        }
        return new Vector3((float)x,(float)y,(float)z);
    }
}
