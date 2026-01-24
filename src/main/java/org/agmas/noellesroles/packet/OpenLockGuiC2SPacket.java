package org.agmas.noellesroles.packet;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.agmas.noellesroles.Noellesroles;

public class OpenLockGuiC2SPacket implements CustomPacketPayload
{
    public static final ResourceLocation OPEN_LOCK_GUI_C2S = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID, "open_lock_gui_c2s");
    public static final CustomPacketPayload.Type<OpenLockGuiC2SPacket> ID = new CustomPacketPayload.Type<>(OPEN_LOCK_GUI_C2S);
    public static final StreamCodec<RegistryFriendlyByteBuf, OpenLockGuiC2SPacket> CODEC = StreamCodec.unit(new OpenLockGuiC2SPacket());
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}
