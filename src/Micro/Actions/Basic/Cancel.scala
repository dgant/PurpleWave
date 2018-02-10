package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val unpowered        = unit.unitClass.requiresPsi && ! unit.powered
    lazy val framesCutoff     = 12 + With.reaction.agencyAverage
    lazy val framesToLive     = unit.matchups.framesToLiveCurrently
    lazy val framesToFinish   = Seq(unit.framesBeforeTechComplete, unit.framesBeforeUpgradeComplete, unit.framesBeforeBuildeeComplete).max
    lazy val doomed           = framesToLive < framesCutoff
    lazy val willNeverFinish  = framesToLive < framesToFinish
    lazy val producing        = unit.training || unit.upgrading || unit.teching
    lazy val beingBorn        = unit.morphing || unit.beingConstructed
    lazy val isDecoy          = unit.unitClass.attacks && unit.matchups.allies.exists(_.isBeingViolent) // Is this correct?
    lazy val shouldCancel     = (unpowered || beingBorn || (producing && willNeverFinish)) && ! isDecoy
    
    val output = (unit.unitClass.isBuilding || unit.morphing) && doomed && shouldCancel
    output
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.cancel(unit)
  }
}
