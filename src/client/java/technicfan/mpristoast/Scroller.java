package technicfan.mpristoast;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class Scroller {
    private static final int maxWidth = 175;
    // scrolls 1 pixel every 96ms
    private static final float scrollRelation = 1f / 96;

    private final String name;
    private final long startTime;

    protected Scroller() {
        this.name = "";
        this.startTime = 0;
    }

    protected Scroller(String name) {
        this.name = name;
        this.startTime = System.currentTimeMillis();
    }

    private float currentOffset(int width) {
        //               2000 +  (width - maxWidth) * 64  +  (width - maxWidth) * 32 
        //             = 2000 +  (width - maxWidth) * 2^6 +  (width - maxWidth) * 2^5
        long roundTime = 2000 + ((width - maxWidth) << 6) + ((width - maxWidth) << 5);
        long time = (System.currentTimeMillis() - startTime) % roundTime - 1000;
        if (time <= 0) {
            return 0;
        } else if (time >= roundTime - 2000) {
            return width - maxWidth;
        }
        return time * scrollRelation;
    }

    public int width(Font font) {
        int width = font.width(name);
        return width <= maxWidth ? width : maxWidth;
    }

    public void draw(GuiGraphics gui, Font font, int x, int y, int color) {
        int width = font.width(name);
        if (width <= maxWidth) {
            gui.drawString(font, Component.nullToEmpty(name), x, y, color);
        } else {
            gui.enableScissor(x, 0, x + maxWidth, y + font.lineHeight);
            gui.pose().pushMatrix();
            gui.pose().translate(x - currentOffset(width), 0);
            gui.drawString(font, name, 0, y, color);
            gui.pose().popMatrix();
            gui.disableScissor();
        }
    }
}
