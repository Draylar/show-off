package draylar.showoff;

import draylar.showoff.mixin.HandledScreenAccessor;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.slot.Slot;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ShowOffClient implements ClientModInitializer {

    private static boolean pressed = false;
    public static final KeyBinding SHARE_ITEM =
            KeyBindingHelper.registerKeyBinding(
                    new KeyBinding(
                            "share_item",
                            InputUtil.Type.KEYSYM,
                            GLFW.GLFW_KEY_T,
                            "key.categories.misc"
                    ));

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            if(client.player == null) return;

            // If the Share Item & shift keybinds was pressed, attempt to share the item with the server.
            if(Screen.hasShiftDown() && InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), KeyBindingHelper.getBoundKeyOf(SHARE_ITEM).getCode())) {
                if(pressed) {
                    return;
                }

                // Determine the index of the slot the user is hovering over
                if(client.currentScreen instanceof HandledScreen) {
                    Slot focusedSlot = ((HandledScreenAccessor) client.currentScreen).getFocusedSlot();
                    if(focusedSlot != null) {
                        int id = focusedSlot.id;

                        // Send a packet to the server with the index
                        PacketByteBuf packet = new PacketByteBuf(Unpooled.buffer());
                        packet.writeInt(id);
                        ClientSidePacketRegistry.INSTANCE.sendToServer(ShowOff.REQUEST_ITEM_SHARE, packet);
                    }
                }

                // mark as pressed down so that we don't duplicate the message
                pressed = true;
            } else {
                pressed = false;
            }
        });
    }
}
