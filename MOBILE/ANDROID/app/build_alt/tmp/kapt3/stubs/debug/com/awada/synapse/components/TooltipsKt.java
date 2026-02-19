package com.awada.synapse.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000\"\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\u001aB\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\u0006\u0010\u0004\u001a\u00020\u00032\u0012\u0010\u0005\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00010\u00062\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0002\u0010\n\u001a\u0004\u0018\u00010\u0003H\u0007\u00a8\u0006\u000b"}, d2 = {"Tooltip", "", "text", "", "primaryButtonText", "onResult", "Lkotlin/Function1;", "Lcom/awada/synapse/components/TooltipResult;", "modifier", "Landroidx/compose/ui/Modifier;", "secondaryButtonText", "app_debug"})
public final class TooltipsKt {
    
    /**
     * Modal tooltip dialog with text and optional buttons.
     *
     * Features:
     * - Centered on screen
     * - Dimmed background (scrim)
     * - AI layer stays on top and remains interactive (not blocked)
     *
     * Tokens:
     * - Background: bg_surface (white)
     * - Scrim: bg_scrim (dimmed)
     * - Radius: Radius_M (24dp)
     * - Text: text_1_level + Body L style
     * - Padding: 20/24dp
     * - Buttons spacing: 12dp (vertical), 16dp (horizontal)
     *
     * @param text Main text content
     * @param onResult Callback with result (Primary/Secondary/Dismissed)
     * @param primaryButtonText Primary button text (right button, required)
     * @param secondaryButtonText Optional secondary button text (left button)
     */
    @androidx.compose.runtime.Composable()
    public static final void Tooltip(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String primaryButtonText, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.awada.synapse.components.TooltipResult, kotlin.Unit> onResult, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.Nullable()
    java.lang.String secondaryButtonText) {
    }
}