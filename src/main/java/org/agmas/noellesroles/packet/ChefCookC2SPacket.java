package org.agmas.noellesroles.packet;

import java.util.ArrayList;
import java.util.List;

import org.agmas.noellesroles.Noellesroles;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ChefCookC2SPacket(ArrayList<String> cookInfo) implements CustomPacketPayload {
    public static final Gson gson = new Gson();
    public static final ResourceLocation ABILITY_PAYLOAD_ID = ResourceLocation.fromNamespaceAndPath(Noellesroles.MOD_ID,
            "ability_no");
    public static final Type<ChefCookC2SPacket> ID = new Type<>(ABILITY_PAYLOAD_ID);
    public static final StreamCodec<RegistryFriendlyByteBuf, ChefCookC2SPacket> CODEC;

    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    public void write(FriendlyByteBuf buf) {
        buf.writeUtf(gson.toJson(cookInfo));
    }

    public static ChefCookC2SPacket read(FriendlyByteBuf buf) {
        String data = buf.readUtf();
        ArrayList<String> cookInfos = new ArrayList<>();
        java.lang.reflect.Type type = new TypeToken<List<String>>() {
        }.getType();
        try {
            cookInfos = gson.fromJson(data, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ChefCookC2SPacket(cookInfos);
    }

    static {
        CODEC = StreamCodec.ofMember(ChefCookC2SPacket::write, ChefCookC2SPacket::read);
    }
}