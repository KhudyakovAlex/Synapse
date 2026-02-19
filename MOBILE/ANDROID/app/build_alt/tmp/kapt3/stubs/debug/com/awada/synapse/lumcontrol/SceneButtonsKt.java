package com.awada.synapse.lumcontrol;

@kotlin.Metadata(mv = {1, 9, 0}, k = 2, xi = 48, d1 = {"\u0000B\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\b\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\u001aH\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u00022\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\b\b\u0002\u0010\u000b\u001a\u00020\t2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00060\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u001aI\u0010\u0010\u001a\u00020\u00062\u000e\b\u0002\u0010\u0011\u001a\b\u0012\u0004\u0012\u00020\u00020\u00012\n\b\u0002\u0010\u0012\u001a\u0004\u0018\u00010\u00132\u0014\b\u0002\u0010\u0014\u001a\u000e\u0012\u0004\u0012\u00020\u0002\u0012\u0004\u0012\u00020\u00060\u00152\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u00a2\u0006\u0002\u0010\u0016\u001a>\u0010\u0017\u001a\u00020\u00062\u0006\u0010\u0018\u001a\u00020\u00192\b\b\u0002\u0010\b\u001a\u00020\t2\b\b\u0002\u0010\n\u001a\u00020\t2\u000e\b\u0002\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00060\r2\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\u001a8\u0010\u001a\u001a\u00020\u00062\u000e\b\u0002\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u00190\u00012\u0014\b\u0002\u0010\u001c\u001a\u000e\u0012\u0004\u0012\u00020\u0019\u0012\u0004\u0012\u00020\u00060\u00152\b\b\u0002\u0010\u000e\u001a\u00020\u000fH\u0007\"\u0017\u0010\u0000\u001a\b\u0012\u0004\u0012\u00020\u00020\u0001\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0003\u0010\u0004\u00a8\u0006\u001d"}, d2 = {"defaultQuickButtons", "", "Lcom/awada/synapse/lumcontrol/QuickButtonItem;", "getDefaultQuickButtons", "()Ljava/util/List;", "QuickButton", "", "item", "isSelected", "", "isEnabled", "isLarge", "onSelected", "Lkotlin/Function0;", "modifier", "Landroidx/compose/ui/Modifier;", "QuickButtonsRow", "buttons", "selectedId", "", "onButtonSelected", "Lkotlin/Function1;", "(Ljava/util/List;Ljava/lang/Integer;Lkotlin/jvm/functions/Function1;Landroidx/compose/ui/Modifier;)V", "SceneButton", "scene", "Lcom/awada/synapse/lumcontrol/LightScene;", "SceneButtonsPanel", "scenes", "onSceneSelected", "app_debug"})
public final class SceneButtonsKt {
    @org.jetbrains.annotations.NotNull()
    private static final java.util.List<com.awada.synapse.lumcontrol.QuickButtonItem> defaultQuickButtons = null;
    
    /**
     * Scene button with 3 states: Default, Pressed, Disabled
     */
    @androidx.compose.runtime.Composable()
    public static final void SceneButton(@org.jetbrains.annotations.NotNull()
    com.awada.synapse.lumcontrol.LightScene scene, boolean isSelected, boolean isEnabled, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSelected, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Panel with scene buttons
     */
    @androidx.compose.runtime.Composable()
    public static final void SceneButtonsPanel(@org.jetbrains.annotations.NotNull()
    java.util.List<com.awada.synapse.lumcontrol.LightScene> scenes, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.awada.synapse.lumcontrol.LightScene, kotlin.Unit> onSceneSelected, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Individual quick button (Off, 1, 2, 3, On)
     */
    @androidx.compose.runtime.Composable()
    public static final void QuickButton(@org.jetbrains.annotations.NotNull()
    com.awada.synapse.lumcontrol.QuickButtonItem item, boolean isSelected, boolean isEnabled, boolean isLarge, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function0<kotlin.Unit> onSelected, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    /**
     * Quick buttons row (Off, 1, 2, 3, On)
     */
    @androidx.compose.runtime.Composable()
    public static final void QuickButtonsRow(@org.jetbrains.annotations.NotNull()
    java.util.List<com.awada.synapse.lumcontrol.QuickButtonItem> buttons, @org.jetbrains.annotations.Nullable()
    java.lang.Integer selectedId, @org.jetbrains.annotations.NotNull()
    kotlin.jvm.functions.Function1<? super com.awada.synapse.lumcontrol.QuickButtonItem, kotlin.Unit> onButtonSelected, @org.jetbrains.annotations.NotNull()
    androidx.compose.ui.Modifier modifier) {
    }
    
    @org.jetbrains.annotations.NotNull()
    public static final java.util.List<com.awada.synapse.lumcontrol.QuickButtonItem> getDefaultQuickButtons() {
        return null;
    }
}