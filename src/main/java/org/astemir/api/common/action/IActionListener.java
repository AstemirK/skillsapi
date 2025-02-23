package org.astemir.api.common.action;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.astemir.api.common.animation.HolderKey;
import org.astemir.api.network.messages.ServerMessageActionSync;
import org.astemir.api.network.NetworkUtils;
import org.checkerframework.checker.units.qual.A;

public interface IActionListener {

    ActionStateMachine getActionStateMachine();


    default void syncClient(){
        if (this instanceof Entity entity){
            NetworkUtils.sendToServer(new ServerMessageActionSync(new HolderKey(entity.getId())));
        }else
        if (this instanceof BlockEntity blockEntity) {
            NetworkUtils.sendToServer(new ServerMessageActionSync(new HolderKey(blockEntity.getBlockPos())));
        }
    }

    default void onActionBegin(Action state){};

    default void onActionEnd(Action state){};

    default void onActionTick(Action state,int ticks){};
}
