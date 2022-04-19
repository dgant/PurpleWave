package Lifecycle

import Debugging._
import Placement.JBWEBWrapper
import bwapi.DefaultBWListener
import jbweb.Walls

class PurpleWave extends DefaultBWListener {

  override def onStart(): Unit = {
    tryCatch(() => {
      With.onStart()
      Walls.logInfo = With.configuration.logstd
      JBWEBWrapper.onStart()
      With.history.onStart()
    })
  }

  override def onFrame(): Unit = {
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
      With.camera.onRun(24000)
      With.visualization.onRun(24000)
    })
    // If we don't initialize static units on frame 0 we're in trouble
    tryCatch(() => if (With.frame == 0 && With.units.neutral.isEmpty) { With.units.onFrame() })
  }

  override def onUnitDestroy(unit: bwapi.Unit): Unit = {
    JBWEBWrapper.onUnitDestroy(unit)
    tryCatch(() => With.units.onUnitDestroy(unit))
  }

  override def onUnitComplete(unit: bwapi.Unit): Unit = {}
  override def onUnitDiscover(unit: bwapi.Unit): Unit = {
    JBWEBWrapper.onUnitDiscover(unit)
  }
  override def onUnitHide(unit: bwapi.Unit): Unit = {}

  override def onUnitMorph(unit: bwapi.Unit): Unit = {
    JBWEBWrapper.onUnitMorph(unit)
  }

  override def onUnitRenegade(unit: bwapi.Unit): Unit = {
    tryCatch(() => With.units.onUnitRenegade(unit))
  }
  
  override def onEnd(isWinner: Boolean): Unit = {
    tryCatch(() => {
      With.logger.debug("Game ended in " + (if (isWinner) "victory" else "defeat"))
      With.history.onEnd(isWinner)
      With.gathering.onEnd()
      With.storyteller.onEnd()
      With.manners.onEnd(isWinner)
      With.logger.flush()
    })
  }
  
  override def onSendText(text: String): Unit = {
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
