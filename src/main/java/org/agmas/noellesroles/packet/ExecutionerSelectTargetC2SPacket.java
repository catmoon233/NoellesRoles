package org.agmas.noellesroles.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.agmas.noellesroles.Noellesroles;

import java.util.UUID;

/**
 * Executioner选择目标的网络包
 * 用于客户端向服务器发送选中的目标玩家UUID
 */
public record ExecutionerSelectTargetC2SPacket(UUID target) implements CustomPayload {
    public static final Identifier EXECUTIONER_SELECT_TARGET_PAYLOAD_ID = Identifier.of(Noellesroles.MOD_ID, "executioner_select_target");
    public static final CustomPayload.Id<ExecutionerSelectTargetC2SPacket> ID = new CustomPayload.Id<>(EXECUTIONER_SELECT_TARGET_PAYLOAD_ID);
    public static final PacketCodec<RegistryByteBuf, ExecutionerSelectTargetC2SPacket> CODEC;

    public ExecutionerSelectTargetC2SPacket(UUID target) {
        this.target = target;
    }

    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void write(PacketByteBuf buf) {
        buf.writeUuid(this.target);
    }

    public static ExecutionerSelectTargetC2SPacket read(PacketByteBuf buf) {
        return new ExecutionerSelectTargetC2SPacket(buf.readUuid());
    }

    public UUID target() {
        return this.target;
    }

    static {
        CODEC = PacketCodec.of(ExecutionerSelectTargetC2SPacket::write, ExecutionerSelectTargetC2SPacket::read);
    }
}