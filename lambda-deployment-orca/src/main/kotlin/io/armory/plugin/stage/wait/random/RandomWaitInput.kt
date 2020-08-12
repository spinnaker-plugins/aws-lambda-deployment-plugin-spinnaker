package io.armory.plugin.stage.wait.random

/**
 * This the the part of the Context map that we care about as input to the stage execution.
 * The data can be key/value pairs or an entire configuration tree.
 */
data class RandomWaitInput(var maxWaitTime: Int) {}