package Micro.Actions.Basic

import Lifecycle.With
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.{Minutes, Seconds}

object Cancel extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    if (With.framesSince(unit.frameDiscovered) < Seconds(5)()) return false

    if ( ! unit.complete && With.blackboard.toCancel().contains(unit.unitClass)) {
      return true
    }
    if (unit.trainee.exists(With.blackboard.toCancel().contains)) {
      return true
    }
    if (unit.upgradeProducing.exists(With.blackboard.toCancel().contains)) {
      return true
    }
    if (unit.techProducing.exists(With.blackboard.toCancel().contains)) {
      return true
    }

    lazy val unpowered        = unit.unitClass.requiresPsi && ! unit.powered
    lazy val framesCutoff     = 48 + With.reaction.agencyAverage
    lazy val framesToLive     = unit.matchups.framesToLive
    lazy val framesToFinish   = Maff.vmax(unit.remainingTechFrames, unit.remainingUpgradeFrames, unit.remainingTrainFrames)
    lazy val doomed           = unit.battle.isDefined && unit.hitPoints < unit.unitClass.maxHitPoints && unit.matchups.threatsInRange.nonEmpty && framesToLive < framesCutoff
    lazy val willNeverFinish  = unit.matchups.threatsInRange.nonEmpty && framesToLive < framesToFinish
    lazy val canCancel        = unit.isAny(_.unitClass.isBuilding, Zerg.LurkerEgg, Zerg.Egg, Zerg.Cocoon) // Performance hack to avoid accessing .training, etc.
    lazy val producing        = unit.training || unit.upgrading || unit.teching
    lazy val beingBorn        = unit.remainingCompletionFrames > 0
    lazy val isDecoy          = With.frame < Minutes(4)() && (unit.unitClass.canAttack || Zerg.CreepColony(unit)) && unit.matchups.allies.exists(u => u.canAttack && u.friendly.exists(_.intent.canFight)) // The 4-pool use case, essentially
    lazy val shouldCancel     = (unpowered || beingBorn || (producing && willNeverFinish)) && ! isDecoy
    
    val output = (
      canCancel
      && (doomed || unpowered)
      && shouldCancel
    )
    
    output
  }
  
  override def perform(unit: FriendlyUnitInfo): Unit = {
    Commander.cancel(unit)
  }
}
