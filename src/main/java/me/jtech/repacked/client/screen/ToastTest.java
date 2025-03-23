package me.jtech.repacked.client.screen;

import io.wispforest.owo.ui.base.BaseOwoToast;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.ToastManager;

import java.util.function.Supplier;

public class ToastTest extends BaseOwoToast {
    protected ToastTest(Supplier components, VisibilityPredicate predicate) {
        super(components, predicate);
    }

    @Override
    public Visibility getVisibility() {
        return null;
    }

    @Override
    public void update(ToastManager manager, long time) {

    }

    @Override
    public void draw(DrawContext context, TextRenderer textRenderer, long startTime) {

    }
}
