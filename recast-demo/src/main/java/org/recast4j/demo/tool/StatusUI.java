/*
Copyright (c) 2009-2010 Mikko Mononen memon@inside.org
recast4j copyright (c) 2021 Piotr Piastucki piotr@jtilia.org

This software is provided 'as-is', without any express or implied
warranty.  In no event will the authors be held liable for any damages
arising from the use of this software.
Permission is granted to anyone to use this software for any purpose,
including commercial applications, and to alter it and redistribute it
freely, subject to the following restrictions:
1. The origin of this software must not be misrepresented; you must not
 claim that you wrote the original software. If you use this software
 in a product, an acknowledgment in the product documentation would be
 appreciated but is not required.
2. Altered source versions must be plainly marked as such, and must not be
 misrepresented as being the original software.
3. This notice may not be removed or altered from any source distribution.
*/
package org.recast4j.demo.tool;

import org.lwjgl.nuklear.NkColor;
import org.lwjgl.nuklear.NkContext;
import org.lwjgl.nuklear.NkRect;
import org.lwjgl.system.MemoryStack;
import org.recast4j.demo.ui.NuklearUIModule;

import static org.lwjgl.nuklear.Nuklear.*;
import static org.lwjgl.system.MemoryStack.stackPush;

public class StatusUI implements NuklearUIModule {

    private final NkColor white = NkColor.create();
    private boolean enabled = true;
    private String content = "no content set!";

    private long lastUpdateNanos;

    @Override
    public boolean layout(NkContext ctx, int x, int y, int width, int height, int mouseX, int mouseY) {
        boolean mouseInside = false;
        nk_rgb(255, 255, 255, white);
        try (MemoryStack stack = stackPush()) {
            NkRect rect = NkRect.mallocStack(stack);
            if (nk_begin(ctx, "Status", nk_rect(255, 5, 800, 42, rect), NK_WINDOW_BORDER | NK_WINDOW_MOVABLE)) {
                if (enabled) {
                    nk_layout_row_dynamic(ctx, 20, 1);
                    nk_text(ctx, content, NK_TEXT_LEFT);
                }
            }
            nk_end(ctx);
        }
        return mouseInside;
    }

    public NkColor getWhite() {
        return white;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }


    public void handleClick(Tool tool) {
        if (tool != null) {
            String posStatus = tool.getPosStatus();
            this.content = posStatus;
        }
    }

    public void handleUpdate(Tool tool) {
        long nanoTime = System.nanoTime();
        long diff = nanoTime - lastUpdateNanos;
        if (diff < 500_000_000) {
            return;
        }
        lastUpdateNanos = nanoTime;
        if (tool != null) {
            String posStatus = tool.getPosStatus();
            this.content = posStatus;
        }
    }

}
