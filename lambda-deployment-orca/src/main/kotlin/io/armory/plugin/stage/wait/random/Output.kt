package io.armory.plugin.stage.wait.random

/**
 * This Output is returned from the stage and can be used later in other stages.
 * In this case, the output contains the actual number of seconds the stage waits.
 */
data class Output(var timeToWait: Int) {}
