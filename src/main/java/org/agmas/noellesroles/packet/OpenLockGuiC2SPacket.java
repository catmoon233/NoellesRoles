package org.agmas.noellesroles.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

import net.minecraft.world.phys.Vec3;

public record OpenLockGuiC2SPacket(Vec3 pos, int lockId) implements CustomPacketPayload {
    public static final ResourceLocation OPEN_LOCK_GUI_C2S = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "open_lock_gui_c2s");
    public static final CustomPacketPayload.Type<OpenLockGuiC2SPacket> ID = new CustomPacketPayload.Type<>(OPEN_LOCK_GUI_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLockGuiC2SPacket> CODEC;
    
    static {
        CODEC = StreamCodec.ofMember(OpenLockGuiC2SPacket::encode, OpenLockGuiC2SPacket::decode);
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
    
    public void encode(RegistryFriendlyByteBuf buf) {
        buf.writeDouble(this.pos.x);
        buf.writeDouble(this.pos.y);
        buf.writeDouble(this.pos.z);
        buf.writeInt(this.lockId);
    }
    
    public static OpenLockGuiC2SPacket decode(RegistryFriendlyByteBuf buf) {
        double x = buf.readDouble();
        double y = buf.readDouble();
        double z = buf.readDouble();
        int lockId = buf.readInt();
        Vec3 pos = new Vec3(x, y, z);
        return new OpenLockGuiC2SPacket(pos, lockId);
    }
}
