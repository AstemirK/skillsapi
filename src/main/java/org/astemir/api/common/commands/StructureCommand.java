package org.astemir.api.common.commands;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import org.astemir.api.common.commands.build.CommandArgument;
import org.astemir.api.common.commands.build.CommandBuilder;
import org.astemir.api.common.commands.build.CommandVariant;
import org.astemir.api.common.world.schematic.ISchematicBuilder;
import org.astemir.api.common.world.schematic.WESchematic;
import org.astemir.api.math.components.Vector3;
import org.astemir.api.io.FileUtils;

public class StructureCommand {


    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        CommandBuilder builder = new CommandBuilder("structure");
        CommandArgument name = CommandArgument.greedyString("name");
        CommandArgument blockPos = CommandArgument.blockPos("position");
        CommandArgument rotation = CommandArgument.vector3("rotation");
        CommandArgument skipAir = CommandArgument.bool("skip-air");
        SchematicBuilder schematicBuilder = new SchematicBuilder();
        builder.addVariant(new CommandVariant(blockPos,rotation,skipAir,name).execute((p)->{
            ServerLevel level = p.getSource().getLevel();
            try {
                WESchematic schematic = new WESchematic(FileUtils.getResource(name.getString(p)+".schem"));
                schematicBuilder.buildSchematic(schematic,level,blockPos.getBlockPos(p), Vector3.from(rotation.getVector3(p)),true, skipAir.getBoolean(p));
            }catch (Exception e){
                e.printStackTrace();
            }
            return 0;
        }));
        builder.addVariant(new CommandVariant(blockPos,rotation,name).execute((p)->{
            ServerLevel level = p.getSource().getLevel();
            try {
                WESchematic schematic = new WESchematic(FileUtils.getResource(name.getString(p)+".schem"));
                schematicBuilder.buildSchematic(schematic,level,blockPos.getBlockPos(p), Vector3.from(rotation.getVector3(p)),true, true);
            }catch (Exception e){
            }
            return 0;
        }));
        builder.addVariant(new CommandVariant(blockPos,name).execute((p)->{
            ServerLevel level = p.getSource().getLevel();
            try {
                WESchematic schematic = new WESchematic(FileUtils.getResource(name.getString(p)+".schem"));


                schematicBuilder.buildSchematic(schematic,level,blockPos.getBlockPos(p),new Vector3(0,0,0),true,true);
            }catch (Exception e){
            }
            return 0;
        }));
        dispatcher.register(builder.permission((p)->p.hasPermission(2)).build());
    }


    private static class SchematicBuilder implements ISchematicBuilder{
    }
}
