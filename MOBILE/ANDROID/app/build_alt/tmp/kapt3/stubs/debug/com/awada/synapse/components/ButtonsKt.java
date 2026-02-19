package com.awada.synapse.components;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u00008\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0002\u001aC\u0010\u0000\u001a\u00020\u00012\u0006\u0010\u0002\u001a\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\t2\n\b\u0003\u0010\n\u001a\u0004\u0018\u00010\u000bH\u0007\u00a2\u0006\u0002\u0010\f\u001a2\u0010\r\u001a\u00020\u00012\u0006\u0010\u000e\u001a\u00020\u000f2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\b\u001a\u00020\tH\u0007\u001a2\u0010\u0010\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0007\u001a2\u0010\u0013\u001a\u00020\u00012\u0006\u0010\b\u001a\u00020\t2\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u00020\u00010\u00052\b\b\u0002\u0010\u0006\u001a\u00020\u00072\b\b\u0002\u0010\u0011\u001a\u00020\u0012H\u0007\u00a8\u0006\u0014"}, d2 = {"KeyboardButton", "", "style", "Lcom/awada/synapse/components/KeyboardButtonStyle;", "onClick", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "text", "", "icon", "", "(Lcom/awada/synapse/components/KeyboardButtonStyle;Lkotlin/jvm/functions/Function0;Landroidx/compose/ui/Modifier;Ljava/lang/String;Ljava/lang/Integer;)V", "PinButton", "state", "Lcom/awada/synapse/components/PinButtonState;", "PrimaryButton", "enabled", "", "SecondaryButton", "app_debug"})
public final class ButtonsKt {
    
    /**
     * Keyboard button component.
     * Size: 88×68dp, Corner radius: 16dp
     *
     * Tokens:
     * - Radius: Radius_S (16dp)
     * - Style=Default: Headline L text style
     * - Style=Help: Label L text style
     * - State=Default background: bg_surface (white) with border_shade_8
     * - State=Pressed background: secondary_pressed (light gray)
     * - Text/Icon color: text_1_level
     */
    @androidx.compose.runtime.Composable()
    public static final void KeyboardButton(@org.jetbrains.annotations.NotNull()
    com.awada.synapse.components.KeyboardButtonStyle style, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @androidx.annotation.DrawableRes()
    @org.jetbrains.annotations.Nullable()
    java.lang.Integer icon) {
    }
    
    /**
     * Primary button component.
     * Filled button with rounded corners (pill shape).
     * Size: height 44dp, width adaptive to content.
     *
     * Tokens:
     * - Radius: Radius_L (40dp)
     * - Text style: Button M (16sp/20sp, Medium weight)
     * - State=Default: primary background + on_primary text
     * - State=Pressed: primary_pressed background + on_primary text
     * - State=Disabled: disabled background + on_disabled text
     */
    @androidx.compose.runtime.Composable()
    public static final void PrimaryButton(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, boolean enabled) {
    }
    
    /**
     * Secondary button component.
     * Outlined button with rounded corners.
     * Size: height 44dp, width adaptive to content (min 80dp).
     *
     * Tokens:
     * - Radius: Radius_M (24dp)
     * - Text style: Button M (16sp/20sp, Medium weight)
     * - State=Default: secondary background + border_primary + on_secondary text
     * - State=Pressed: secondary_pressed background + border_primary + on_secondary text
     * - State=Disabled: disabled background + on_disabled text (no border)
     */
    @androidx.compose.runtime.Composable()
    public static final void SecondaryButton(@org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, boolean enabled) {
    }
    
    /**
     * PIN keyboard button component.
     * Size: 49×56dp, Corner radius: 8dp
     *
     * Tokens:
     * - Radius: Numeric_8 (8dp)
     * - Text style: Headline M (28sp/36sp line height)
     * - State=Default/Input: bg_surface + border_shade_8 + text_1_level
     * - State=Error: error_bg + border_error + text_error
     */
    @androidx.compose.runtime.Composable()
    public static final void PinButton(@org.jetbrains.annotations.NotNull()
    com.awada.synapse.components.PinButtonState state, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text) {
    }
}