package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import Micro.Agency.Commander
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Strategies.Zerg.ZvE4Pool

object Kindle extends Action {
  
  private val safetyMarginFrames = 6
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.isScout
    && unit.agent.canFight
    && unit.canAttack
    && unit.matchups.framesOfSafety > safetyMarginFrames
    && ZvE4Pool.active
  )
  
  def legalTarget(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    unit.matchups.allies.exists(_.is(Zerg.Zergling))
    || (
      ( ! target.unitClass.canAttack|| target.remainingCompletionFrames > unit.framesToTravelPixels(target.pixelRangeAgainst(unit)))
      && unit.matchups.framesOfSafety > safetyMarginFrames
    )
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val legalTargets = unit.matchups.targets.filter(legalTarget(unit, _))
    Target.choose(unit, legalTargets)
    Commander.attack(unit)
  }
}
