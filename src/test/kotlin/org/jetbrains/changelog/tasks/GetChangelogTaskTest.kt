package org.jetbrains.changelog.tasks

import org.jetbrains.changelog.BaseTest
import org.jetbrains.changelog.ChangelogPluginConstants.GET_CHANGELOG_TASK_NAME
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetChangelogTaskTest : BaseTest() {

    @BeforeTest
    fun localSetUp() {
        changelog =
            """
            # Changelog
            ## [Unreleased]
            - bar
            ## [1.0.0]
            ### Added
            - foo
            """

        buildFile =
            """
            plugins {
                id 'org.jetbrains.changelog'
            }
            changelog {
                version = "1.0.0"
            }
            """

        project.evaluate()
    }

    @Test
    fun `returns change notes for the version specified with extension`() {
        val result = runTask(GET_CHANGELOG_TASK_NAME, "--quiet")

        assertEquals(
            """
            ## [1.0.0]
            ### Added
            - foo
            """.trimIndent(),
            result.output.trim()
        )
    }

    @Test
    fun `returns the Unreleased change notes`() {
        val result = runTask(GET_CHANGELOG_TASK_NAME, "--quiet", "--unreleased")

        assertEquals(
            """
            ## [Unreleased]
            - bar
            """.trimIndent(),
            result.output.trim()
        )
    }

    @Test
    fun `returns change notes without header for the version specified with extension`() {
        val result = runTask(GET_CHANGELOG_TASK_NAME, "--quiet", "--no-header")

        assertEquals(
            """
            ### Added
            - foo
            """.trimIndent(),
            result.output.trim()
        )
    }

    @Test
    fun `returns change notes with Pattern set to headerParserRegex`() {
        buildFile =
            """
            plugins {
                id 'org.jetbrains.changelog'
            }
            changelog {
                version = "1.0.0"
                headerParserRegex = ~/\d\.\d\.\d/
            }
            """

        project.evaluate()

        runTask(GET_CHANGELOG_TASK_NAME)
    }

    @Test
    fun `returns change notes with String set to headerParserRegex`() {
        buildFile =
            """
            plugins {
                id 'org.jetbrains.changelog'
            }
            changelog {
                version = "1.0.0"
                headerParserRegex = "\\d\\.\\d\\.\\d"
            }
            """

        project.evaluate()

        runTask(GET_CHANGELOG_TASK_NAME)
    }

    @Test
    fun `fails with Integer set to headerParserRegex`() {
        buildFile =
            """
            plugins {
                id 'org.jetbrains.changelog'
            }
            changelog {
                version = "1.0.0"
                headerParserRegex = 123
            }
            """

        project.evaluate()

        runFailingTask(GET_CHANGELOG_TASK_NAME)
    }

    @Test
    fun `throws VersionNotSpecifiedException when changelog extension has no version provided`() {
        buildFile =
            """
            plugins {
                id 'org.jetbrains.changelog'
            }
            changelog {
            }
            """

        project.evaluate()

        val result = runFailingTask(GET_CHANGELOG_TASK_NAME)

        assertTrue(
            result.output.contains(
                "org.jetbrains.changelog.exceptions.VersionNotSpecifiedException: Version is missing. " +
                    "Please provide the project version to the `project` or `changelog.version` property explicitly."
            )
        )
    }

    @Test
    fun `task loads from the configuration cache`() {
        runTask(GET_CHANGELOG_TASK_NAME, "--configuration-cache")
        val result = runTask(GET_CHANGELOG_TASK_NAME, "--configuration-cache")

        assertTrue(result.output.contains("Reusing configuration cache."))
    }
}
