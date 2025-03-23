package me.jtech.repacked.client.screen;

import io.wispforest.owo.ui.base.BaseOwoToast;

import java.util.function.Supplier;

public class ToastTest extends BaseOwoToast {
    protected ToastTest(Supplier components, VisibilityPredicate predicate) {
        super(components, predicate);
    }
}
