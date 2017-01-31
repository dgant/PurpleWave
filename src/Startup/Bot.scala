package Startup

import Development.{AutoCamera, Overlay}
import Operations.Logger
import Processes.{Banker, Prioritizer, Recruiter}
import Types.Plans.PlanWinTheGame
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    Logger.debug("Purple Wave, reporting in.")
    With.bank = new Banker
    With.recruiter = new Recruiter
    With.prioritizer = new Prioritizer
    With.gameplan = new PlanWinTheGame
    Logger.debug("Reading map")
    BWTA.readMap()
    Logger.debug("Analyzing map")
    BWTA.analyze()
    Logger.debug("Bot initialization complete.")
    With.game.setLocalSpeed(1)
  }
  
  def _onFrame() {
    With.bank.startFrame()
    With.recruiter.startFrame()
    With.prioritizer.startFrame()
    With.gameplan.startFrame()
    val tactics = With.gameplan.execute()
    tactics.foreach(_.execute())
    Overlay.update()
    AutoCamera.update()
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
