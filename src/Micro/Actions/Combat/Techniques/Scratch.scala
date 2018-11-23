package Micro.Actions.Combat.Techniques

import Micro.Actions.Combat.Targeting.Filters.TargetFilterWhitelist
import Micro.Actions.Combat.Targeting.TargetAction
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import Micro.Actions.Commands.Attack
import Micro.Matchups.{MatchupAnalysis, MatchupConditions}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Scratch extends ActionTechnique {
  
  // Even if we don't want to fight, maybe we safely poke SOMETHING
  
  private val safetyMarginFrames = 6
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.agent.canFight
    && unit.matchups.threats.forall(t => t.pixelRangeAgainst(unit) < unit.pixelRangeMax && t.topSpeed <= unit.topSpeed)
    && unit.matchups.framesOfSafety >= safetyMarginFrames
    && (unit.flying || unit.zone.exit.map(_.pixelCenter).forall(exit =>
      ByOption
        .min(unit.matchups.threats.map(_.framesToTravelTo(exit)))
        .forall(_ > unit.framesToTravelTo(exit) + safetyMarginFrames)
    ))
  )
  
  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val legalTargets = unit.matchups.targets.filter(target => {
      val attackPoint = unit.pixelCenter.project(target.pixelCenter, unit.pixelDistanceEdge(target))
      lazy val matchup = MatchupAnalysis(unit, MatchupConditions(attackPoint, 0))
      val output = (
        ! target.canAttack(unit)
        && matchup.framesOfSafety >= safetyMarginFrames)
      output
    })
    
    val targetAction = new TargetAction(TargetFilterWhitelist(legalTargets))
    targetAction.delegate(unit)
    Attack.delegate(unit)
  }
}
