package com.focusx.app.blocking

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent

class FocusAccessibilityService : AccessibilityService() {
    private var lastPackage: String? = null

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event?.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg == packageName) {
            lastPackage = null
            return
        }
        if (pkg !in BlockRuleStore.getBlockedPackages(this) || pkg == lastPackage) return
        lastPackage = pkg

        // Accessibility services can perform a global Home action without inspecting content.
        performGlobalAction(GLOBAL_ACTION_HOME)
        val intent = Intent(this, BlockedActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra(BlockedActivity.EXTRA_PACKAGE, pkg)
        }
        runCatching { startActivity(intent) }
    }

    override fun onInterrupt() { lastPackage = null }
}
