package com.awada.synapse.activities

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.awada.synapse.R
import com.awada.synapse.ai.AI
import com.awada.synapse.ai.LLMActionCommand
import com.awada.synapse.ai.LLMDebugLog
import com.awada.synapse.ai.LLMCurrentScreenContext
import com.awada.synapse.ai.LLMCurrentScreenParams
import com.awada.synapse.ai.LLMNavigationCommand
import com.awada.synapse.ai.LLMUiContext
import com.awada.synapse.ai.loadSystemPrompt
import com.awada.synapse.components.TooltipOverlayState
import com.awada.synapse.components.Tooltip
import com.awada.synapse.components.TooltipResult
import com.awada.synapse.logdog.Logdog
import com.awada.synapse.lumcontrol.LumControlLayer
import com.awada.synapse.pages.LocalBottomOverlayInset
import com.awada.synapse.pages.PageChangePassword
import com.awada.synapse.pages.PageGraph
import com.awada.synapse.pages.PageGraphs
import com.awada.synapse.pages.PageIconSelect
import com.awada.synapse.pages.PageInitializeController
import com.awada.synapse.pages.PageLum
import com.awada.synapse.pages.PageLumSettings
import com.awada.synapse.pages.PageLocation
import com.awada.synapse.pages.PageLocationSettings
import com.awada.synapse.pages.PageLocations
import com.awada.synapse.pages.PageButtonPanel
import com.awada.synapse.pages.PageButtonSettings
import com.awada.synapse.pages.PagePassword
import com.awada.synapse.pages.PageGroup
import com.awada.synapse.pages.PageButtonPanelSettings
import com.awada.synapse.pages.PageRoom
import com.awada.synapse.pages.PageRoomSettings
import com.awada.synapse.pages.PageScenario
import com.awada.synapse.pages.PageSchedule
import com.awada.synapse.pages.PageSchedulePoint
import com.awada.synapse.pages.PageSearch
import com.awada.synapse.pages.PageSensorBrightSettings
import com.awada.synapse.pages.PageSensorPressSettings
import com.awada.synapse.pages.PageSettings
import com.awada.synapse.pages.LocalAiChatExpanded
import com.awada.synapse.data.IconCatalogManager
import com.awada.synapse.ui.theme.PixsoColors
import com.awada.synapse.ui.theme.SynapseTheme
import com.awada.synapse.db.AppDatabase
import com.awada.synapse.db.ButtonEntity
import com.awada.synapse.db.BrightSensorEntity
import com.awada.synapse.db.ButtonPanelEntity
import com.awada.synapse.db.ControllerEntity
import com.awada.synapse.db.LuminaireEntity
import com.awada.synapse.db.LuminaireTypeEntity
import com.awada.synapse.db.PresSensorEntity
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LLMDebugLog.clearOnProcessStart()
        val systemPromptMd = loadSystemPrompt(applicationContext)
        Logdog.i(
            message = "process_start",
            fields = mapOf(
                "versionName" to com.awada.synapse.BuildConfig.VERSION_NAME,
                "versionCode" to com.awada.synapse.BuildConfig.VERSION_CODE,
                "debug" to com.awada.synapse.BuildConfig.DEBUG,
                "systemPromptChars" to systemPromptMd.length,
            ),
            attachments = listOf(
                Logdog.Attachment(
                    kind = "md",
                    name = "llm-system-prompt.md",
                    content = systemPromptMd
                )
            )
        )
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(PixsoColors.Color_Bg_bg_elevated.toArgb())
        )
        lifecycleScope.launch {
            if (savedInstanceState == null) {
                AppDatabase.getInstance(applicationContext).aiMessageDao().clear()
            }
            setContent {
                SynapseTheme {
                    MainContent()
                }
            }
        }
    }
}

enum class AppScreen {
    Location,
    LocationDetails,
    InitializeController,
    RoomDetails,
    GroupDetails,
    RoomSettings,
    LocationSettings,
    Lum,
    Search,
    Settings,
    LumSettings,
    SensorPressSettings,
    SensorBrightSettings,
    ButtonPanelSettings,
    ButtonSettings,
    Scenario,
    Panel,
    Password,
    ChangePassword,
    Schedule,
    SchedulePoint,
    Graphs,
    Graph,
    IconSelect
}

private val AppScreen.llmScreenName: String
    get() = when (this) {
        AppScreen.Location -> "Locations"
        AppScreen.LocationDetails -> "Location"
        AppScreen.RoomDetails -> "Room"
        AppScreen.GroupDetails -> "Group"
        else -> name
    }

private fun llmScreenToAppScreen(screenName: String): AppScreen? = when (screenName) {
    "Locations" -> AppScreen.Location
    "Location", AppScreen.LocationDetails.name -> AppScreen.LocationDetails
    "Room", AppScreen.RoomDetails.name -> AppScreen.RoomDetails
    "Group", AppScreen.GroupDetails.name -> AppScreen.GroupDetails
    else -> AppScreen.entries.firstOrNull { it.name == screenName }
}

