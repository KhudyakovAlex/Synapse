package com.awada.synapse.ai;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000$\n\u0000\n\u0002\u0018\u0002\n\u0002\b\r\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0000\u001a$\u0010\u000e\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0013H\u0007\u001a$\u0010\u0015\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\b\b\u0002\u0010\u0014\u001a\u00020\u0013H\u0007\u001a*\u0010\u0016\u001a\u00020\u000f2\b\b\u0002\u0010\u0010\u001a\u00020\u00112\u0006\u0010\u0012\u001a\u00020\u00132\u000e\b\u0002\u0010\u0017\u001a\b\u0012\u0004\u0012\u00020\u000f0\u0018H\u0007\"\u0010\u0010\u0000\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0003\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0004\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0005\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0006\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u0007\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\t\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\n\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\u000b\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\f\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\"\u0010\u0010\r\u001a\u00020\u0001X\u0082\u0004\u00a2\u0006\u0004\n\u0002\u0010\u0002\u00a8\u0006\u0019"}, d2 = {"BUBBLE_MAX_WIDTH", "Landroidx/compose/ui/unit/Dp;", "F", "BUBBLE_PADDING_HORIZONTAL", "BUBBLE_PADDING_VERTICAL", "MESSAGE_PADDING_BOTTOM", "MESSAGE_PADDING_SIDE", "MESSAGE_PADDING_TOP", "QUICK_REPLY_MIN_HEIGHT", "QUICK_REPLY_MIN_WIDTH", "QUICK_REPLY_PADDING_HORIZONTAL", "QUICK_REPLY_PADDING_VERTICAL", "QUICK_REPLY_SPACING", "TIME_PADDING_TOP", "UIMessageAI", "", "modifier", "Landroidx/compose/ui/Modifier;", "text", "", "time", "UIMessageUser", "UIQuickReply", "onClick", "Lkotlin/Function0;", "app_debug"})
public final class ChatMessagesKt {
    private static final float BUBBLE_MAX_WIDTH = 0.0F;
    private static final float BUBBLE_PADDING_HORIZONTAL = 0.0F;
    private static final float BUBBLE_PADDING_VERTICAL = 0.0F;
    private static final float MESSAGE_PADDING_TOP = 0.0F;
    private static final float MESSAGE_PADDING_BOTTOM = 0.0F;
    private static final float MESSAGE_PADDING_SIDE = 0.0F;
    private static final float TIME_PADDING_TOP = 0.0F;
    private static final float QUICK_REPLY_MIN_WIDTH = 0.0F;
    private static final float QUICK_REPLY_MIN_HEIGHT = 0.0F;
    private static final float QUICK_REPLY_PADDING_HORIZONTAL = 0.0F;
    private static final float QUICK_REPLY_PADDING_VERTICAL = 0.0F;
    private static final float QUICK_REPLY_SPACING = 0.0F;
    
    /**
     * AI message bubble - aligned to start (left)
     * Corner radii: topStart=S, topEnd=S, bottomStart=None, bottomEnd=S
     * Time is inside the bubble, aligned to end
     */
    @androidx.compose.runtime.Composable()
    public static final void UIMessageAI(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String time) {
    }
    
    /**
     * User message bubble - aligned to end (right)
     * Corner radii: topStart=S, topEnd=S, bottomStart=S, bottomEnd=None
     * Time is inside the bubble, aligned to end
     */
    @androidx.compose.runtime.Composable()
    public static final void UIMessageUser(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    java.lang.String time) {
    }
    
    /**
     * Quick reply chip - aligned to end (right)
     * Used for suggested responses
     */
    @androidx.compose.runtime.Composable()
    public static final void UIQuickReply(@org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier, @org.jetbrains.annotations.NotNull()
    java.lang.String text, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
    }
}