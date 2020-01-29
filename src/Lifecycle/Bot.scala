package Lifecycle

import Debugging._
import Strategery.History.OpponentLogger
import bwapi.DefaultBWListener

class Bot() extends DefaultBWListener {

  override def onStart() {
    try {
      With.onStart()
      With.logger.debug("OnStart: Frame " + With.frame)
      With.history.onStart()
    }
    catch { case exception: Exception => With.logger.onException(exception) }
  }

  override def onFrame() {
    try {
      if (With.frame < 6) {
        With.logger.debug("OnFrame: Frame " + With.frame)
      }
      With.performance.startFrame()
      With.onFrame()
      if ( ! With.configuration.doAbsolutelyNothing) {
        With.tasks.run()
      }
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
      With.history.onEnd(isWinner)
      OpponentLogger.onEnd()
      Manners.onEnd(isWinner)
      With.onEnd()
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
