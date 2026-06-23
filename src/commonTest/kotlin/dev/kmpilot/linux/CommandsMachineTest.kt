package dev.kmpilot.linux

import dev.kmpilot.linux.domain.Command
import dev.kmpilot.linux.presentation.CommandsMachine
import dev.kmpilot.linux.presentation.CommandsUi
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** Acceptance criteria for the Commands screen statechart: Loading → Content / Empty / Error (+ retry). */
class CommandsMachineTest {
    private val cmds = listOf(
        Command("ls", "Files", "List", "ls", emptyList()),
        Command("cat", "Files", "Cat", "cat", emptyList()),
    )

    @Test fun starts_loading() = runTest {
        val m = CommandsMachine(backgroundScope); m.start()
        assertEquals(CommandsUi.Loading, m.ui.value)
    }

    @Test fun loaded_with_commands_becomes_content() = runTest {
        val m = CommandsMachine(backgroundScope); m.start()
        m.onLoaded(cmds); runCurrent()
        val ui = m.ui.value
        assertTrue(ui is CommandsUi.Content)
        assertEquals(2, ui.commands.size)
    }

    @Test fun loaded_with_no_matches_becomes_empty() = runTest {
        val m = CommandsMachine(backgroundScope); m.start()
        m.onLoaded(emptyList()); runCurrent()
        assertEquals(CommandsUi.Empty, m.ui.value)
    }

    @Test fun empty_recovers_to_content_when_a_search_matches() = runTest {
        val m = CommandsMachine(backgroundScope); m.start()
        m.onLoaded(emptyList()); runCurrent(); assertEquals(CommandsUi.Empty, m.ui.value)
        m.onLoaded(cmds); runCurrent(); assertTrue(m.ui.value is CommandsUi.Content)
    }

    @Test fun failure_goes_to_error_then_retry_reloads() = runTest {
        val m = CommandsMachine(backgroundScope); m.start()
        m.onLoadFailed("disk gone"); runCurrent()
        val ui = m.ui.value
        assertTrue(ui is CommandsUi.Error)
        assertEquals("disk gone", ui.message)
        m.retry(); runCurrent()
        assertEquals(CommandsUi.Loading, m.ui.value)
    }
}
