package net.darkhax.bookshelf.serialization;

import com.google.gson.JsonElement;

import net.minecraft.network.PacketBuffer;
import net.minecraft.util.text.ITextComponent;

public final class SerializerText implements ISerializer<ITextComponent> {
    
    public static final ISerializer<ITextComponent> SERIALIZER = new SerializerText();
    
    @Override
    public ITextComponent read (JsonElement json) {
        
        return ITextComponent.Serializer.func_240641_a_(json);
    }
    
    @Override
    public JsonElement write (ITextComponent toWrite) {
        
        return ITextComponent.Serializer.toJsonTree(toWrite);
    }
    
    @Override
    public ITextComponent read (PacketBuffer buffer) {
        
        final String jsonString = buffer.readString();
        return ITextComponent.Serializer.func_240643_a_(jsonString);
    }
    
    @Override
    public void write (PacketBuffer buffer, ITextComponent toWrite) {
        
        buffer.writeString(ITextComponent.Serializer.toJson(toWrite));
    }
}