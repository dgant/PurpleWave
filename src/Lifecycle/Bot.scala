package Lifecycle

import Debugging.{Visualization, _}
import bwapi.DefaultBWListener

class Bot() extends DefaultBWListener {

  override def onStart() {
    try {
     With.onStart()
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onFrame() {
    try {
      With.onFrame()
      With.performance.startCounting()
      With.latency.onFrame()
      if (With.latency.shouldRun) {
        
        //Observe
        With.units.onFrame()
        With.geography.onFrame()
        With.grids.onFrame()
        
        //Orient
        With.battles.onFrame()
        With.economy.onFrame()
        With.realEstate.onFrame()
        With.bank.onFrame()
        With.recruiter.onFrame()
        With.prioritizer.onFrame()
        
        //Decide
        With.gameplan.onFrame()
        With.scheduler.onFrame()
        
        //Act
        With.commander.onFrame()
        With.executor.onFrame()
      }
      With.performance.stopCounting()
      
      With.camera.onFrame()
      Visualization.Visualization.onFrame()
      Manners.onFrame()
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)
    }
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    try {
      With.units.onUnitDestroy(unit)
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    try {
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  override def onEnd(isWinner: Boolean) {
    try {
      Manners.onEnd(isWinner)
      With.onEnd()
    }
    catch { case exception:Exception =>
      val dontLoseTheExceptionWhileDebugging = exception
      val dontLoseTheStackTraceWhileDebugging = exception.getStackTrace
      With.logger.onException(exception)}
  }
  
  override def onSendText(text: String) {
   KeyboardCommands.onSendText(text)
  }
}
