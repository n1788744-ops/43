package com.example

import com.example.data.ChapterCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.ArrayDeque

class ChapterCatalogTest {

    @Test
    fun allFiveManorLevelsHaveValidReachableRooms() {
        assertEquals(5, ChapterCatalog.chapters.size)

        for (chapter in ChapterCatalog.chapters) {
            val grid = chapter.mapGrid
            val rows = grid.size
            val cols = grid.first().length
            assertEquals("Grid must be 15 rows in ${chapter.titleRu}", 15, rows)
            assertTrue("All rows must be 15 cols in ${chapter.titleRu}", grid.all { it.length == 15 })

            var start: Pair<Int, Int>? = null
            val targets = mutableMapOf<Char, Pair<Int, Int>>()

            for (r in 0 until rows) {
                for (c in 0 until cols) {
                    val ch = grid[r][c]
                    if (ch == 'S') start = r to c
                    if (ch != '#' && ch != 'O' && ch != 'I' && ch != 'B' &&
                        ch != '.' && ch != 'T' && ch != 'M' && ch != 'R' && ch != 'D'
                    ) {
                        targets[ch] = r to c
                    }
                }
            }

            val s = requireNotNull(start) { "Missing start 'S' in ${chapter.titleRu}" }
            assertTrue("Missing exit 'E' in ${chapter.titleRu}", targets.containsKey('E'))
            assertTrue("Missing puzzle 'P' in ${chapter.titleRu}", targets.containsKey('P'))
            assertTrue("Missing witch 'W' in ${chapter.titleRu}", targets.containsKey('W'))
            assertTrue("Missing kitchen chest 'K' in ${chapter.titleRu}", targets.containsKey('K'))
            assertTrue("Missing missing-teens evidence 'V' in ${chapter.titleRu}", targets.containsKey('V'))
            assertTrue("Missing rotary phone 'F' in ${chapter.titleRu}", targets.containsKey('F'))
            assertTrue("Missing breaker 'X' in ${chapter.titleRu}", targets.containsKey('X'))
            assertTrue("Missing under-bed stash 'U' in ${chapter.titleRu}", targets.containsKey('U'))

            val visited = Array(rows) { BooleanArray(cols) }
            val queue = ArrayDeque<Pair<Int, Int>>()
            queue.add(s)
            visited[s.first][s.second] = true

            val dirs = arrayOf(-1 to 0, 1 to 0, 0 to -1, 0 to 1)
            while (queue.isNotEmpty()) {
                val (r, c) = queue.removeFirst()
                for ((dr, dc) in dirs) {
                    val nr = r + dr
                    val nc = c + dc
                    if (nr in 0 until rows && nc in 0 until cols && !visited[nr][nc]) {
                        val cell = grid[nr][nc]
                        // Walls '#', Obstacles 'O', Iron Bars 'I', Furniture 'B' are solid; Doors 'D' open!
                        if (cell != '#' && cell != 'O' && cell != 'I' && cell != 'B') {
                            visited[nr][nc] = true
                            queue.add(nr to nc)
                        }
                    }
                }
            }

            for ((marker, pos) in targets) {
                assertTrue(
                    "Marker '$marker' at $pos is unreachable in ${chapter.titleRu}",
                    visited[pos.first][pos.second]
                )
            }
            for (spawn in chapter.itemSpawns) {
                assertTrue(
                    "Item '${spawn.item.nameRu}' ('${spawn.markerChar}') missing or unreachable in ${chapter.titleRu}",
                    targets[spawn.markerChar]?.let { visited[it.first][it.second] } == true
                )
            }
            for (note in chapter.noteSpawns) {
                assertTrue(
                    "Note '${note.titleRu}' ('${note.markerChar}') missing or unreachable in ${chapter.titleRu}",
                    targets[note.markerChar]?.let { visited[it.first][it.second] } == true
                )
            }
        }
    }
}
