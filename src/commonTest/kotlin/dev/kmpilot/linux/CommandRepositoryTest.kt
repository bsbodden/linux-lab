package dev.kmpilot.linux

import dev.kmpilot.linux.data.CommandRepository
import dev.kmpilot.linux.domain.Command
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Acceptance criteria for the command reference repository — the search/filter contract. */
class CommandRepositoryTest {
    private val repo = CommandRepository()

    @Test fun categories_are_distinct_and_sorted() {
        val cats = repo.categories()
        assertEquals(cats.distinct(), cats)
        assertEquals(cats.sorted(), cats)
    }

    @Test fun empty_search_returns_all_sorted_by_name() {
        val all = repo.search("")
        assertEquals(20, all.size)
        assertEquals(all.map { it.name }.sorted(), all.map { it.name })
    }

    @Test fun search_matches_a_name_substring() {
        val hits = repo.search("ch").map { it.name }
        assertTrue("chmod" in hits)
        assertTrue("chown" in hits)
    }

    @Test fun search_matches_summary_text() {
        assertTrue(repo.search("directory").any { it.name == "cd" })   // "...working directory"
    }

    @Test fun search_is_case_insensitive() {
        assertEquals(repo.search("GREP").map { it.name }, repo.search("grep").map { it.name })
    }

    @Test fun search_with_no_match_is_empty() {
        assertTrue(repo.search("zzz-not-a-command").isEmpty())
    }

    @Test fun by_category_filters_and_sorts() {
        val files = repo.byCategory("Files")
        assertTrue(files.isNotEmpty())
        assertTrue(files.all { it.category == "Files" })
        assertEquals(files.map { it.name }.sorted(), files.map { it.name })
    }

    @Test fun by_name_finds_exact_or_null() {
        assertEquals("grep", repo.byName("grep")?.name)
        assertNull(repo.byName("not-a-command"))
    }

    @Test fun count_equals_category_size() {
        assertEquals(repo.byCategory("Files").size, repo.count("Files"))
    }

    @Test fun a_custom_command_list_is_honoured() {
        val r = CommandRepository(listOf(Command("x", "Demo", "summary", "x", emptyList())))
        assertEquals(1, r.search("").size)
        assertEquals(listOf("Demo"), r.categories())
    }
}
