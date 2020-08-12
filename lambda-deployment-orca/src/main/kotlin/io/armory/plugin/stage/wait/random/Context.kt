package io.armory.plugin.stage.wait.random

/**
 * Context is used within the stage itself and returned to the Orca pipeline execution.
 */
data class Context(var maxWaitTime: Int) {}
