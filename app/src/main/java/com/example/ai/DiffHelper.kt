package com.example.ai

object DiffHelper {

    enum class DiffType {
        SAME, ADDED, REMOVED
    }

    data class DiffLine(
        val type: DiffType,
        val text: String,
        val oldLineNumber: Int? = null,
        val newLineNumber: Int? = null
    )

    data class DiffResult(
        val lines: List<DiffLine>,
        val additionsCount: Int,
        val deletionsCount: Int
    )

    /**
     * Compute a simple Myers/LCS-based or line-matching diff suitable for UI preview.
     */
    fun computeDiff(original: String, modified: String): DiffResult {
        val origLines = original.lines()
        val modLines = modified.lines()

        val lcs = computeLCS(origLines, modLines)
        val result = mutableListOf<DiffLine>()

        var i = 0
        var j = 0
        var additions = 0
        var deletions = 0

        while (i < origLines.size || j < modLines.size) {
            if (i < origLines.size && j < modLines.size && origLines[i] == modLines[j]) {
                result.add(DiffLine(DiffType.SAME, origLines[i], i + 1, j + 1))
                i++
                j++
            } else if (j < modLines.size && (i >= origLines.size || !lcs.contains(origLines[i]) || (lcs.contains(modLines[j]) && origLines[i] != modLines[j]))) {
                result.add(DiffLine(DiffType.ADDED, modLines[j], null, j + 1))
                additions++
                j++
            } else if (i < origLines.size) {
                result.add(DiffLine(DiffType.REMOVED, origLines[i], i + 1, null))
                deletions++
                i++
            } else {
                break
            }
        }

        return DiffResult(result, additions, deletions)
    }

    private fun computeLCS(s1: List<String>, s2: List<String>): Set<String> {
        val set2 = s2.toHashSet()
        return s1.filter { set2.contains(it) }.toSet()
    }
}
