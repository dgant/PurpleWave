package Micro.Actions.Scouting

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Commands.Attack
import Micro.Matchups.{MatchupAnalysis, MatchupConditions}
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Strategery.Strategies.Zerg.Zerg4Pool

object Kindle extends Action {
  
  private val safetyMarginFrames = 6
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canScout
    && unit.agent.canFight
    && unit.canAttack
    && unit.matchups.framesOfSafety > safetyMarginFrames
    && Zerg4Pool.active
  )
  
  def legalTarget(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = (
    unit.matchups.allies.exists(_.is(Zerg.Zergling))
    || (
      ( ! target.unitClass.attacks|| target.remainingCompletionFrames > unit.framesToTravelPixels(target.pixelRangeAgainst(unit)))
      && MatchupAnalysis(unit, MatchupConditions(unit.pixelToFireAt(target), 0)).framesOfSafety > safetyMarginFrames
    )
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val legalTargets = unit.matchups.targets.filter(legalTarget(unit, _))
    val targetAction = new TargetAction(TargetFilterWhitelist(legalTargets))
    targetAction.delegate(unit)
    Attack.delegate(unit)
  }
}
