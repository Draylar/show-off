package draylar.showoff;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.MessageType;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

public class ShowOff implements ModInitializer {

    public static final Identifier REQUEST_ITEM_SHARE = new Identifier("showoff", "request_item_share");

    @Override
    public void onInitialize() {
        ServerSidePacketRegistry.INSTANCE.register(REQUEST_ITEM_SHARE, (context, buffer) -> {
            int index = buffer.readInt();

            // grab the item from the container the user is currently in
            context.getTaskQueue().execute(() -> {
                PlayerEntity player = context.getPlayer();
                ScreenHandler currentScreenHandler = player.currentScreenHandler;

                if(currentScreenHandler != null) {
                    try {
                        Slot slot = currentScreenHandler.getSlot(index);

                        // ensure the index was valid
                        if (slot != null) {
                            ItemStack stack = slot.getStack();

                            // do not print air
                            if(!stack.isEmpty()) {
                                Text text = stack.toHoverableText();
                                LiteralText prefix = new LiteralText(String.format("<%s> ", player.getDisplayName().asString()));
                                prefix.append(text);
                                player.world.getServer().getPlayerManager().broadcastChatMessage(prefix, MessageType.CHAT, player.getUuid());
                            }
                        }
                    } catch (Exception e) {
                        // NO-OP to catch malicious clients trying to index oob the server
                    }
                }
            });
        });
    }
}
