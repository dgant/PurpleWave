package Startup

import Operations.Logger
import Processes.{Commander, DecisionMaker, Delegator, Visionary}
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot(var game:bwapi.Game) extends DefaultBWListener {
  val self = game.self
  val visionary = new Visionary()
  val decisionMaker = new DecisionMaker()
  val delegator = new Delegator()
  val commander = new Commander()

  override def onStart(): Unit = {
    Logger.debug("Purple Wave, reporting in.");
    Logger.debug("Reading map");
    BWTA.readMap();

    Logger.debug("Analyzing map");
    BWTA.analyze();

    Logger.debug("Initialization complete");
  }

  override def onFrame(): Unit = {
    var plans = visionary.envisionPlans()
    var decisions = decisionMaker.makeDecisions(plans)
    var tactics = delegator.delegateTactics(decisions)
    commander.command(tactics)
  }
}
