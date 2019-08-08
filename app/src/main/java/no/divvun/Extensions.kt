package no.divvun

import kotlin.math.min
import java.util.*

/**
 * Because Android likes strings as locale.
 * https://softwareengineering.stackexchange.com/questions/325458/parse-both-en-us-and-en-us-as-locale-in-java
 */
fun String.toLocale(): Locale {
    return Locale.forLanguageTag(replace('_', '-'))
}

fun String.toLanguageTag(): String {
    return toLocale().toLanguageTag()
}

fun levenshtein(lhs: CharSequence, rhs: CharSequence): Int {
    val lhsLength = lhs.length
    val rhsLength = rhs.length

    var cost = Array(lhsLength) { it }
    var newCost = Array(lhsLength) { 0 }

    for (i in 1 until rhsLength) {
        newCost[0] = i

        for (j in 1 until lhsLength) {
            val match = if (lhs[j - 1] == rhs[i - 1]) 0 else 1

            val costReplace = cost[j - 1] + match
            val costInsert = cost[j] + 1
            val costDelete = newCost[j - 1] + 1

            newCost[j] = min(min(costInsert, costDelete), costReplace)
        }

        val swap = cost
        cost = newCost
        newCost = swap
    }

    return cost[lhsLength - 1]
}

fun String.levenshteinTo(other: String): Int {
    return levenshtein(this, other)
}
