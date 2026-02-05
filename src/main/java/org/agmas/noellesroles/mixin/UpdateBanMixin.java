package org.agmas.noellesroles.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.net.URL;
import java.net.URLConnection;

@Mixin(URL.class)
public class UpdateBanMixin {
    @Inject(method = "openConnection()Ljava/net/URLConnection;", at = @At("HEAD"), cancellable = true)
    public void openConnection(CallbackInfoReturnable<URLConnection> cir) {
        if (((URL) (Object) this).getHost().contains("github")) {
            cir.cancel();
        }
    }
}
