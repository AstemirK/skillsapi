package org.astemir.api.common.gfx;

import net.minecraft.network.FriendlyByteBuf;
import org.astemir.api.math.components.Color;
import org.astemir.api.common.entity.ClientSideValue;
import org.astemir.api.network.NetworkUtils;

public class GFXBlackInOut extends GFXEffect{

    private ClientSideValue firstValue = new ClientSideValue(0,1);
    private ClientSideValue secondValue = new ClientSideValue(1,0);
    private Color color;
    private double speed;
    private boolean out = false;

    public GFXBlackInOut(Color color, double speed) {
        this.color = color;
        this.speed = (float) speed/10f;
    }

    public GFXBlackInOut() {
    }

    @Override
    public void update() {
        if (out){
            secondValue.update((float)speed);
        }else{
            if (firstValue.valueO >= 0.99f){
                out = true;
            }
            firstValue.update((float)speed);
        }
    }

    @Override
    public void read(FriendlyByteBuf buf) {
        this.color = NetworkUtils.readColor(buf);
        this.speed = buf.readDouble();
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        NetworkUtils.writeColor(buf,color);
        buf.writeDouble(speed);
    }


    @Override
    public boolean isRemoved() {
        if (out) {
            return Math.abs(secondValue.valueO) <= 0.001f;
        }
        return false;
    }


    public ClientSideValue getValue() {
        if (out){
            return secondValue;
        }else{
            return firstValue;
        }
    }

    public Color getColor() {
        return color;
    }

    @Override
    public GFXEffectType getEffectType() {
        return GFXEffectType.BLACK_IN_OUT;
    }
}
