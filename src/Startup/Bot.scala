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
      With.map = new Map
      With.gameplan = new PlanWinTheGame
      With.prioritizer = new Prioritizer
      With.recruiter = new Recruiter
      With.scout = new Scout

      Overlay.enabled = Configuration.enableOverlay
      AutoCamera.enabled = Configuration.enableCamera
      With.game.enableFlag(1)
      With.game.setLocalSpeed(0)
    })
  }

  override def onFrame() {
    _try(() => {
      With.scout.onFrame()
      With.bank.onFrame()
      With.recruiter.onFrame()
      With.prioritizer.onFrame()
      With.gameplan.onFrame() //This needs to be last!
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
      With.scout.onUnitDestroy(unit)
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
