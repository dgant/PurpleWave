package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    if (unit.agent.canCancel) return true
    
    lazy val unpowered        = unit.unitClass.requiresPsi && ! unit.powered
    lazy val framesCutoff     = 12 + With.reaction.agencyAverage
    lazy val framesToLive     = unit.matchups.framesToLive
    lazy val framesToFinish   = Seq(unit.remainingTechFrames, unit.remainingUpgradeFrames, unit.remainingTrainFrames).max
    lazy val doomed           = unit.matchups.threatsInRange.nonEmpty && framesToLive < framesCutoff
    lazy val willNeverFinish  = unit.matchups.threatsInRange.nonEmpty && framesToLive < framesToFinish
    lazy val producing        = unit.training || unit.upgrading || unit.teching
    lazy val beingBorn        = unit.morphing || unit.beingConstructed
    lazy val isDecoy          = unit.unitClass.attacks && unit.matchups.allies.exists(_.isBeingViolent) // Is this correct?
    lazy val shouldCancel     = (unpowered || beingBorn || (producing && willNeverFinish)) && ! isDecoy
    
    val output = (
      (unit.unitClass.isBuilding || unit.morphing)
      && shouldCancel
      && (doomed || unpowered)
    )
    
    output
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.cancel(unit)
  }
}
