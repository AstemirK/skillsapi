package org.astemir.api.client.model;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import org.astemir.api.client.animation.AnimationBone;
import org.astemir.api.client.animation.AnimationDataHandler;
import org.astemir.api.client.animation.AnimationFrame;
import org.astemir.api.client.animation.AnimationTrack;
import org.astemir.api.client.misc.AdvancedCubeRenderer;
import org.astemir.api.common.animation.Animation;
import org.astemir.api.common.animation.AnimationFactory;
import org.astemir.api.common.animation.IAnimated;
import org.astemir.api.common.animation.ITESRModel;
import org.astemir.api.math.Transform;
import org.astemir.api.math.Vector3;
import org.astemir.api.utils.AnimationUtils;
import org.astemir.api.utils.JsonUtils;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;


public abstract class AnimatedAdvancedModel<T extends ITESRModel & IAnimated> extends AdvancedModel<T> {

    public Set<AnimationTrack> animations = new HashSet<>();

    public AnimatedAdvancedModel(Function<ResourceLocation, RenderType> p_103110_, ResourceLocation modelLoc, ResourceLocation animationsLoc) {
        super(p_103110_, modelLoc);
        if (animationsLoc != null) {
            animations = JsonUtils.getAnimationTracks(animationsLoc);
        }
    }


