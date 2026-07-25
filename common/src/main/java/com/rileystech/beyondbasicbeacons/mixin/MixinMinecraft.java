package com.rileystech.beyondbasicbeacons.mixin;

import com.rileystech.beyondbasicbeacons.Constants;
import com.rileystech.beyondbasicbeacons.platform.Services;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(CallbackInfo info) {

        Constants.LOG.info("This line is printed by an example mod common mixin!");
        Constants.LOG.info("MC Version: {}", SharedConstants.getCurrentVersion().name());

        if (Services.PLATFORM.isDevelopmentEnvironment()) {
            Minecraft client = Minecraft.getInstance();

            if (client.getLevelSource().levelExists("world")) {
                client.createWorldOpenFlows().openWorld("world", () -> {});
            }
        }
    }
}