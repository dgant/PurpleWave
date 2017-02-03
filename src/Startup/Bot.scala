package Startup

import Development.{AutoCamera, Logger, Overlay}
import Processes._
import Plans.GamePlans.PlanWinTheGame
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    Logger.debug("Purple Wave, reporting in.")
    
    With.architect = new Architect
    With.bank = new Banker
    With.gameplan = new PlanWinTheGame
    With.prioritizer = new Prioritizer
    With.recruiter = new Recruiter
    With.scout = new Scout
    
    Logger.debug("Reading map")
    BWTA.readMap()
    Logger.debug("Analyzing map")
    BWTA.analyze()
    Logger.debug("Bot initialization complete.")
    
    val manualControl = true
    AutoCamera.enabled = ! manualControl
    
    if (manualControl) {
      With.game.enableFlag(1) //Enable user input
      With.game.enableFlag(2) //black sheep wall
      With.game.setLocalSpeed(5)
    } else {
      With.game.setLocalSpeed(0)
    }
  }
  
  def _onFrame() {
    With.architect.update()
    With.bank.recountResources()
    With.recruiter.recountUnits()
    With.prioritizer.reassignPriorities()
    With.gameplan.execute()
    With.scout.update()
    Overlay.render()
    AutoCamera.render()
  }

  override def onFrame() {
    try {
      _onFrame()
    }
    catch {
      case exception:Exception =>
        exception.printStackTrace()
        Logger.debug(exception.getClass.getSimpleName)
        
        if (exception.getStackTrace.nonEmpty) {
          Logger.debug(
            exception.getStackTrace.head.getClassName
            + "."
            + exception.getStackTrace.head.getMethodName
            + "(): "
            + exception.getStackTrace.head.getLineNumber)
        }
    }
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    AutoCamera.focusUnit(unit)
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    AutoCamera.focusUnit(unit)
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    AutoCamera.focusUnit(unit)
  }
}