    public boolean isRendererUsed(IAnimated animated,AdvancedCubeRenderer renderer){
        for (AnimationTrack track : animations) {
            if (isPlayingTrack(animated,track)){
                if (track.hasBone(renderer.getName())){
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isPlayingTrack(IAnimated animated, AnimationTrack track){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            if (playingAnimation.getName().equals(track.getName())){
                return true;
            }
        }
        return false;
    }


    private boolean checkCanRotate(T animated,Animation animation,AnimationBone bone){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            if (!playingAnimation.getName().equals(animation.getName())) {
                if (playingAnimation.getPriority() > animation.getPriority()) {
                    AnimationTrack track = getTrack(playingAnimation.getName());
                    if (track.hasBone(bone.getBoneName())) {
                        AnimationBone otherBone = track.getBone(bone.getBoneName());
                        if (otherBone.getRotations() != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkCanMove(T animated,Animation animation,AnimationBone bone){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            if (!playingAnimation.getName().equals(animation.getName())) {
                if (playingAnimation.getPriority() > animation.getPriority()) {
                    AnimationTrack track = getTrack(playingAnimation.getName());
                    if (track.hasBone(bone.getBoneName())) {
                        AnimationBone otherBone = track.getBone(bone.getBoneName());
                        if (otherBone.getPositions() != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public AnimationTrack getTrack(String name){
        for (AnimationTrack animation : animations) {
            if (animation.getName().equals(name)){
                return animation;
            }
        }
        return null;
    }

    private boolean checkCanScale(T animated,Animation animation,AnimationBone bone){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            if (!playingAnimation.getName().equals(animation.getName())) {
                if (playingAnimation.getPriority() > animation.getPriority()) {
                    AnimationTrack track = getTrack(playingAnimation.getName());
                    if (track.hasBone(bone.getBoneName())) {
                        AnimationBone otherBone = track.getBone(bone.getBoneName());
                        if (otherBone.getScales() != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean isRotatingInAnyTrack(T animated,AdvancedCubeRenderer renderer){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            AnimationTrack track = getTrack(playingAnimation.getName());
            if (track.hasBone(renderer.getName())){
                return track.getBone(renderer.getName()).getRotations() != null;
            }
        }
        return false;
    }


    private boolean isPositioningInAnyTrack(T animated,AdvancedCubeRenderer renderer){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            AnimationTrack track = getTrack(playingAnimation.getName());
            if (track.hasBone(renderer.getName())){
                return track.getBone(renderer.getName()).getPositions() != null;
            }
        }
        return false;
    }


    private boolean isScalingInAnyTrack(T animated,AdvancedCubeRenderer renderer){
        for (Animation playingAnimation : animated.getAnimationFactory().getPlayingAnimations()) {
            AnimationTrack track = getTrack(playingAnimation.getName());
            if (track.hasBone(renderer.getName())){
                return track.getBone(renderer.getName()).getScales() != null;
            }
        }
        return false;
    }

    @Override
    public void setupAnim(T animated, float limbSwing, float limbSwingAmount, float ticks,float headYaw, float headPitch) {
        float partialTicks = Minecraft.getInstance().getPartialTick();
        if (!Minecraft.getInstance().isPaused()) {
            float smoothness = 2;
            float delta = partialTicks/smoothness;
            if (!animations.isEmpty()) {
                reset();
                AnimationDataHandler animationManager = AnimationDataHandler.getInstance();
                AnimationDataHandler.BoneStates data = animationManager.getOrCreateData(animated);
                float deltaTime = (data.getTempTick() - data.getPrevTempTick()) / 20f;
                if (deltaTime < 0) {
                    deltaTime = 0;
                }
                data.update(animated, ticks, deltaTime);

                for (AdvancedCubeRenderer renderer : getRenderers()) {
                    Transform rendererTransform = animationManager.getTransformData(animated, renderer);

                    Vector3 rot = rendererTransform.getRotation();
                    Vector3 scale = rendererTransform.getScale();
                    Vector3 pos = rendererTransform.getPosition();
                    if (isRendererUsed(animated, renderer)) {
                        AnimationFactory animationFactory = animated.getAnimationFactory();
                        for (Animation animation : animationFactory.getPlayingAnimations()) {
                            AnimationTrack track = getTrack(animation.getName());
                            if (track.hasBone(renderer.getName())) {
                                AnimationBone bone = track.getBone(renderer.getName());
                                float animationTick = data.getAnimationTick(animation);
                                if (bone.getRotations() != null) {
                                    if (checkCanRotate(animated, animation, bone)) {
                                        AnimationFrame[] frames = bone.getRotations();
                                        rot = rot.rotLerp(AnimationUtils.interpolatePoints(frames, animationTick), delta);
                                    }
                                }
                                if (bone.getScales() != null) {
                                    if (checkCanScale(animated, animation, bone)) {
                                        AnimationFrame[] frames = bone.getScales();
                                        scale = scale.lerp(AnimationUtils.interpolatePoints(frames, animationTick), delta);
                                    }
                                }
                                if (bone.getPositions() != null) {
                                    if (checkCanMove(animated, animation, bone)) {
                                        AnimationFrame[] frames = bone.getPositions();
                                        pos = pos.lerp(AnimationUtils.interpolatePoints(frames, animationTick), delta);
                                    }
                                }
                            }
                        }
                    } else {
                        rot = rot.lerp(new Vector3(0, 0, 0), delta);
                        scale = scale.lerp(new Vector3(1, 1, 1), delta);
                        pos = pos.lerp(new Vector3(0, 0, 0), delta);
                    }
                    if (!isRotatingInAnyTrack(animated, renderer)) {
                        rot = rot.lerp(new Vector3(0, 0, 0), delta);
                    }
                    if (!isScalingInAnyTrack(animated, renderer)) {
                        scale = scale.lerp(new Vector3(1, 1, 1), delta);
                    }
                    if (!isPositioningInAnyTrack(animated, renderer)) {
                        pos = pos.lerp(new Vector3(0, 0, 0), delta);
                    }
                    rendererTransform.setRotation(rot);
                    rendererTransform.setScale(scale);
                    rendererTransform.setPosition(pos);
                    renderer.apply(rendererTransform);
                }
            }
            animate(animated, limbSwing, limbSwingAmount, ticks, delta, headYaw, headPitch);
        }
        customAnimate(animated,limbSwing,limbSwingAmount,ticks,partialTicks,headYaw,headPitch);
    }

}
