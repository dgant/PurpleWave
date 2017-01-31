package Startup

import Development.{AutoCamera, Overlay}
import Operations.Logger
import Processes.Allocation.{Banker, Recruiter}
import Processes._
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    Logger.debug("Purple Wave, reporting in.")
    With.bank = new Banker
    With.recruiter = new Recruiter
    With.planner = new Planner
    Logger.debug("Reading map")
    BWTA.readMap()
    Logger.debug("Analyzing map")
    BWTA.analyze()
    Logger.debug("Bot initialization complete.")
    With.game.setLocalSpeed(0)
  }
  
  def _onFrame() {
    //Update Bank & Recruiter
    With.bank.tally()
    With.recruiter.tally()
    With.planner.plans.foreach(_.requireInitialization)
  
    //Update priorities for each buyer
    var priority = 0
    With.planner.plans.foreach(plan => {
      plan.priority = priority
      priority += 1
    })
    
    //Fulfill minimum requirements
    With.planner.plans.filterNot(_.isComplete).foreach(_.requirements.fulfill)
    
    //Fulfill remaining requirements for active plans
    val activePlans = With.planner.plans.filter(_.active)
    val inactivePlans = With.planner.plans.filterNot(_.active)
    inactivePlans.foreach(_.abort())
    
    //Get and execute tactics
    val tactics = activePlans.flatten(_.execute())
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
