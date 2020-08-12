package io.armory.plugin.stage.wait.random

import com.netflix.spinnaker.orca.api.simplestage.SimpleStageInput
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageOutput
import com.netflix.spinnaker.orca.api.simplestage.SimpleStageStatus
import dev.minutest.junit.JUnit5Minutests
import dev.minutest.rootContext
import strikt.api.expectThat
import strikt.assertions.isEqualTo

class RandomWaitStageTest : JUnit5Minutests {

    fun tests() = rootContext {
        test("execute random wait stage") {
            expectThat(RandomWaitStage(RandomWaitConfig(30)).execute(SimpleStageInput(RandomWaitInput(1))))
                    .isEqualTo(
                            SimpleStageOutput<Output, Context>().apply {
                                status = SimpleStageStatus.SUCCEEDED
                                output = Output(0)
                                context = Context(1)
                            }
                    )
        }
    }
}
