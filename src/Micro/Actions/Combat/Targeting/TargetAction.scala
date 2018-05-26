package Micro.Actions.Combat.Targeting

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters._
import Micro.Heuristics.Targeting.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class TargetAction(val additionalFiltersRequired: TargetFilter*) extends Action {
  
  val additionalFiltersOptional: Vector[TargetFilter] = Vector.empty
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canFight
    && unit.agent.toAttack.isEmpty
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    var filtersOptional = additionalFiltersOptional ++ Vector(TargetFilterCombatants, TargetFilterIgnoreScouts)
    val filtersRequired = Vector(TargetFilterStayCloaked, TargetFilterFutility, TargetFilterAlmostAnything) ++ additionalFiltersRequired
    
    val targetsRequired = new mutable.ListBuffer[UnitInfo]
    for (target <- unit.matchups.targets) {
      if (filtersRequired.forall(_.legal(unit, target))) targetsRequired += target
    }
    
    def audit =
      unit.matchups.targets.map(target =>
        (target, (filtersOptional ++ filtersRequired).map(filter =>
          (filter.legal(unit, target), filter))))
    
    do {
      val targetsOptional = targetsRequired.filter(target => filtersOptional.forall(_.legal(unit, target)))
      unit.agent.toAttack = EvaluateTargets.best(unit, targetsOptional)
      filtersOptional = filtersOptional.drop(1)
    }
    while (unit.agent.toAttack.isEmpty && filtersOptional.nonEmpty)
    
    Unit
  }
}