@Composable
private fun MainContent() {
    var currentScreen by remember { mutableStateOf(AppScreen.Location) }
    val context = LocalContext.current
    val db = remember { AppDatabase.getInstance(context) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    var lastBackPressTime by remember { mutableLongStateOf(0L) }
    var lumBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var systemSettingsBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var settingsLumBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var buttonPanelBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var buttonSettingsBackTarget by remember { mutableStateOf(AppScreen.Panel) }
    var scenarioBackTarget by remember { mutableStateOf(AppScreen.Location) }
    var roomSettingsBackTarget by remember { mutableStateOf(AppScreen.RoomDetails) }
    var groupBackTarget by remember { mutableStateOf(AppScreen.LocationDetails) }
    var changePasswordBackTarget by remember { mutableStateOf(AppScreen.LocationSettings) }
    var scheduleBackTarget by remember { mutableStateOf(AppScreen.LocationSettings) }
    var schedulePointBackTarget by remember { mutableStateOf(AppScreen.Schedule) }
    var graphsBackTarget by remember { mutableStateOf(AppScreen.LocationSettings) }
    var graphBackTarget by remember { mutableStateOf(AppScreen.Graphs) }
    var iconSelectBackTarget by remember { mutableStateOf(AppScreen.LocationSettings) }
    var controllerInitializationBackTarget by remember { mutableStateOf(AppScreen.LocationDetails) }
    var shouldResetDevicesOnInitialization by remember { mutableStateOf(false) }
    var appearingLocationRoomId by remember { mutableStateOf<Int?>(null) }
    var appearingLocationId by remember { mutableStateOf<Int?>(null) }
    var selectedLocation by remember {
        mutableStateOf<com.awada.synapse.components.LocationItem?>(null)
    }
    var selectedRoom by remember {
        mutableStateOf<RoomState?>(null)
    }
    var selectedGroup by remember {
        mutableStateOf<GroupState?>(null)
    }
    var selectedLuminaireId by remember { mutableStateOf<Long?>(null) }
    var selectedButtonPanelId by remember { mutableStateOf<Long?>(null) }
    var selectedButtonNumber by remember { mutableStateOf<Int?>(null) }
    var selectedScenarioId by remember { mutableStateOf<Long?>(null) }
    var selectedPresSensorId by remember { mutableStateOf<Long?>(null) }
    var selectedBrightSensorId by remember { mutableStateOf<Long?>(null) }
    var selectedGraphId by remember { mutableStateOf<Long?>(null) }
    var selectedEventId by remember { mutableStateOf<Long?>(null) }
    var iconSelectCategory by remember { mutableStateOf<String?>(null) }
    var iconSelectCurrentIconId by remember { mutableStateOf<Int?>(null) }
    var pendingSaveSceneNum by remember { mutableStateOf<Int?>(null) }
    var pendingDeleteLocationId by remember { mutableStateOf<Int?>(null) }
    var pendingDeleteLocationTitle by remember { mutableStateOf<String?>(null) }
    var pendingReinitializeControllerId by remember { mutableStateOf<Int?>(null) }
    var pendingReinitializeControllerTitle by remember { mutableStateOf<String?>(null) }
    // For vertical centering between AppBar (top) and LumControlLayer (bottom)
    var rootHeightPx by remember { mutableFloatStateOf(0f) }
    var lumPanelTopPx by remember { mutableFloatStateOf(Float.NaN) }
    var aiPanelTopPx by remember { mutableFloatStateOf(Float.NaN) }
    var isAiChatExpanded by remember { mutableStateOf(false) }
    var aiChatCollapseRequestKey by remember { mutableStateOf(0) }
    var connectedControllerId by remember { mutableStateOf<Int?>(null) }
    val lumBottomInsetDp by remember {
        derivedStateOf {
            if (!lumPanelTopPx.isFinite() || rootHeightPx <= 0f) return@derivedStateOf 0.dp
            with(density) { (rootHeightPx - lumPanelTopPx).coerceAtLeast(0f).toDp() }
        }
    }
    var aiBottomInsetDp by remember { mutableStateOf(0.dp) }
    suspend fun loadLocationItem(controllerId: Int): com.awada.synapse.components.LocationItem? {
        val controller = db.controllerDao().getById(controllerId) ?: return null
        return com.awada.synapse.components.LocationItem(
            title = controller.name.ifBlank { "Локация" },
            iconResId = com.awada.synapse.components.iconResId(context, controller.icoNum),
            controllerId = controller.id
        )
    }
    suspend fun loadRoomState(controllerId: Int, roomId: Int): RoomState? {
        val room = db.roomDao().getById(controllerId, roomId) ?: return null
        return RoomState(
            controllerId = room.controllerId,
            roomId = room.id,
            title = room.name.ifBlank { "Помещение ${room.id + 1}" },
            iconId = room.icoNum
        )
    }
    suspend fun applySelectedIcon(iconCategory: String, iconId: Int) {
        when (iconCategory) {
            "controller" -> {
                val controllerId = selectedLocation?.controllerId ?: return
                val controller = db.controllerDao().getById(controllerId) ?: return
                db.controllerDao().update(controller.copy(icoNum = iconId))
                selectedLocation = loadLocationItem(controllerId)
            }
            "room" -> {
                val room = selectedRoom ?: return
                val entity = db.roomDao().getById(room.controllerId, room.roomId) ?: return
                db.roomDao().update(entity.copy(icoNum = iconId))
                selectedRoom = room.copy(iconId = iconId)
            }
            "luminaire" -> {
                val luminaireId = selectedLuminaireId ?: return
                db.luminaireDao().setIcoNum(luminaireId, iconId)
            }
        }
    }
    suspend fun resolveControllerIdFromParams(params: LLMCurrentScreenParams): Int? {
        return params.controllerId
            ?: params.luminaireId?.let { db.luminaireDao().getById(it)?.controllerId }
            ?: params.buttonPanelId?.let { db.buttonPanelDao().getById(it)?.controllerId }
            ?: params.presSensorId?.let { db.presSensorDao().getById(it)?.controllerId }
            ?: params.brightSensorId?.let { db.brightSensorDao().getById(it)?.controllerId }
            ?: params.scenarioId?.let { db.scenarioDao().getById(it)?.controllerId }
            ?: params.graphId?.let { db.graphDao().getById(it)?.controllerId }
            ?: params.eventId?.let { db.eventDao().getById(it)?.controllerId }
    }
    fun startFindControllerFlow() {
        currentScreen = AppScreen.Search
    }
    suspend fun completeFoundControllerFlow() {
        val dao = db.controllerDao()
        val suffix = (System.currentTimeMillis() % 100_000_000)
            .toString()
            .padStart(8, '0')
        val name = "SYN_$suffix"
        val controllerId = dao.insertAtEnd(
            ControllerEntity(
                name = name,
                password = "1234",
                icoNum = 100
            )
        ).toInt()
        appearingLocationId = controllerId
        currentScreen = AppScreen.Location
    }
    suspend fun deleteLocationById(controllerId: Int) {
        clearDevicesForController(db = db, controllerId = controllerId)
        db.controllerDao().deleteById(controllerId)
        pendingDeleteLocationId = null
        pendingDeleteLocationTitle = null
        selectedLocation = null
        selectedRoom = null
        selectedGroup = null
        selectedLuminaireId = null
        selectedButtonPanelId = null
        selectedButtonNumber = null
        selectedScenarioId = null
        selectedPresSensorId = null
        selectedBrightSensorId = null
        selectedGraphId = null
        selectedEventId = null
        iconSelectCategory = null
        iconSelectCurrentIconId = null
        currentScreen = AppScreen.Location
    }
    suspend fun hasAnyControllerDevices(controllerId: Int): Boolean {
        return db.luminaireDao().observeCountForController(controllerId).first() +
            db.buttonPanelDao().observeCountForController(controllerId).first() +
            db.presSensorDao().observeCountForController(controllerId).first() +
            db.brightSensorDao().observeCountForController(controllerId).first() > 0
    }
    suspend fun isControllerConnected(controllerId: Int): Boolean {
        return db.controllerDao().getById(controllerId)?.isConnected == true
    }
    suspend fun startControllerReinitialization(controllerId: Int) {
        val location = loadLocationItem(controllerId) ?: return
        selectedLocation = location
        pendingReinitializeControllerId = null
        pendingReinitializeControllerTitle = null
        shouldResetDevicesOnInitialization = true
        controllerInitializationBackTarget = AppScreen.LocationDetails
        currentScreen = AppScreen.InitializeController
    }
    fun canOpenWhenDisconnected(screen: AppScreen): Boolean = when (screen) {
        AppScreen.Location,
        AppScreen.LocationDetails,
        AppScreen.Search,
        AppScreen.Settings,
        AppScreen.Password -> true
        else -> false
    }
    val llmCurrentScreenParams = when (currentScreen) {
        AppScreen.Location -> LLMCurrentScreenParams()
        AppScreen.LocationDetails -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId
        )
        AppScreen.InitializeController -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId
        )
        AppScreen.RoomDetails -> LLMCurrentScreenParams(
            controllerId = selectedRoom?.controllerId,
            roomId = selectedRoom?.roomId
        )
        AppScreen.GroupDetails -> LLMCurrentScreenParams(
            controllerId = selectedGroup?.controllerId,
            groupId = selectedGroup?.groupId
        )
        AppScreen.RoomSettings -> LLMCurrentScreenParams(
            controllerId = selectedRoom?.controllerId,
            roomId = selectedRoom?.roomId
        )
        AppScreen.LocationSettings -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId
        )
        AppScreen.Lum -> LLMCurrentScreenParams(
            luminaireId = selectedLuminaireId
        )
        AppScreen.Search -> LLMCurrentScreenParams()
        AppScreen.Settings -> LLMCurrentScreenParams()
        AppScreen.LumSettings -> LLMCurrentScreenParams(
            luminaireId = selectedLuminaireId
        )
        AppScreen.SensorPressSettings -> LLMCurrentScreenParams(
            presSensorId = selectedPresSensorId
        )
        AppScreen.SensorBrightSettings -> LLMCurrentScreenParams(
            brightSensorId = selectedBrightSensorId
        )
        AppScreen.ButtonPanelSettings -> LLMCurrentScreenParams(
            buttonPanelId = selectedButtonPanelId
        )
        AppScreen.ButtonSettings -> LLMCurrentScreenParams(
            buttonPanelId = selectedButtonPanelId,
            buttonNumber = selectedButtonNumber
        )
        AppScreen.Scenario -> LLMCurrentScreenParams(
            buttonPanelId = selectedButtonPanelId,
            scenarioId = selectedScenarioId
        )
        AppScreen.Panel -> LLMCurrentScreenParams(
            buttonPanelId = selectedButtonPanelId
        )
        AppScreen.Password -> LLMCurrentScreenParams()
        AppScreen.ChangePassword -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId
        )
        AppScreen.Schedule -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId ?: selectedRoom?.controllerId
        )
        AppScreen.SchedulePoint -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId ?: selectedRoom?.controllerId,
            eventId = selectedEventId
        )
        AppScreen.Graphs -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId ?: selectedRoom?.controllerId
        )
        AppScreen.Graph -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId ?: selectedRoom?.controllerId,
            graphId = selectedGraphId
        )
        AppScreen.IconSelect -> LLMCurrentScreenParams(
            controllerId = selectedLocation?.controllerId ?: selectedRoom?.controllerId,
            roomId = selectedRoom?.roomId,
            luminaireId = selectedLuminaireId,
            iconCategory = iconSelectCategory,
            currentIconId = iconSelectCurrentIconId
        )
    }
    val llmUiContext = LLMUiContext(
        currentScreen = LLMCurrentScreenContext(
            name = currentScreen.llmScreenName,
            params = llmCurrentScreenParams
        )
    )
    LaunchedEffect(currentScreen, llmCurrentScreenParams) {
        val resolvedControllerId = when (currentScreen) {
            AppScreen.Location,
            AppScreen.Search,
            AppScreen.Settings,
            AppScreen.Password -> null
            else -> resolveControllerIdFromParams(llmCurrentScreenParams)
        }
        connectedControllerId = resolvedControllerId
        db.controllerDao().clearConnections()
        if (resolvedControllerId != null) {
            db.controllerDao().setIsConnected(resolvedControllerId, true)
        }
    }
    val selectedLuminaireOrNull by remember(db, selectedLuminaireId) {
        if (selectedLuminaireId == null) {
            flowOf<LuminaireEntity?>(null)
        } else {
            db.luminaireDao().observeById(selectedLuminaireId!!)
        }
    }.collectAsState(initial = null)
    val lumControlLuminaires by remember(
        db,
        currentScreen,
        selectedLuminaireId,
        selectedLocation?.controllerId,
        selectedRoom?.controllerId,
        selectedRoom?.roomId,
        selectedGroup?.controllerId,
        selectedGroup?.groupId
    ) {
        when (currentScreen) {
            AppScreen.Lum -> {
                if (selectedLuminaireId == null) {
                    flowOf(emptyList())
                } else {
                    db.luminaireDao().observeById(selectedLuminaireId!!).map { luminaire ->
                        luminaire?.let { listOf(it) } ?: emptyList()
                    }
                }
            }
            AppScreen.LocationDetails -> {
                val controllerId = selectedLocation?.controllerId
                if (controllerId == null) {
                    flowOf(emptyList())
                } else {
                    db.luminaireDao().observeAllForController(controllerId)
                }
            }
            AppScreen.RoomDetails -> {
                val room = selectedRoom
                if (room == null) {
                    flowOf(emptyList())
                } else {
                    db.luminaireDao().observeAll(room.controllerId, room.roomId)
                }
            }
            AppScreen.GroupDetails -> {
                val group = selectedGroup
                if (group == null) {
                    flowOf(emptyList())
                } else {
                    db.luminaireDao().observeAllForGroup(group.controllerId, group.groupId)
                }
            }
            else -> flowOf(emptyList())
        }
    }.collectAsState(initial = emptyList())
    val lumControlBrightnessValue by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            if (lumControlLuminaires.isEmpty()) {
                0f
            } else {
                lumControlLuminaires
                    .map { it.bright }
                    .average()
                    .roundToInt()
                    .toFloat()
            }
        }
    }
    val lumControlColorValue by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            if (lumControlLuminaires.isEmpty()) {
                0f
            } else {
                lumControlLuminaires
                    .map { it.hue }
                    .average()
                    .roundToInt()
                    .toFloat()
            }
        }
    }
    val lumControlSaturationValue by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            if (lumControlLuminaires.isEmpty()) {
                0f
            } else {
                lumControlLuminaires
                    .map { it.saturation }
                    .average()
                    .roundToInt()
                    .toFloat()
            }
        }
    }
    val lumControlTemperatureValue by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            if (lumControlLuminaires.isEmpty()) {
                3500f
            } else {
                lumControlLuminaires
                    .map { if (it.temperature > 0) it.temperature else 3500 }
                    .average()
                    .roundToInt()
                    .toFloat()
            }
        }
    }
    val lumControlBrightnessEnabled by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            when (currentScreen) {
                AppScreen.Lum,
                AppScreen.LocationDetails,
                AppScreen.RoomDetails,
                AppScreen.GroupDetails -> lumControlLuminaires.isNotEmpty()
                else -> false
            }
        }
    }
    val lumControlSliders by remember(currentScreen, lumControlLuminaires) {
        derivedStateOf {
            if (currentScreen != AppScreen.Lum &&
                currentScreen != AppScreen.LocationDetails &&
                currentScreen != AppScreen.RoomDetails &&
                currentScreen != AppScreen.GroupDetails
            ) {
                emptyList()
            } else {
                buildList {
                    val typeIds = lumControlLuminaires.map { it.typeId }.toSet()
                    if (LuminaireTypeEntity.TYPE_RGB in typeIds) {
                        add("Color")
                        add("Saturation")
                    }
                    if (LuminaireTypeEntity.TYPE_TW in typeIds) {
                        add("Temperature")
                    }
                    if (typeIds.isNotEmpty()) {
                        add("Brightness")
                    }
                }
            }
        }
    }
    // Hide group lighting controls when the group has no luminaires.
    val isLumControlVisible by remember {
        derivedStateOf {
            when (currentScreen) {
                AppScreen.Lum,
                AppScreen.LocationDetails,
                AppScreen.RoomDetails -> true
                AppScreen.GroupDetails -> lumControlLuminaires.isNotEmpty()
                else -> false
            }
        }
    }

    // Handle system back button
    BackHandler(enabled = !isAiChatExpanded) {
        when (currentScreen) {
            AppScreen.LumSettings -> currentScreen = settingsLumBackTarget
            AppScreen.SensorPressSettings, AppScreen.SensorBrightSettings, AppScreen.ButtonPanelSettings -> currentScreen = systemSettingsBackTarget
            AppScreen.ButtonSettings -> currentScreen = buttonSettingsBackTarget
            AppScreen.Scenario -> currentScreen = scenarioBackTarget
            AppScreen.Panel -> currentScreen = buttonPanelBackTarget
            AppScreen.ChangePassword -> currentScreen = changePasswordBackTarget
            AppScreen.Schedule -> currentScreen = scheduleBackTarget
            AppScreen.SchedulePoint -> currentScreen = schedulePointBackTarget
            AppScreen.Graphs -> currentScreen = graphsBackTarget
            AppScreen.Graph -> currentScreen = graphBackTarget
            AppScreen.IconSelect -> currentScreen = iconSelectBackTarget
            AppScreen.InitializeController -> {
                shouldResetDevicesOnInitialization = false
                currentScreen = controllerInitializationBackTarget
            }
            AppScreen.Lum, AppScreen.Search, AppScreen.Settings, AppScreen.Password -> {
                currentScreen = if (currentScreen == AppScreen.Lum) lumBackTarget else AppScreen.Location
            }
            AppScreen.LocationSettings -> {
                currentScreen = AppScreen.LocationDetails
            }
            AppScreen.RoomDetails -> {
                currentScreen = AppScreen.LocationDetails
            }
            AppScreen.GroupDetails -> {
                currentScreen = groupBackTarget
            }
            AppScreen.RoomSettings -> {
                currentScreen = roomSettingsBackTarget
            }
            AppScreen.LocationDetails -> {
                currentScreen = AppScreen.Location
            }
            AppScreen.Location -> {
                // If on Location, handle exit with double press
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastBackPressTime < 2000) {
                    (context as? ComponentActivity)?.finish()
                } else {
                    lastBackPressTime = currentTime
                    Toast.makeText(context, "Нажмите ещё раз для выхода", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    CompositionLocalProvider(LocalAiChatExpanded provides isAiChatExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    rootHeightPx = coords.size.height.toFloat()
                }
        ) {
        // Cache the smallest observed AI inset (collapsed panel). When AI expands, we
        // don't want pages to "jump" their layout based on the temporary expanded top.
        val aiInsetCandidateDp by remember {
            derivedStateOf {
                if (!aiPanelTopPx.isFinite() || rootHeightPx <= 0f) return@derivedStateOf 0.dp
                with(density) { (rootHeightPx - aiPanelTopPx).coerceAtLeast(0f).toDp() }
            }
        }
        LaunchedEffect(aiInsetCandidateDp, aiBottomInsetDp) {
            if (aiInsetCandidateDp > 0.dp && (aiBottomInsetDp == 0.dp || aiInsetCandidateDp < aiBottomInsetDp)) {
                aiBottomInsetDp = aiInsetCandidateDp
            }
        }

        // Navigation between pages with animation.
        //
        // Important: AnimatedContent keeps both initial+target screens composed during the transition.
        // If we compute bottom inset from `currentScreen` outside of AnimatedContent, the outgoing screen
        // may "jump" its layout when overlays (e.g. LumControlLayer) become visible for the target screen.
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (initialState == AppScreen.Lum && targetState != AppScreen.Lum) {
                    (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(
                        fadeOut(animationSpec = tween(durationMillis = 0))
                    )
                } else if (targetState != AppScreen.Location) {
                    // Slide in from right when going away from locations
                    (slideInHorizontally { it } + fadeIn()).togetherWith(
                        slideOutHorizontally { -it / 3 } + fadeOut()
                    )
                } else {
                    // Slide in from left when going back to locations
                    (slideInHorizontally { -it / 3 } + fadeIn()).togetherWith(
                        slideOutHorizontally { it } + fadeOut()
                    )
                }.using(SizeTransform(clip = false))
            },
            label = "ScreenNavigation"
        ) { screen ->
            val isLumControlVisibleForScreen = when (screen) {
                AppScreen.Lum, AppScreen.LocationDetails, AppScreen.RoomDetails -> true
                AppScreen.GroupDetails -> lumControlLuminaires.isNotEmpty()
                else -> false
            }
            val bottomOverlayInsetForScreen = maxOf(
                aiBottomInsetDp,
                if (isLumControlVisibleForScreen) lumBottomInsetDp else 0.dp
            )

            CompositionLocalProvider(LocalBottomOverlayInset provides bottomOverlayInsetForScreen) {
                when (screen) {
                    AppScreen.Location -> {
                        PageLocations(
                            onSettingsClick = { currentScreen = AppScreen.Settings },
                            onLocationClick = { item ->
                                selectedLocation = item
                                currentScreen = AppScreen.LocationDetails
                            },
                            onFindControllerClick = { startFindControllerFlow() },
                            appearingLocationId = appearingLocationId,
                            onAppearingLocationConsumed = { controllerId ->
                                if (appearingLocationId == controllerId) {
                                    appearingLocationId = null
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LocationDetails -> {
                        val loc = selectedLocation
                            ?: com.awada.synapse.components.LocationItem(
                                title = "Локация",
                                iconResId = R.drawable.controller_100_default
                            )
                        PageLocation(
                            location = loc,
                            onBackClick = { currentScreen = AppScreen.Location },
                            onSettingsClick = { currentScreen = AppScreen.LocationSettings },
                            onRoomClick = { roomId, roomTitle, roomIconId ->
                                val cid = loc.controllerId ?: return@PageLocation
                                selectedRoom = RoomState(
                                    controllerId = cid,
                                    roomId = roomId,
                                    title = roomTitle,
                                    iconId = roomIconId
                                )
                                currentScreen = AppScreen.RoomDetails
                            },
                            onGroupClick = { groupId ->
                                val controllerId = loc.controllerId ?: return@PageLocation
                                selectedGroup = GroupState(controllerId = controllerId, groupId = groupId)
                                groupBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.GroupDetails
                            },
                            onLumClick = { luminaireId ->
                                selectedLuminaireId = luminaireId
                                lumBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.Lum
                            },
                            onSensorPressSettingsClick = { sensorId ->
                                selectedPresSensorId = sensorId
                                systemSettingsBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.SensorPressSettings
                            },
                            onSensorBrightSettingsClick = { sensorId ->
                                selectedBrightSensorId = sensorId
                                systemSettingsBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.SensorBrightSettings
                            },
                            onButtonPanelClick = { panelId ->
                                selectedButtonPanelId = panelId
                                buttonPanelBackTarget = AppScreen.LocationDetails
                                currentScreen = AppScreen.Panel
                            },
                            onInitializeControllerClick = {
                                if (loc.controllerId != null) {
                                    shouldResetDevicesOnInitialization = false
                                    controllerInitializationBackTarget = AppScreen.LocationDetails
                                    currentScreen = AppScreen.InitializeController
                                }
                            },
                            appearingRoomId = appearingLocationRoomId,
                            onAppearingRoomConsumed = { roomId: Int ->
                                if (appearingLocationRoomId == roomId) {
                                    appearingLocationRoomId = null
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.RoomDetails -> {
                        val room = selectedRoom
                        if (room != null) {
                            PageRoom(
                                roomTitle = room.title,
                                controllerId = room.controllerId,
                                roomId = room.roomId,
                                onBackClick = { currentScreen = AppScreen.LocationDetails },
                                onSettingsClick = { currentScreen = AppScreen.RoomSettings },
                                onGroupClick = { groupId ->
                                    selectedGroup = GroupState(controllerId = room.controllerId, groupId = groupId)
                                    groupBackTarget = AppScreen.RoomDetails
                                    currentScreen = AppScreen.GroupDetails
                                },
                                onLumClick = { luminaireId ->
                                    selectedLuminaireId = luminaireId
                                    lumBackTarget = AppScreen.RoomDetails
                                    currentScreen = AppScreen.Lum
                                },
                                onSensorPressSettingsClick = { sensorId ->
                                    selectedPresSensorId = sensorId
                                    systemSettingsBackTarget = AppScreen.RoomDetails
                                    currentScreen = AppScreen.SensorPressSettings
                                },
                                onSensorBrightSettingsClick = { sensorId ->
                                    selectedBrightSensorId = sensorId
                                    systemSettingsBackTarget = AppScreen.RoomDetails
                                    currentScreen = AppScreen.SensorBrightSettings
                                },
                                onButtonPanelSettingsClick = { panelId ->
                                    selectedButtonPanelId = panelId
                                    buttonPanelBackTarget = AppScreen.RoomDetails
                                    currentScreen = AppScreen.Panel
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    AppScreen.GroupDetails -> {
                        val group = selectedGroup
                        if (group != null) {
                            PageGroup(
                                controllerId = group.controllerId,
                                groupId = group.groupId,
                                onBackClick = { currentScreen = groupBackTarget },
                                onLumClick = { luminaireId ->
                                    selectedLuminaireId = luminaireId
                                    lumBackTarget = AppScreen.GroupDetails
                                    currentScreen = AppScreen.Lum
                                },
                                onSensorBrightSettingsClick = { sensorId ->
                                    selectedBrightSensorId = sensorId
                                    systemSettingsBackTarget = AppScreen.GroupDetails
                                    currentScreen = AppScreen.SensorBrightSettings
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                    AppScreen.RoomSettings -> {
                        PageRoomSettings(
                            controllerId = selectedRoom?.controllerId,
                            roomId = selectedRoom?.roomId,
                            initialName = selectedRoom?.title.orEmpty(),
                            initialIconId = selectedRoom?.iconId ?: 200,
                            onBackClick = { currentScreen = roomSettingsBackTarget },
                            onSaved = { name, iconId ->
                                selectedRoom = selectedRoom?.copy(title = name, iconId = iconId)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LocationSettings -> {
                        PageLocationSettings(
                            controllerId = selectedLocation?.controllerId,
                            onBackClick = { currentScreen = AppScreen.LocationDetails },
                            onSaved = { newName, newIconId ->
                                selectedLocation = selectedLocation?.copy(
                                    title = newName,
                                    iconResId = com.awada.synapse.components.iconResId(context, newIconId)
                                )
                            },
                            onRoomAdded = { roomId ->
                                appearingLocationRoomId = roomId
                                currentScreen = AppScreen.LocationDetails
                            },
                            onInitializeControllerClick = {
                                if (selectedLocation?.controllerId != null) {
                                    shouldResetDevicesOnInitialization = true
                                    controllerInitializationBackTarget = AppScreen.LocationSettings
                                    currentScreen = AppScreen.InitializeController
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.InitializeController -> {
                        PageInitializeController(
                            onBackClick = {
                                shouldResetDevicesOnInitialization = false
                                currentScreen = controllerInitializationBackTarget
                            },
                            onInitializationComplete = {
                                scope.launch {
                                    val controllerId = selectedLocation?.controllerId ?: return@launch
                                    if (shouldResetDevicesOnInitialization) {
                                        clearDevicesForController(db = db, controllerId = controllerId)
                                    }
                                    seedMockDevicesForController(db = db, controllerId = controllerId)
                                    shouldResetDevicesOnInitialization = false
                                    selectedLocation = loadLocationItem(controllerId)
                                    currentScreen = AppScreen.LocationDetails
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.ChangePassword -> {
                        PageChangePassword(
                            onBackClick = { currentScreen = changePasswordBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Schedule -> {
                        PageSchedule(
                            controllerId = selectedLocation?.controllerId,
                            onBackClick = { currentScreen = scheduleBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SchedulePoint -> {
                        PageSchedulePoint(
                            controllerId = selectedLocation?.controllerId,
                            eventId = selectedEventId,
                            onBackClick = { currentScreen = schedulePointBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Graphs -> {
                        PageGraphs(
                            controllerId = selectedLocation?.controllerId,
                            onBackClick = { currentScreen = graphsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Graph -> {
                        PageGraph(
                            controllerId = selectedLocation?.controllerId,
                            graphId = selectedGraphId,
                            onBackClick = { currentScreen = graphBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.IconSelect -> {
                        PageIconSelect(
                            category = iconSelectCategory ?: "controller",
                            currentIconId = iconSelectCurrentIconId ?: 100,
                            onIconSelected = { iconId ->
                                scope.launch {
                                    applySelectedIcon(
                                        iconCategory = iconSelectCategory ?: return@launch,
                                        iconId = iconId
                                    )
                                    iconSelectCurrentIconId = iconId
                                    currentScreen = iconSelectBackTarget
                                }
                            },
                            onBackClick = { currentScreen = iconSelectBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Lum -> {
                        PageLum(
                            brightnessPercent = selectedLuminaireOrNull?.bright ?: 0,
                            typeId = selectedLuminaireOrNull?.typeId ?: LuminaireTypeEntity.TYPE_DIMMABLE,
                            hue = selectedLuminaireOrNull?.hue ?: 0,
                            saturation = selectedLuminaireOrNull?.saturation ?: 0,
                            temperature = selectedLuminaireOrNull?.temperature ?: 0,
                            onBackClick = { currentScreen = lumBackTarget },
                            onSettingsClick = {
                                settingsLumBackTarget = AppScreen.Lum
                                currentScreen = AppScreen.LumSettings
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Search -> {
                        PageSearch(
                            onBackClick = { currentScreen = AppScreen.Location },
                            onAutoNavigateNext = { currentScreen = AppScreen.Password },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.Settings -> {
                        PageSettings(
                            onBackClick = { currentScreen = AppScreen.Location },
                            onFindControllerClick = { startFindControllerFlow() },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.LumSettings -> {
                        PageLumSettings(
                            luminaireId = selectedLuminaireId,
                            onBackClick = { currentScreen = settingsLumBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SensorPressSettings -> {
                        PageSensorPressSettings(
                            sensorId = selectedPresSensorId,
                            onBackClick = { currentScreen = systemSettingsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.SensorBrightSettings -> {
                        PageSensorBrightSettings(
                            sensorId = selectedBrightSensorId,
                            onBackClick = { currentScreen = systemSettingsBackTarget },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.ButtonPanelSettings -> {
                        PageButtonPanelSettings(
                            selectedButtonPanelId,
                            { currentScreen = systemSettingsBackTarget },
                            { buttonNumber: Int ->
                                selectedButtonNumber = buttonNumber
                                buttonSettingsBackTarget = AppScreen.ButtonPanelSettings
                                currentScreen = AppScreen.ButtonSettings
                            },
                            Modifier.fillMaxSize()
                        )
                    }
                    AppScreen.ButtonSettings -> {
                        PageButtonSettings(
                            buttonPanelId = selectedButtonPanelId,
                            buttonNumber = selectedButtonNumber,
                            onBackClick = { currentScreen = buttonSettingsBackTarget },
                            onScenarioClick = { scenarioId ->
                                selectedScenarioId = scenarioId
                                scenarioBackTarget = AppScreen.ButtonSettings
                                currentScreen = AppScreen.Scenario
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    AppScreen.Scenario -> {
                        PageScenario(
                            scenarioId = selectedScenarioId,
                            buttonPanelId = selectedButtonPanelId,
                            onBackClick = { currentScreen = scenarioBackTarget },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    AppScreen.Panel -> {
                        PageButtonPanel(
                            buttonPanelId = selectedButtonPanelId,
                            onBackClick = { currentScreen = buttonPanelBackTarget },
                            onSettingsClick = {
                                systemSettingsBackTarget = AppScreen.Panel
                                currentScreen = AppScreen.ButtonPanelSettings
                            },
                            onButtonLongClick = { buttonNumber ->
                                selectedButtonNumber = buttonNumber
                                buttonSettingsBackTarget = AppScreen.Panel
                                currentScreen = AppScreen.ButtonSettings
                            },
                            modifier = Modifier.fillMaxSize(),
                        )
                    }
                    AppScreen.Password -> {
                        PagePassword(
                            correctPassword = "1234", // TODO: Get from settings
                            onPasswordCorrect = {
                                scope.launch {
                                    completeFoundControllerFlow()
                                }
                            },
                            onBackClick = { currentScreen = AppScreen.Location },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
        
        // Lighting control layer - between pages and AI
        // Positioned above AI component with proper padding
        LumControlLayer(
            isVisible = isLumControlVisible && !TooltipOverlayState.isVisible,
            sliders = lumControlSliders,
            onSceneSelected = { sceneNum ->
                val targetLuminaires = lumControlLuminaires
                if (targetLuminaires.isNotEmpty()) {
                    scope.launch {
                        applySceneToLuminaires(
                            db = db,
                            sceneNum = sceneNum,
                            luminaires = targetLuminaires
                        )

                        when (currentScreen) {
                            AppScreen.RoomDetails -> {
                                val room = selectedRoom
                                if (room != null) {
                                    db.roomDao().setSceneNum(room.controllerId, room.roomId, sceneNum)
                                }
                            }
                            AppScreen.GroupDetails -> Unit
                            AppScreen.LocationDetails -> {
                                val controllerId = selectedLocation?.controllerId
                                if (controllerId != null) {
                                    db.controllerDao().setSceneNum(controllerId, sceneNum)
                                }
                            }
                            else -> Unit
                        }
                    }
                }
            },
            onSceneLongSelected = { sceneNum ->
                if (lumControlLuminaires.isNotEmpty()) {
                    pendingSaveSceneNum = sceneNum
                }
            },
            colorValue = if (
                currentScreen == AppScreen.Lum ||
                currentScreen == AppScreen.LocationDetails ||
                currentScreen == AppScreen.RoomDetails ||
                currentScreen == AppScreen.GroupDetails
            ) {
                lumControlColorValue
            } else {
                null
            },
            onColorValueChange = when (currentScreen) {
                AppScreen.Lum -> { value ->
                    val luminaireId = selectedLuminaireId
                    if (luminaireId != null) {
                        scope.launch {
                            db.luminaireDao().setHue(
                                id = luminaireId,
                                hue = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.LocationDetails -> { value ->
                    val controllerId = selectedLocation?.controllerId
                    if (controllerId != null) {
                        scope.launch {
                            db.luminaireDao().setHueForController(
                                controllerId = controllerId,
                                hue = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.RoomDetails -> { value ->
                    val room = selectedRoom
                    if (room != null) {
                        scope.launch {
                            db.luminaireDao().setHueForRoom(
                                controllerId = room.controllerId,
                                roomId = room.roomId,
                                hue = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.GroupDetails -> { value ->
                    val group = selectedGroup
                    if (group != null) {
                        scope.launch {
                            db.luminaireDao().setHueForGroup(
                                controllerId = group.controllerId,
                                groupId = group.groupId,
                                hue = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                else -> null
            },
            saturationValue = if (
                currentScreen == AppScreen.Lum ||
                currentScreen == AppScreen.LocationDetails ||
                currentScreen == AppScreen.RoomDetails ||
                currentScreen == AppScreen.GroupDetails
            ) {
                lumControlSaturationValue
            } else {
                null
            },
            onSaturationValueChange = when (currentScreen) {
                AppScreen.Lum -> { value ->
                    val luminaireId = selectedLuminaireId
                    if (luminaireId != null) {
                        scope.launch {
                            db.luminaireDao().setSaturation(
                                id = luminaireId,
                                saturation = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.LocationDetails -> { value ->
                    val controllerId = selectedLocation?.controllerId
                    if (controllerId != null) {
                        scope.launch {
                            db.luminaireDao().setSaturationForController(
                                controllerId = controllerId,
                                saturation = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.RoomDetails -> { value ->
                    val room = selectedRoom
                    if (room != null) {
                        scope.launch {
                            db.luminaireDao().setSaturationForRoom(
                                controllerId = room.controllerId,
                                roomId = room.roomId,
                                saturation = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.GroupDetails -> { value ->
                    val group = selectedGroup
                    if (group != null) {
                        scope.launch {
                            db.luminaireDao().setSaturationForGroup(
                                controllerId = group.controllerId,
                                groupId = group.groupId,
                                saturation = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                else -> null
            },
            temperatureValue = if (
                currentScreen == AppScreen.Lum ||
                currentScreen == AppScreen.LocationDetails ||
                currentScreen == AppScreen.RoomDetails ||
                currentScreen == AppScreen.GroupDetails
            ) {
                lumControlTemperatureValue
            } else {
                null
            },
            onTemperatureValueChange = when (currentScreen) {
                AppScreen.Lum -> { value ->
                    val luminaireId = selectedLuminaireId
                    if (luminaireId != null) {
                        scope.launch {
                            db.luminaireDao().setTemperature(
                                id = luminaireId,
                                temperature = value.roundToInt().coerceIn(3000, 5000)
                            )
                        }
                    }
                }
                AppScreen.LocationDetails -> { value ->
                    val controllerId = selectedLocation?.controllerId
                    if (controllerId != null) {
                        scope.launch {
                            db.luminaireDao().setTemperatureForController(
                                controllerId = controllerId,
                                temperature = value.roundToInt().coerceIn(3000, 5000)
                            )
                        }
                    }
                }
                AppScreen.RoomDetails -> { value ->
                    val room = selectedRoom
                    if (room != null) {
                        scope.launch {
                            db.luminaireDao().setTemperatureForRoom(
                                controllerId = room.controllerId,
                                roomId = room.roomId,
                                temperature = value.roundToInt().coerceIn(3000, 5000)
                            )
                        }
                    }
                }
                AppScreen.GroupDetails -> { value ->
                    val group = selectedGroup
                    if (group != null) {
                        scope.launch {
                            db.luminaireDao().setTemperatureForGroup(
                                controllerId = group.controllerId,
                                groupId = group.groupId,
                                temperature = value.roundToInt().coerceIn(3000, 5000)
                            )
                        }
                    }
                }
                else -> null
            },
            brightnessValue = if (
                currentScreen == AppScreen.Lum ||
                currentScreen == AppScreen.LocationDetails ||
                currentScreen == AppScreen.RoomDetails ||
                currentScreen == AppScreen.GroupDetails
            ) {
                lumControlBrightnessValue
            } else {
                null
            },
            onBrightnessValueChange = when (currentScreen) {
                AppScreen.Lum -> { value ->
                    val luminaireId = selectedLuminaireId
                    if (luminaireId != null) {
                        scope.launch {
                            db.luminaireDao().setBright(
                                id = luminaireId,
                                bright = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.LocationDetails -> { value ->
                    val controllerId = selectedLocation?.controllerId
                    if (controllerId != null) {
                        scope.launch {
                            db.luminaireDao().setBrightForController(
                                controllerId = controllerId,
                                bright = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.RoomDetails -> { value ->
                    val room = selectedRoom
                    if (room != null) {
                        scope.launch {
                            db.luminaireDao().setBrightForRoom(
                                controllerId = room.controllerId,
                                roomId = room.roomId,
                                bright = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                AppScreen.GroupDetails -> { value ->
                    val group = selectedGroup
                    if (group != null) {
                        scope.launch {
                            db.luminaireDao().setBrightForGroup(
                                controllerId = group.controllerId,
                                groupId = group.groupId,
                                bright = value.roundToInt().coerceIn(0, 100)
                            )
                        }
                    }
                }
                else -> null
            },
            brightnessEnabled = lumControlBrightnessEnabled,
            autoExpandOnShow = currentScreen == AppScreen.Lum,
            stateKey = currentScreen,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .fillMaxWidth()
                .onGloballyPositioned { coords ->
                    lumPanelTopPx = coords.boundsInRoot().top
                }
        )
        
        AI(
            modifier = Modifier.fillMaxSize(),
            uiContext = llmUiContext,
            onNavigationCommand = { command: LLMNavigationCommand ->
                scope.launch {
                    val targetScreen = llmScreenToAppScreen(command.screen) ?: return@launch
                    if (connectedControllerId == null && !canOpenWhenDisconnected(targetScreen)) {
                        return@launch
                    }
                    when (targetScreen) {
                        AppScreen.Location -> {
                            currentScreen = AppScreen.Location
                        }
                        AppScreen.LocationDetails -> {
                            val controllerId = command.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            selectedLocation = location
                            currentScreen = AppScreen.LocationDetails
                        }
                        AppScreen.InitializeController -> {
                            val controllerId = command.controllerId ?: selectedLocation?.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            if (!isControllerConnected(controllerId)) {
                                Toast.makeText(context, "Сначала подключитесь к контроллеру этой локации", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            if (hasAnyControllerDevices(controllerId)) {
                                pendingReinitializeControllerId = controllerId
                                pendingReinitializeControllerTitle = location.title
                                return@launch
                            }
                            selectedLocation = location
                            shouldResetDevicesOnInitialization = false
                            controllerInitializationBackTarget = AppScreen.LocationDetails
                            currentScreen = AppScreen.InitializeController
                        }
                        AppScreen.RoomDetails -> {
                            val controllerId = command.controllerId ?: return@launch
                            val roomId = command.roomId ?: return@launch
                            val room = loadRoomState(controllerId, roomId) ?: return@launch
                            selectedRoom = room
                            currentScreen = AppScreen.RoomDetails
                        }
                        AppScreen.GroupDetails -> {
                            val controllerId = command.controllerId ?: return@launch
                            val groupId = command.groupId ?: return@launch
                            if (db.groupDao().getById(groupId) == null) return@launch
                            selectedGroup = GroupState(controllerId = controllerId, groupId = groupId)
                            currentScreen = AppScreen.GroupDetails
                        }
                        AppScreen.LocationSettings -> {
                            val controllerId = command.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            selectedLocation = location
                            currentScreen = AppScreen.LocationSettings
                        }
                        AppScreen.ChangePassword -> {
                            val controllerId = command.controllerId ?: selectedLocation?.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            selectedLocation = location
                            changePasswordBackTarget = AppScreen.LocationSettings
                            currentScreen = AppScreen.ChangePassword
                        }
                        AppScreen.Schedule -> {
                            val controllerId = command.controllerId ?: selectedLocation?.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            selectedLocation = location
                            scheduleBackTarget = AppScreen.LocationSettings
                            currentScreen = AppScreen.Schedule
                        }
                        AppScreen.SchedulePoint -> {
                            val eventId = command.eventId
                            val resolvedControllerId = command.controllerId
                                ?: eventId?.let { db.eventDao().getById(it)?.controllerId }
                                ?: selectedLocation?.controllerId
                                ?: return@launch
                            val location = loadLocationItem(resolvedControllerId) ?: return@launch
                            selectedLocation = location
                            selectedEventId = eventId
                            scheduleBackTarget = AppScreen.LocationSettings
                            schedulePointBackTarget = AppScreen.Schedule
                            currentScreen = AppScreen.SchedulePoint
                        }
                        AppScreen.Graphs -> {
                            val controllerId = command.controllerId ?: selectedLocation?.controllerId ?: return@launch
                            val location = loadLocationItem(controllerId) ?: return@launch
                            selectedLocation = location
                            graphsBackTarget = AppScreen.LocationSettings
                            currentScreen = AppScreen.Graphs
                        }
                        AppScreen.Graph -> {
                            val graphId = command.graphId
                            val resolvedControllerId = command.controllerId
                                ?: graphId?.let { db.graphDao().getById(it)?.controllerId }
                                ?: selectedLocation?.controllerId
                                ?: return@launch
                            val location = loadLocationItem(resolvedControllerId) ?: return@launch
                            selectedLocation = location
                            selectedGraphId = graphId
                            graphsBackTarget = AppScreen.LocationSettings
                            graphBackTarget = AppScreen.Graphs
                            currentScreen = AppScreen.Graph
                        }
                        AppScreen.RoomSettings -> {
                            val controllerId = command.controllerId ?: return@launch
                            val roomId = command.roomId ?: return@launch
                            val room = loadRoomState(controllerId, roomId) ?: return@launch
                            selectedRoom = room
                            roomSettingsBackTarget = AppScreen.RoomDetails
                            currentScreen = AppScreen.RoomSettings
                        }
                        AppScreen.Lum -> {
                            val luminaireId = command.luminaireId ?: return@launch
                            selectedLuminaireId = luminaireId
                            lumBackTarget = when {
                                command.roomId != null && command.controllerId != null -> {
                                    loadRoomState(command.controllerId, command.roomId)?.let { selectedRoom = it }
                                    AppScreen.RoomDetails
                                }
                                command.groupId != null && command.controllerId != null -> {
                                    selectedGroup = GroupState(
                                        controllerId = command.controllerId,
                                        groupId = command.groupId
                                    )
                                    AppScreen.GroupDetails
                                }
                                command.controllerId != null -> {
                                    loadLocationItem(command.controllerId)?.let { selectedLocation = it }
                                    AppScreen.LocationDetails
                                }
                                else -> currentScreen
                            }
                            currentScreen = AppScreen.Lum
                        }
                        AppScreen.LumSettings -> {
                            val luminaireId = command.luminaireId ?: return@launch
                            selectedLuminaireId = luminaireId
                            settingsLumBackTarget = AppScreen.Lum
                            currentScreen = AppScreen.LumSettings
                        }
                        AppScreen.SensorPressSettings -> {
                            val sensorId = command.presSensorId ?: return@launch
                            if (db.presSensorDao().getById(sensorId) == null) return@launch
                            selectedPresSensorId = sensorId
                            systemSettingsBackTarget = currentScreen
                            currentScreen = AppScreen.SensorPressSettings
                        }
                        AppScreen.SensorBrightSettings -> {
                            val sensorId = command.brightSensorId ?: return@launch
                            if (db.brightSensorDao().getById(sensorId) == null) return@launch
                            selectedBrightSensorId = sensorId
                            systemSettingsBackTarget = currentScreen
                            currentScreen = AppScreen.SensorBrightSettings
                        }
                        AppScreen.Panel -> {
                            val buttonPanelId = command.buttonPanelId ?: return@launch
                            if (db.buttonPanelDao().getById(buttonPanelId) == null) return@launch
                            selectedButtonPanelId = buttonPanelId
                            buttonPanelBackTarget = currentScreen
                            currentScreen = AppScreen.Panel
                        }
                        AppScreen.ButtonPanelSettings -> {
                            val buttonPanelId = command.buttonPanelId ?: return@launch
                            if (db.buttonPanelDao().getById(buttonPanelId) == null) return@launch
                            selectedButtonPanelId = buttonPanelId
                            systemSettingsBackTarget = AppScreen.Panel
                            currentScreen = AppScreen.ButtonPanelSettings
                        }
                        AppScreen.ButtonSettings -> {
                            val buttonPanelId = command.buttonPanelId ?: return@launch
                            val buttonNumber = command.buttonNumber ?: return@launch
                            if (db.buttonPanelDao().getById(buttonPanelId) == null) return@launch
                            selectedButtonPanelId = buttonPanelId
                            selectedButtonNumber = buttonNumber
                            buttonSettingsBackTarget = AppScreen.Panel
                            currentScreen = AppScreen.ButtonSettings
                        }
                        AppScreen.Scenario -> {
                            val scenarioId = command.scenarioId ?: return@launch
                            selectedScenarioId = scenarioId
                            scenarioBackTarget = currentScreen
                            currentScreen = AppScreen.Scenario
                        }
                        AppScreen.Search -> {
                            startFindControllerFlow()
                        }
                        AppScreen.Settings -> {
                            currentScreen = AppScreen.Settings
                        }
                        AppScreen.Password -> {
                            currentScreen = AppScreen.Password
                        }
                        AppScreen.IconSelect -> {
                            when (command.iconCategory) {
                                "controller" -> {
                                    val controllerId = command.controllerId ?: selectedLocation?.controllerId ?: return@launch
                                    val controller = db.controllerDao().getById(controllerId) ?: return@launch
                                    selectedLocation = loadLocationItem(controllerId)
                                    iconSelectCategory = "controller"
                                    iconSelectCurrentIconId = controller.icoNum
                                    iconSelectBackTarget = AppScreen.LocationSettings
                                    currentScreen = AppScreen.IconSelect
                                }
                                "room" -> {
                                    val controllerId = command.controllerId ?: selectedRoom?.controllerId ?: return@launch
                                    val roomId = command.roomId ?: selectedRoom?.roomId ?: return@launch
                                    val room = loadRoomState(controllerId, roomId) ?: return@launch
                                    selectedRoom = room
                                    iconSelectCategory = "room"
                                    iconSelectCurrentIconId = room.iconId
                                    iconSelectBackTarget = AppScreen.RoomSettings
                                    currentScreen = AppScreen.IconSelect
                                }
                                "luminaire" -> {
                                    val luminaireId = command.luminaireId ?: selectedLuminaireId ?: return@launch
                                    val luminaire = db.luminaireDao().getById(luminaireId) ?: return@launch
                                    selectedLuminaireId = luminaireId
                                    iconSelectCategory = "luminaire"
                                    iconSelectCurrentIconId = luminaire.icoNum
                                    iconSelectBackTarget = AppScreen.LumSettings
                                    currentScreen = AppScreen.IconSelect
                                }
                            }
                        }
                    }
                }
            },
            onActionCommand = { action: LLMActionCommand ->
                scope.launch {
                    when (action.type) {
                        "deleteLocation" -> {
                            val controllerId = action.controllerId ?: return@launch
                            val controller = db.controllerDao().getById(controllerId) ?: return@launch
                            pendingDeleteLocationId = controllerId
                            pendingDeleteLocationTitle = controller.name.ifBlank { "Локация" }
                        }
                        "reinitializeController" -> {
                            val controllerId = action.controllerId ?: return@launch
                            val controller = db.controllerDao().getById(controllerId) ?: return@launch
                            if (!isControllerConnected(controllerId)) {
                                Toast.makeText(context, "Сначала подключитесь к контроллеру этой локации", Toast.LENGTH_SHORT).show()
                                return@launch
                            }
                            pendingReinitializeControllerId = controllerId
                            pendingReinitializeControllerTitle = controller.name.ifBlank { "Локация" }
                        }
                    }
                }
            },
            onMainPanelTopPxChanged = { aiPanelTopPx = it },
            onChatExpandedChange = { isAiChatExpanded = it },
            collapseRequestKey = aiChatCollapseRequestKey
        )

        BackHandler(enabled = isAiChatExpanded) {
            aiChatCollapseRequestKey += 1
        }

        if (pendingSaveSceneNum != null) {
            val sceneNum = pendingSaveSceneNum!!
            Tooltip(
                text = "Сохранить текущие параметры в сцену ${sceneLabel(sceneNum)}?",
                primaryButtonText = "Сохранить",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    when (result) {
                        TooltipResult.Primary -> {
                            val targetLuminaires = lumControlLuminaires
                            pendingSaveSceneNum = null
                            if (targetLuminaires.isNotEmpty()) {
                                scope.launch {
                                    saveSceneForLuminaires(
                                        db = db,
                                        sceneNum = sceneNum,
                                        luminaires = targetLuminaires
                                    )
                                }
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Tertiary, TooltipResult.Quaternary, TooltipResult.Dismissed -> {
                            pendingSaveSceneNum = null
                        }
                    }
                }
            )
        }
        if (pendingDeleteLocationId != null) {
            val title = pendingDeleteLocationTitle
            val text = if (!title.isNullOrBlank()) {
                "Удалить локацию «$title»?"
            } else {
                "Удалить локацию?"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Удалить",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    when (result) {
                        TooltipResult.Primary -> {
                            val controllerId = pendingDeleteLocationId ?: return@Tooltip
                            scope.launch {
                                deleteLocationById(controllerId)
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                            pendingDeleteLocationId = null
                            pendingDeleteLocationTitle = null
                        }
                    }
                }
            )
        }
        if (pendingReinitializeControllerId != null) {
            val title = pendingReinitializeControllerTitle
            val text = if (!title.isNullOrBlank()) {
                "Переинициализировать контроллер локации «$title»? Все настройки будут сброшены"
            } else {
                "Переинициализировать контроллер? Все настройки будут сброшены"
            }
            Tooltip(
                text = text,
                primaryButtonText = "Да",
                secondaryButtonText = "Отмена",
                onResult = { result ->
                    when (result) {
                        TooltipResult.Primary -> {
                            val controllerId = pendingReinitializeControllerId ?: return@Tooltip
                            scope.launch {
                                startControllerReinitialization(controllerId)
                            }
                        }
                        TooltipResult.Secondary, TooltipResult.Dismissed, TooltipResult.Tertiary, TooltipResult.Quaternary -> {
                            pendingReinitializeControllerId = null
                            pendingReinitializeControllerTitle = null
                        }
                    }
                }
            )
        }
        }
    }
}

private data class RoomState(
    val controllerId: Int,
    val roomId: Int,
    val title: String,
    val iconId: Int
)

private data class GroupState(
    val controllerId: Int,
    val groupId: Int
)

private suspend fun clearDevicesForController(
    db: AppDatabase,
    controllerId: Int
) {
    db.eventDao().deleteAllForController(controllerId)
    db.graphDao().deleteAllForController(controllerId)
    db.scenarioDao().deleteAllForController(controllerId)
    db.buttonDao().deleteAllForController(controllerId)
    db.buttonPanelDao().deleteAllForController(controllerId)
    db.presSensorDao().deleteAllForController(controllerId)
    db.brightSensorDao().deleteAllForController(controllerId)
    db.luminaireDao().deleteAllForController(controllerId)
    db.roomDao().deleteAllForController(controllerId)
}

private suspend fun seedMockDevicesForController(
    db: AppDatabase,
    controllerId: Int
) {
    val existingDeviceCount =
        db.luminaireDao().observeCountForController(controllerId).first() +
            db.buttonPanelDao().observeCountForController(controllerId).first() +
            db.presSensorDao().observeCountForController(controllerId).first() +
            db.brightSensorDao().observeCountForController(controllerId).first()
    if (existingDeviceCount > 0) return

    var nextGridPos = 0
    val luminaireDao = db.luminaireDao()
    val luminaireTypes = listOf(
        LuminaireTypeEntity.TYPE_ON_OFF,
        LuminaireTypeEntity.TYPE_ON_OFF,
        LuminaireTypeEntity.TYPE_DIMMABLE,
        LuminaireTypeEntity.TYPE_DIMMABLE,
        LuminaireTypeEntity.TYPE_RGB,
        LuminaireTypeEntity.TYPE_RGB,
        LuminaireTypeEntity.TYPE_TW,
        LuminaireTypeEntity.TYPE_TW
    )
    val luminaireTypeOccurrences = mutableMapOf<Int, Int>()
    luminaireTypes.forEach { typeId ->
        val occurrence = luminaireTypeOccurrences.getOrDefault(typeId, 0) + 1
        luminaireTypeOccurrences[typeId] = occurrence
        val typeName = when (typeId) {
            LuminaireTypeEntity.TYPE_ON_OFF -> "Вкл/выкл"
            LuminaireTypeEntity.TYPE_DIMMABLE -> "Диммируемый"
            LuminaireTypeEntity.TYPE_RGB -> "RGB"
            LuminaireTypeEntity.TYPE_TW -> "TW"
            else -> "Светильник"
        }
        luminaireDao.insert(
            LuminaireEntity(
                controllerId = controllerId,
                roomId = null,
                name = "$typeName $occurrence",
                icoNum = 300,
                typeId = typeId,
                bright = 0,
                temperature = 0,
                saturation = 0,
                hue = 0,
                gridPos = nextGridPos++
            )
        )
    }

    val buttonDao = db.buttonDao()
    val buttonPanelDao = db.buttonPanelDao()
    listOf(1, 2, 4, 6, 8).forEach { buttonCount ->
        val buttonPanelId = buttonPanelDao.insert(
            ButtonPanelEntity(
                controllerId = controllerId,
                roomId = null,
                name = "Кнопочная панель $buttonCount",
                gridPos = nextGridPos++
            )
        )
        val nextButtonId = buttonDao.getNextId()
        buttonDao.insertAll(
            List(buttonCount) { buttonIdx ->
                ButtonEntity(
                    id = nextButtonId + buttonIdx,
                    num = buttonIdx + 1,
                    buttonPanelId = buttonPanelId,
                    daliInst = ButtonEntity.UNASSIGNED_DALI_INST,
                    matrixRow = buttonIdx / ButtonEntity.MATRIX_SIZE,
                    matrixCol = buttonIdx % ButtonEntity.MATRIX_SIZE,
                )
            }
        )
    }

    repeat(2) { index ->
        db.presSensorDao().insert(
            PresSensorEntity(
                controllerId = controllerId,
                roomId = null,
                name = "Датчик присутствия ${index + 1}",
                gridPos = nextGridPos++
            )
        )
    }
    repeat(2) { index ->
        db.brightSensorDao().insert(
            BrightSensorEntity(
                controllerId = controllerId,
                roomId = null,
                name = "Датчик освещенности ${index + 1}",
                gridPos = nextGridPos++
            )
        )
    }
}

private suspend fun applySceneToLuminaires(
    db: AppDatabase,
    sceneNum: Int,
    luminaires: List<LuminaireEntity>
) {
    if (luminaires.isEmpty()) return

    val sceneByLuminaireId = db.luminaireSceneDao()
        .getAllForSceneAndLuminaires(sceneNum, luminaires.map { it.id })
        .associateBy { it.luminaireId }

    val luminaireDao = db.luminaireDao()
    luminaires.forEach { luminaire ->
        val sceneState = sceneByLuminaireId[luminaire.id]
        val fallback = defaultSceneStateFor(luminaire = luminaire, sceneNum = sceneNum)
        val resolvedBright = sceneState?.bright ?: fallback.bright
        val resolvedTemperature = sceneState?.temperature ?: fallback.temperature
        val resolvedSaturation = sceneState?.saturation ?: fallback.saturation
        val resolvedHue = sceneState?.hue ?: fallback.hue

        luminaireDao.update(
            luminaire.copy(
                bright = resolvedBright,
                temperature = resolvedTemperature,
                saturation = resolvedSaturation,
                hue = resolvedHue
            )
        )
    }
}

private suspend fun saveSceneForLuminaires(
    db: AppDatabase,
    sceneNum: Int,
    luminaires: List<LuminaireEntity>
) {
    if (luminaires.isEmpty()) return

    db.luminaireSceneDao().upsertAll(
        luminaires.map { luminaire ->
            when (luminaire.typeId) {
                LuminaireTypeEntity.TYPE_RGB -> com.awada.synapse.db.LuminaireSceneEntity(
                    sceneNum = sceneNum,
                    luminaireId = luminaire.id,
                    bright = luminaire.bright,
                    temperature = null,
                    saturation = luminaire.saturation,
                    hue = luminaire.hue
                )
                LuminaireTypeEntity.TYPE_TW -> com.awada.synapse.db.LuminaireSceneEntity(
                    sceneNum = sceneNum,
                    luminaireId = luminaire.id,
                    bright = luminaire.bright,
                    temperature = luminaire.temperature,
                    saturation = null,
                    hue = null
                )
                else -> com.awada.synapse.db.LuminaireSceneEntity(
                    sceneNum = sceneNum,
                    luminaireId = luminaire.id,
                    bright = luminaire.bright,
                    temperature = null,
                    saturation = null,
                    hue = null
                )
            }
        }
    )
}

private fun defaultSceneStateFor(
    luminaire: LuminaireEntity,
    sceneNum: Int
): LuminaireEntity {
    val defaultBright = when (sceneNum.coerceIn(0, 4)) {
        0 -> 0
        1 -> 25
        2 -> 50
        3 -> 75
        else -> 100
    }

    return when (luminaire.typeId) {
        LuminaireTypeEntity.TYPE_RGB -> luminaire.copy(
            bright = defaultBright,
            hue = 0,
            saturation = 100
        )
        LuminaireTypeEntity.TYPE_TW -> luminaire.copy(
            bright = defaultBright,
            temperature = 4000
        )
        else -> luminaire.copy(bright = defaultBright)
    }
}

private fun sceneLabel(sceneNum: Int): String = when (sceneNum) {
    0 -> "«Выкл»"
    4 -> "«Вкл»"
    else -> "«$sceneNum»"
}

private fun iconIdFromDrawableResId(context: Context, drawableResId: Int): Int? {
    val entryName = runCatching { context.resources.getResourceEntryName(drawableResId) }.getOrNull()
        ?: return null
    val catalog = IconCatalogManager.load(context)
    return catalog.icons.firstOrNull { it.resourceName == entryName }?.id
}

@Preview(showBackground = true)
@Composable
private fun MainPreview() {
    SynapseTheme {
        MainContent()
    }
}
