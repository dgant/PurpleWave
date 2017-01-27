package Startup

import Development.AutoCamera
import Operations.Logger
import Processes.{Commander, DecisionMaker, Delegator, Visionary}
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {
  val visionary = new Visionary()
  val decisionMaker = new DecisionMaker()
  val delegator = new Delegator()
  val commander = new Commander()

  override def onStart() {
    Logger.debug("Purple Wave, reporting in.")
    Logger.debug("Reading map")
    BWTA.readMap()
    Logger.debug("Analyzing map")
    BWTA.analyze()
    Logger.debug("Bot initialization complete.")
    With.game.setLocalSpeed(1)
  }

  override def onFrame() {
    val plans = visionary.envisionPlans
    val decisions = decisionMaker.makeDecisions(plans)
    val tactics = delegator.delegateTactics(decisions)
    commander.command(tactics)
    AutoCamera.update
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
