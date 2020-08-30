package Lifecycle

import Debugging._
import bwapi.DefaultBWListener

class BotPurpleWave extends DefaultBWListener {

  override def onStart() {
    try {
      With.onStart()
      With.history.onStart()
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }

  override def onFrame() {
    try {
      With.performance.startFrame()
      With.onFrame()
      With.lambdas.update()
      With.tasks.run()
      With.storyteller.onFrame()
      With.performance.endFrame()
    }
    catch { case exception: Exception => With.logger.onException(exception) }

    // If we don't initialize static units on frame 0 we're in trouble
    try {
      if (With.frame == 0 && With.units.neutral.isEmpty) {
        With.units.update()
      }
    } catch { case exception: Exception => With.logger.onException(exception) }
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    try {
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    try {
      With.units.onUnitDestroy(unit)
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    try {
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }
  
  override def onUnitHide(unit: bwapi.Unit) {
    try {
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }
  
  override def onEnd(isWinner: Boolean) {
    try {
      With.logger.debug("Game ended in " + (if (isWinner) "victory" else "defeat"))
      With.history.onEnd(isWinner)
      With.tasks.onEnd()
      With.storyteller.onEnd()
      Manners.onEnd(isWinner)
      With.logger.flush()
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }
  
  override def onSendText(text: String) {
    try {
      KeyboardCommands.onSendText(text)
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }
}
