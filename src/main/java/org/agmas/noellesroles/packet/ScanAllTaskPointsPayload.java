package org.agmas.noellesroles.packet;

import java.util.HashMap;
import java.util.UUID;

import org.agmas.noellesroles.Noellesroles;

import com.google.gson.reflect.TypeToken;

import dev.doctor4t.trainmurdermystery.data.MapConfig;
import dev.doctor4t.trainmurdermystery.game.GameFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ScanAllTaskPointsPayload(HashMap<BlockPos, Integer> taskBlocks) implements CustomPacketPayload {
    public static final ResourceLocation ABILITY_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID,
            "client_scan_task");
    public static final Type<ScanAllTaskPointsPayload> ID = new Type<>(ABILITY_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ScanAllTaskPointsPayload> CODEC;

    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(MapConfig.gson.toJson(this.taskBlocks()));
    }

    public static ScanAllTaskPointsPayload read(FriendlyByteBuf buf) {
        String data = buf.readUtf();
        java.lang.reflect.Type type = new TypeToken<HashMap<BlockPos, Integer>>() {
        }.getType();
        HashMap<BlockPos, Integer> returnData = new HashMap<>();
        try {
            returnData = MapConfig.gson.fromJson(data, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ScanAllTaskPointsPayload(returnData);
    }

    static {
        CODEC = StreamCodec.ofMember(ScanAllTaskPointsPayload::write, ScanAllTaskPointsPayload::read);
    }
}