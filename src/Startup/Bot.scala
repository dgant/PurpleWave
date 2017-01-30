package Startup

import Development.AutoCamera
import Operations.Logger
import Processes.Allocation.{Banker, Recruiter}
import Processes._
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {
  val planner = new Planner()
  val commander = new Commander()

  override def onStart() {
    Logger.debug("Purple Wave, reporting in.")
    With.bank = new Banker
    With.recruiter = new Recruiter
    Logger.debug("Reading map")
    BWTA.readMap()
    Logger.debug("Analyzing map")
    BWTA.analyze()
    Logger.debug("Bot initialization complete.")
    With.game.setLocalSpeed(1)
  }
  
  def _onFrame() {
    //Update Bank & Recruiter
    With.bank.tally()
    With.recruiter.tally()
  
    //Update priorities for each buyer
    var priority = 0
    planner.plans.foreach(_.update())
    planner.plans.foreach(plan => {
      plan.priority = priority
      priority += 1
    })
    
    //Fulfill minimum requirements
    planner.plans.foreach(_.update)
    planner.plans.foreach(_.requirementsMinimal.fulfill)
    
    //Fulfill remaining requirements for active plans
    val activePlans = planner.plans.filter(_.active)
    var inactivePlans = planner.plans.filterNot(_.active)
    inactivePlans.foreach(_.abort())
    activePlans.foreach(_.requirementsOptional.fulfill)
    activePlans.foreach(_.requirementsOptimal.fulfill)
    activePlans.foreach(_.execute())
    
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
