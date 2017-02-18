package Startup

import Development.{AutoCamera, Configuration, Logger, Overlay}
import Plans.GamePlans.PlanWinTheGame
import Processes.{Map, _}
import bwapi.DefaultBWListener
import bwta.BWTA

class Bot() extends DefaultBWListener {

  override def onStart() {
    _try(() => {
      With.logger = new Logger
      
      With.logger.debug("Loading BWTA.")
      BWTA.readMap()
      BWTA.analyze()
      With.logger.debug("BWTA analysis complete.")
      
      With.architect = new Architect
      With.bank = new Banker
      With.economist = new Economist
      With.map = new Map
      With.gameplan = new PlanWinTheGame
      With.history = new History
      With.prioritizer = new Prioritizer
      With.recruiter = new Recruiter
      With.scheduler = new Scheduler
      With.scout = new Scout
      With.tracker = new Tracker

      Overlay.enabled = Configuration.enableOverlay
      AutoCamera.enabled = Configuration.enableCamera
      With.game.enableFlag(1)
      With.game.setLocalSpeed(0)
    })
  }

  override def onFrame() {
    _try(() => {
      With.onFrame()
      With.economist.onFrame()
      With.tracker.onFrame()
      With.bank.onFrame()
      With.recruiter.onFrame()
      With.prioritizer.onFrame()
      With.gameplan.onFrame() //This needs to be last!
      With.scheduler.onFrame()
      Overlay.onFrame()
      AutoCamera.onFrame()
    })
  }

  override def onUnitComplete(unit: bwapi.Unit) {
    _try(() => {
      AutoCamera.focusUnit(unit)
    })
  }

  override def onUnitDestroy(unit: bwapi.Unit) {
    _try(() => {
      With.tracker.onUnitDestroy(unit)
      With.history.onUnitDestroy(unit)
      AutoCamera.focusUnit(unit)
    })
  }

  override def onUnitDiscover(unit: bwapi.Unit) {
    _try(() => {
      AutoCamera.focusUnit(unit)
    })
  }
  
  override def onEnd(isWinner: Boolean) {
    _try(() => {
      With.logger.debug(if (isWinner) "We won!" else "We lost!")
      With.logger.onEnd
      BWTA.cleanMemory()
    })
  }
  
  def _try(action:() => Unit) = {
    try { action() }
    catch { case exception:Exception =>
      if (With.logger != null) {
        With.logger.onException(exception)
      } else {
        System.out.println(exception)
      }}
  }
}
