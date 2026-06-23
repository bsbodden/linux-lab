package dev.kmpilot.linux.presentation

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.kmpilot.linux.data.CommandRepository
import dev.kmpilot.linux.domain.Command
import dev.kmpilot.linux.runtime.ChartSpec
import dev.kmpilot.linux.runtime.StateSpec
import dev.kmpilot.linux.runtime.TransitionSpec
import dev.kmpilot.linux.runtime.publishAppGraph
import dev.kmpilot.linux.runtime.publishCurrentScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/** Commands screen — the MEAT: a searchable list backed by [CommandsMachine] (Loading/Content/Empty/Error). */
class CommandsComponent(
    ctx: ComponentContext,
    private val scope: CoroutineScope,
    private val repo: CommandRepository,
    val onOpen: (String) -> Unit,
    val onBasics: () -> Unit,
    val onSettings: () -> Unit,
) : ComponentContext by ctx {
    private val machine = CommandsMachine(scope)
    val ui: StateFlow<CommandsUi> get() = machine.ui
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    init { scope.launch { machine.start(); machine.onLoaded(repo.search("")) } }

    fun setQuery(q: String) { _query.value = q; scope.launch { machine.onLoaded(repo.search(q)) } }
    fun retry() { scope.launch { machine.retry(); machine.onLoaded(repo.search(_query.value)) } }
}

/** Command detail — the MEAT: a real man-page-style view of one command. */
class CommandDetailComponent(
    ctx: ComponentContext,
    private val repo: CommandRepository,
    name: String,
    val onBack: () -> Unit,
) : ComponentContext by ctx {
    val command: Command? = repo.byName(name)
}

/** Basics — a real grouped-by-category browser. */
class BasicsComponent(
    ctx: ComponentContext,
    private val repo: CommandRepository,
    val onOpen: (String) -> Unit,
    val onBack: () -> Unit,
) : ComponentContext by ctx {
    val groups: List<Pair<String, List<Command>>> = repo.categories().map { it to repo.byCategory(it) }
}

/** Settings — a SCAFFOLD placeholder (meat-first: the screen exists + is reachable, the content is stubbed). */
class SettingsComponent(ctx: ComponentContext, val onBack: () -> Unit) : ComponentContext by ctx

class RootComponent(
    ctx: ComponentContext,
    private val scope: CoroutineScope,
    private val repo: CommandRepository,
) : ComponentContext by ctx {

    private val nav = StackNavigation<Config>()
    val stack: Value<ChildStack<Config, Child>> = childStack(
        source = nav,
        serializer = Config.serializer(),
        initialConfiguration = Config.Commands,
        handleBackButton = true,
        childFactory = ::child,
    )

    init {
        publishAppGraph(Json.encodeToString(APP_GRAPH))
        stack.subscribe { childStack -> publishCurrentScreen(childStack.active.configuration.screenName()) }
    }

    companion object {
        val APP_GRAPH = ChartSpec(
            id = "App", initial = "Commands",
            states = listOf(
                StateSpec("Commands", "list"),
                StateSpec("CommandDetail", "detail"),
                StateSpec("Basics", "list"),
                StateSpec("Settings", "settings"),
            ),
            transitions = listOf(
                TransitionSpec("Commands", "CommandDetail", "Open"),
                TransitionSpec("Commands", "Basics", "Basics"),
                TransitionSpec("Commands", "Settings", "Settings"),
                TransitionSpec("CommandDetail", "Commands", "Back"),
                TransitionSpec("Basics", "CommandDetail", "Open"),
                TransitionSpec("Basics", "Commands", "Back"),
                TransitionSpec("Settings", "Commands", "Back"),
            ),
        )
    }

    private fun child(config: Config, childCtx: ComponentContext): Child = when (config) {
        Config.Commands -> Child.Commands(
            CommandsComponent(childCtx, scope, repo,
                onOpen = { nav.pushNew(Config.CommandDetail(it)) },
                onBasics = { nav.pushNew(Config.Basics) },
                onSettings = { nav.pushNew(Config.Settings) }),
        )
        is Config.CommandDetail -> Child.CommandDetail(
            CommandDetailComponent(childCtx, repo, config.name, onBack = { nav.pop() }),
        )
        Config.Basics -> Child.Basics(
            BasicsComponent(childCtx, repo, onOpen = { nav.pushNew(Config.CommandDetail(it)) }, onBack = { nav.pop() }),
        )
        Config.Settings -> Child.Settings(SettingsComponent(childCtx, onBack = { nav.pop() }))
    }

    fun navigateTo(screen: String) {
        when (screen) {
            "Commands" -> nav.replaceAll(Config.Commands)
            "CommandDetail" -> nav.replaceAll(Config.Commands, Config.CommandDetail(repo.search("").firstOrNull()?.name ?: "ls"))
            "Basics" -> nav.replaceAll(Config.Commands, Config.Basics)
            "Settings" -> nav.replaceAll(Config.Commands, Config.Settings)
        }
    }

    @Serializable
    sealed interface Config {
        @Serializable data object Commands : Config
        @Serializable data class CommandDetail(val name: String) : Config
        @Serializable data object Basics : Config
        @Serializable data object Settings : Config
    }

    sealed interface Child {
        class Commands(val component: CommandsComponent) : Child
        class CommandDetail(val component: CommandDetailComponent) : Child
        class Basics(val component: BasicsComponent) : Child
        class Settings(val component: SettingsComponent) : Child
    }

    private fun Config.screenName(): String = when (this) {
        Config.Commands -> "Commands"
        is Config.CommandDetail -> "CommandDetail"
        Config.Basics -> "Basics"
        Config.Settings -> "Settings"
    }
}
