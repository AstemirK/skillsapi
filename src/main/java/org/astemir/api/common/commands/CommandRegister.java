package org.astemir.api.common.commands;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.astemir.api.common.commands.PlayActionCommand;
import org.astemir.api.common.commands.PlayAnimationCommand;
import org.astemir.api.common.commands.GFXEffectCommand;

public class CommandRegister {


    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e){
        GFXEffectCommand.register(e.getDispatcher());
        PlayAnimationCommand.register(e.getDispatcher());
        PlayActionCommand.register(e.getDispatcher());
        StructureCommand.register(e.getDispatcher());
    }
}
