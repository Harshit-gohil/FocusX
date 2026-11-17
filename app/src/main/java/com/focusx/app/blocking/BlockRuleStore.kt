package com.focusx.app.blocking

import android.content.Context

object BlockRuleStore {
    private const val PREFS = "focusx_block_rules"
    private const val KEY_PACKAGES = "blocked_packages"

    fun getBlockedPackages(context: Context): Set<String> =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getStringSet(KEY_PACKAGES, emptySet())?.toSet() ?: emptySet()

    fun setBlocked(context: Context, packageName: String, blocked: Boolean) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val next = prefs.getStringSet(KEY_PACKAGES, emptySet())?.toMutableSet() ?: mutableSetOf()
        if (blocked) next.add(packageName) else next.remove(packageName)
        prefs.edit().putStringSet(KEY_PACKAGES, next).apply()
    }
}
