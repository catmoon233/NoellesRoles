package org.agmas.noellesroles.packet;

import org.agmas.noellesroles.Noellesroles;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * 傀儡师技能包
 * 用于客户端请求使用假人技能
 */
public record PuppeteerC2SPacket(Action action) implements CustomPayload {
    
    public static final Id<PuppeteerC2SPacket> ID = new Id<>(
        Identifier.of(Noellesroles.MOD_ID, "puppeteer_ability")
    );
    
    public static final PacketCodec<RegistryByteBuf, PuppeteerC2SPacket> CODEC = PacketCodec.tuple(
        PacketCodecs.VAR_INT.xmap(Action::fromId, Action::getId),
        PuppeteerC2SPacket::action,
        PuppeteerC2SPacket::new
    );
    
    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }
    
    public enum Action {
        USE_PUPPET(0),      // 使用假人技能
        RETURN_TO_BODY(1);  // 主动返回本体
        
        private final int id;
        
        Action(int id) {
            this.id = id;
        }
        
        public int getId() {
            return id;
        }
        
        public static Action fromId(int id) {
            return switch (id) {
                case 1 -> RETURN_TO_BODY;
                default -> USE_PUPPET;
            };
        }
    }
}