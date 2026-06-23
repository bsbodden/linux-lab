package dev.kmpilot.linux.presentation

import dev.kmpilot.linux.domain.Command
import dev.kmpilot.linux.runtime.ChartSpec
import dev.kmpilot.linux.runtime.StateSpec
import dev.kmpilot.linux.runtime.TransitionSpec
import dev.kmpilot.linux.runtime.publishChartSpec
import dev.kmpilot.linux.runtime.publishScreenState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.nsk.kstatemachine.event.*
import ru.nsk.kstatemachine.state.*
import ru.nsk.kstatemachine.statemachine.*
import ru.nsk.kstatemachine.transition.*

/** The rendered states of the Commands screen — projected 1:1 from the statechart's active state. */
sealed interface CommandsUi {
    data object Loading : CommandsUi
    data class Content(val commands: List<Command>) : CommandsUi
    data object Empty : CommandsUi               // a search with no matches
    data class Error(val message: String) : CommandsUi
}

/**
 * The Commands screen as a STATECHART (the same `ScreenMachine` pattern as todo-lab's TaskListMachine). Search
 * results feed in as `Loaded` events; the machine projects Loading → Content / Empty (no matches) / Error. The
 * active state drives [ui]; the structure is published for the editor's per-screen drill-down.
 */
class CommandsMachine(private val scope: CoroutineScope) {

    data class Loaded(val commands: List<Command>) : Event
    data class LoadFailed(val message: String) : Event
    object Retry : Event

    companion object {
        val CHART = ChartSpec(
            id = "Commands", initial = "Loading",
            states = listOf(
                StateSpec("Loading", "loading"), StateSpec("Content", "content"),
                StateSpec("Empty", "empty"), StateSpec("Error", "error"),
            ),
            transitions = listOf(
                TransitionSpec("Loading", "Content", "Loaded[items]"),
                TransitionSpec("Loading", "Empty", "Loaded[none]"),
                TransitionSpec("Loading", "Error", "LoadFailed"),
                TransitionSpec("Content", "Content", "Loaded[items]"),
                TransitionSpec("Content", "Empty", "Search[no match]"),
                TransitionSpec("Empty", "Content", "Search[match]"),
                TransitionSpec("Empty", "Empty", "Loaded[none]"),
                TransitionSpec("Error", "Loading", "Retry"),
            ),
        )
    }

    private val _ui = MutableStateFlow<CommandsUi>(CommandsUi.Loading)
    val ui: StateFlow<CommandsUi> = _ui.asStateFlow()

    private lateinit var machine: StateMachine

    private fun project(commands: List<Command>): CommandsUi =
        if (commands.isEmpty()) CommandsUi.Empty else CommandsUi.Content(commands)

    private fun emit(ui: CommandsUi) {
        _ui.value = ui
        publishScreenState("Commands", describe(ui))
    }

    private fun describe(ui: CommandsUi): String = when (ui) {
        is CommandsUi.Loading -> "Loading"
        is CommandsUi.Content -> "Content:${ui.commands.size}"
        is CommandsUi.Empty -> "Empty"
        is CommandsUi.Error -> "Error:${ui.message}"
    }

    suspend fun start() {
        machine = createStateMachine(scope, name = "Commands") {
            val loading = initialState("Loading")
            val content = state("Content")
            val empty = state("Empty")
            val error = state("Error")

            content {
                transitionConditionally<Loaded> {
                    direction = { if (event.commands.isEmpty()) targetState(empty) else targetState(content) }
                    onTriggered { emit(project(it.event.commands)) }
                }
            }
            empty {
                transitionConditionally<Loaded> {
                    direction = { if (event.commands.isEmpty()) targetState(empty) else targetState(content) }
                    onTriggered { emit(project(it.event.commands)) }
                }
            }
            loading {
                transitionConditionally<Loaded> {
                    direction = { if (event.commands.isEmpty()) targetState(empty) else targetState(content) }
                    onTriggered { emit(project(it.event.commands)) }
                }
                transition<LoadFailed> {
                    onTriggered { emit(CommandsUi.Error(it.event.message)) }
                    targetState = error
                }
            }
            error {
                transition<Retry> {
                    onTriggered { emit(CommandsUi.Loading) }
                    targetState = loading
                }
            }
        }
        publishChartSpec(Json.encodeToString(CHART))
        publishScreenState("Commands", "Loading")
    }

    suspend fun onLoaded(commands: List<Command>) { machine.processEvent(Loaded(commands)) }
    suspend fun onLoadFailed(message: String) { machine.processEvent(LoadFailed(message)) }
    suspend fun retry() { machine.processEvent(Retry) }
}
