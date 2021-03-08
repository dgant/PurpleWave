package Lifecycle

import Debugging._
import bwapi.DefaultBWListener

class BotPurpleWave extends DefaultBWListener {

  override def onStart() {
    tryCatch(() => {
      With.onStart()
      With.history.onStart()
    })
  }

  override def onFrame() {
    tryCatch(() => {
      With.performance.startFrame()
      With.onFrame()
      With.latency.onFrame()
      With.units.onFrame()
      With.gathering.updateAccelerantPixels()
      With.lambdas.onFrame()
      With.tasks.run(With.performance.msBeforeTarget)
      With.storyteller.onFrame()
      With.performance.endFrame()
    })
    // If we don't initialize static units on frame 0 we're in trouble
    tryCatch(() => if (With.frame == 0 && With.units.neutral.isEmpty) { With.units.onFrame() })
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    tryCatch(() => With.units.onUnitDestroy(unit))
  }

  override def onUnitComplete(unit: bwapi.Unit) {}
  override def onUnitDiscover(unit: bwapi.Unit) {}
  override def onUnitHide(unit: bwapi.Unit) {}

  override def onUnitRenegade(unit: bwapi.Unit): Unit = {
    tryCatch(() => With.units.onUnitRenegade(unit))
  }
  
  override def onEnd(isWinner: Boolean) {
    tryCatch(() => {
      With.logger.debug("Game ended in " + (if (isWinner) "victory" else "defeat"))
      With.history.onEnd(isWinner)
      With.storyteller.onEnd()
      With.manners.onEnd(isWinner)
      With.logger.flush()
    })
  }
  
  override def onSendText(text: String) {
    tryCatch(() => KeyboardCommands.onSendText(text))
  }

  private def tryCatch(lambda: () => Unit): Unit = {
    try {
      lambda()
    } catch { case exception: Exception =>
      With.logger.onException(exception)
    }
  }
}
