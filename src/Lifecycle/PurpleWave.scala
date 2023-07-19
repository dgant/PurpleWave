package Lifecycle

import Debugging._
import Placement.JBWEBWrapper
import Utilities.?
import bwapi.{DefaultBWListener, Player}
import jbweb.Walls

class PurpleWave extends DefaultBWListener {

  private def timeFrameZero(todo: () => Unit): Unit = {
    val msBefore = System.nanoTime / 1000000
    todo()
    if (With.frame == 0) {
      With.frame0ms += System.nanoTime / 1000000 - msBefore
    }
  }

  override def onStart(): Unit = {
    With.frame0ms = 0
    timeFrameZero(() => {
      tryCatch(() => {
        With.onStart()
        Walls.logInfo = With.configuration.logstd
        JBWEBWrapper.onStart()
        With.history.onStart()
        With.units.onStart()
        With.geography.onStart()
      })})
  }

  override def onFrame(): Unit = {
    timeFrameZero(() => {
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
    })
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
      With.logger.debug(f"Game ended in ${?(isWinner, "victory", "defeat")}")
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

  override def onReceiveText(player: Player, text: String): Unit = {
    tryCatch(() => KeyboardCommands.onReceiveText(text))
  }

  private def tryCatch(lambda: () => Unit): Unit = {
    try {
      lambda()
    } catch { case exception: Exception =>
      With.logger.onException(exception)
    }
  }
}
