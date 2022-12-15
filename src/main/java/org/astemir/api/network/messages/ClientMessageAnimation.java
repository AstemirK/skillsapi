package org.astemir.api.network.messages;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import org.astemir.api.common.animation.Animation;
import org.astemir.api.common.animation.AnimationFactory;
import org.astemir.api.common.animation.IAnimated;
import org.astemir.api.network.AnimationTarget;

import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ClientMessageAnimation {

    private int id;
    private BlockPos pos;
    private Action action;
    private AnimationTarget target;
    private int animationId;
    private int tick = 0;

    public ClientMessageAnimation(AnimationTarget target, int id, Action action, int animationId, int tick) {
        this.id = id;
        this.action = action;
        this.target = target;
        this.animationId = animationId;
        this.tick = tick;
    }

    public ClientMessageAnimation(AnimationTarget target, BlockPos pos, Action action, int animationId, int tick) {
        this.pos = pos;
        this.action = action;
        this.target = target;
        this.animationId = animationId;
        this.tick = tick;
    }

    public ClientMessageAnimation(AnimationTarget target, int id, Action action, int animationId) {
        this.id = id;
        this.action = action;
        this.target = target;
        this.animationId = animationId;
    }

    public ClientMessageAnimation(AnimationTarget target, BlockPos pos, Action action, int animationId) {
        this.pos = pos;
        this.action = action;
        this.target = target;
        this.animationId = animationId;
    }

    public static void encode(ClientMessageAnimation message, FriendlyByteBuf buf) {
        buf.writeEnum(message.target);
        if (message.target == AnimationTarget.ENTITY) {
            buf.writeInt(message.id);
        }else
        if (message.target == AnimationTarget.BLOCK){
            buf.writeBlockPos(message.pos);
        }
        buf.writeEnum(message.action);
        buf.writeInt(message.animationId);
        buf.writeInt(message.tick);
    }

    public static ClientMessageAnimation decode(FriendlyByteBuf buf) {
        AnimationTarget target = buf.readEnum(AnimationTarget.class);
        if (target == AnimationTarget.ENTITY) {
            return new ClientMessageAnimation(target, buf.readInt(), buf.readEnum(Action.class), buf.readInt(),buf.readInt());
        }else
        if (target == AnimationTarget.BLOCK){
            return new ClientMessageAnimation(target, buf.readBlockPos(), buf.readEnum(Action.class), buf.readInt(),buf.readInt());
        }
        return null;
    }



    public static class Handler implements BiConsumer<ClientMessageAnimation, Supplier<NetworkEvent.Context>> {

        @Override
        public void accept(ClientMessageAnimation message, Supplier<NetworkEvent.Context> contextSupplier) {
            final NetworkEvent.Context context = contextSupplier.get();
            context.enqueueWork(() -> {
                AnimationFactory factory = null;
                switch (message.target){
                    case ENTITY:{
                        if (Minecraft.getInstance().level.getEntity(message.id) instanceof IAnimated animatedEntity){
                            factory = animatedEntity.getAnimationFactory();
                        }
                        break;
                    }
                    case BLOCK:{
                        BlockEntity blockEntity = Minecraft.getInstance().level.getBlockEntity(message.pos);
                        if (blockEntity instanceof IAnimated){
                            factory = ((IAnimated)blockEntity).getAnimationFactory();
                        }
                    }
                }
                if (factory != null) {
                    switch (message.action) {
                        case START -> {
                            Animation animation = factory.getAnimationList().getAnimation(message.animationId);
                            factory.setAnimation(animation,message.tick);
                            break;
                        }
                        case STOP -> {
                            Animation animation = factory.getAnimationList().getAnimation(message.animationId);
                            factory.removeAnimation(animation);
                            break;
                        }
                    }
                }
            });
            context.setPacketHandled(true);
        }
    }

    public enum Action{
        STOP,START
    }
}
