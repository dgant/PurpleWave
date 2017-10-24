package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val framesCutoff     = 1.5 * With.reaction.agencyAverage
    lazy val framesToLive     = Math.min(unit.totalHealth / 24.0 / unit.damageInLastSecond, unit.matchups.framesToLiveCurrently)
    lazy val framesToFinish   = Seq(unit.framesBeforeTechComplete, unit.framesBeforeUpgradeComplete, unit.framesBeforeBuildeeComplete).max
    lazy val doomed           = framesToLive < framesCutoff
    lazy val willNeverFinish  = framesToLive < framesToFinish
    lazy val producing        = unit.training || unit.upgrading || unit.teching
    lazy val beingBorn        = unit.morphing || unit.beingConstructed
    lazy val shouldCancel     = beingBorn || (producing && willNeverFinish)
    
    val output = unit.unitClass.isBuilding && doomed && shouldCancel
    output
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    With.commander.cancel(unit)
  }
}
