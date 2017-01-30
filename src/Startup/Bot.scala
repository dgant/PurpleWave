package Startup

import Development.AutoCamera
import Operations.Logger
import Processes.Allocation.{Banker, Recruiter}
import Processes._
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {
  val visionary = new Planner()
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
    visionary.plans.foreach(_.update())
    visionary.plans.foreach(plan => {
      plan.priority = priority
      priority += 1
    })
    
    //For each plan
      //Try to fulfill all requirements
      //If successful, mark plan as active
      //If unsuccessful, mark plan as inactive, and release all contracts
    
    //Fulfill
    visionary.plans.foreach(plan => {
      plan.requirementsMinimal
    })
    
    //Reallocate resources
    
    AutoCamera.update()
  }

  override def onFrame() {
    try {
      _onFrame()
    }
    catch {
      case exception:Exception =>
        Logger.debug("EXCEPTION")
        Logger.debug(exception.getMessage)
        exception.printStackTrace()
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
