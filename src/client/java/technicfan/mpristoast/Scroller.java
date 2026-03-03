package technicfan.mpristoast;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class Scroller {
    private static final int maxWidth = 175;
    private static final int scrollTime = 40;
    private static final float scrollStep = 0.4f;
    private static final float scrollRelation = scrollStep / scrollTime;

    private final String name;
    private final int height;
    private final int width;
    private final int fullWidth;
    private final long roundTime;
    private final long startTime;

    protected Scroller() {
        this.name = "";
        this.height = 0;
        this.width = 0;
        this.fullWidth = 0;
        this.roundTime = 0;
        this.startTime = 0;
    }

    protected Scroller(String name, Minecraft client) {
        this.name = name;
        this.startTime = System.currentTimeMillis();
        this.height = client.font.lineHeight;
        this.fullWidth = getWidth(name, client);
        if (fullWidth <= maxWidth) {
            this.width = fullWidth;
            this.roundTime = 0;
        } else {
            this.width = maxWidth;
            this.roundTime = 2000 + scrollTime * (int) ((fullWidth - maxWidth) / scrollStep);
        }
    }

    private float currentOffset() {
        long time = (System.currentTimeMillis() - startTime) % roundTime - 1000;
        if (time <= 0) {
            return 0;
        } else if (time >= roundTime - 2000) {
            return fullWidth - maxWidth;
        }
        return time * scrollRelation;
    }

    private int getWidth(String string, Minecraft client) {
        CompletableFuture<Integer> width = new CompletableFuture<>();
        client.execute(() -> {
            width.complete(client.font.width(string));
        });
        try {
            return width.get();
        } catch (InterruptedException | ExecutionException e) {
            return 0;
        }
    }

    public int width() {
        return width;
    }

    public void draw(GuiGraphics gui, Font font, int x, int y, int color) {
        if (fullWidth == width) {
            gui.drawString(font, Component.nullToEmpty(name), x, y, color);
        } else {
            gui.enableScissor(x, 0, x + width, y + height);
            gui.pose().pushMatrix();
            gui.pose().translate(x - currentOffset(), 0);
            gui.drawString(font, name, 0, y, color);
            gui.pose().popMatrix();
            gui.disableScissor();
        }
    }
}
