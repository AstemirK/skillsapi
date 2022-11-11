package org.astemir.api.common.event;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.astemir.api.common.commands.PlayActionCommand;
import org.astemir.api.common.commands.PlayAnimationCommand;
import org.astemir.api.common.commands.ShakeScreenCommand;

public class CommandsRegisterEvents {


    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent e){
        ShakeScreenCommand.register(e.getDispatcher());
        PlayAnimationCommand.register(e.getDispatcher());
        PlayActionCommand.register(e.getDispatcher());
    }
}
