package Micro.Actions.Basic

import Lifecycle.With
import Micro.Actions.Action
import Micro.Agency.Commander
import Planning.UnitMatchers.MatchBuilding
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.{Minutes, Seconds}

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    if (unit.intent.shouldCancel) return true
    if (With.framesSince(unit.frameDiscovered) < Seconds(5)()) return false

    lazy val unpowered        = unit.unitClass.requiresPsi && ! unit.powered
    lazy val framesCutoff     = 48 + With.reaction.agencyAverage
    lazy val framesToLive     = unit.matchups.framesToLive
    lazy val framesToFinish   = Seq(unit.remainingTechFrames, unit.remainingUpgradeFrames, unit.remainingTrainFrames).max
    lazy val doomed           = unit.battle.isDefined && unit.matchups.threatsInRange.nonEmpty && framesToLive < framesCutoff
    lazy val willNeverFinish  = unit.matchups.threatsInRange.nonEmpty && framesToLive < framesToFinish
    lazy val canCancel        = unit.isAny(MatchBuilding, Zerg.LurkerEgg, Zerg.Egg, Zerg.Cocoon) // Performance hack to avoid accessing .training, etc.
    lazy val producing        = unit.training || unit.upgrading || unit.teching
    lazy val beingBorn        = unit.remainingCompletionFrames > 0
    lazy val isDecoy          = With.frame < Minutes(4)() && (unit.unitClass.canAttack || unit.is(Zerg.CreepColony)) && unit.matchups.allies.exists(_.isBeingViolent) // The 4-pool use case, essentially
    lazy val shouldCancel     = (unpowered || beingBorn || (producing && willNeverFinish)) && ! isDecoy
    
    val output = (
      canCancel
      && (doomed || unpowered)
      && shouldCancel
    )
    
    output
  }
  
  override def perform(unit: FriendlyUnitInfo) {
    Commander.cancel(unit)
  }
}
