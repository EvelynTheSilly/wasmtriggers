package name.wasmtriggers.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.input.KeyEvent;

public final class KeyboardEvents {
    private KeyboardEvents() {}

    public static final Event<KeyPress> KEY_PRESS = EventFactory.createArrayBacked(
        KeyPress.class,
        listeners -> (handler, handle, action, event) -> {
            for (KeyPress listener : listeners) {
                listener.onKeyPress(handler, handle, action, event);
            }
        }
    );

    @FunctionalInterface
    public interface KeyPress {
        void onKeyPress(KeyboardHandler handler, long handle, int action, KeyEvent event);
    }
}
