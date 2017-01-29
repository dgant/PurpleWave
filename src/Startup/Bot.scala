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

  override def onFrame() {
    try {
      With.bank.tally()
      With.recruiter.tally()
      val plans = visionary.getPlans()
      //val tactics = delegator.delegateTactics(decisions)
      //
      //commander.command(tactics)
      //TODO
      AutoCamera.update()
    }
    catch {
      case e:Exception =>
        Logger.debug("EXCEPTION")
        Logger.debug(e.getMessage)
        e.printStackTrace()
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
