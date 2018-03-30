package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Filters._
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class TargetAction(val additionalFiltersRequired: TargetFilter*) extends Action {
  
  val additionalFiltersOptional: Vector[TargetFilter] = Vector.empty
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    var filtersOptional = additionalFiltersOptional ++ Vector(TargetFilterCombatants, TargetFilterIgnoreScouts)
    val filtersRequired = Vector(TargetFilterStayCloaked, TargetFilterMission, TargetFilterAlmostAnything) ++ additionalFiltersRequired
    
    do {
      val filters: Vector[TargetFilter] = if (Yolo.active) Vector.empty else filtersOptional ++ filtersRequired
      ApplyTargetFilters(unit, filters)
      filtersOptional = filtersOptional.drop(1)
    }
    while (unit.agent.toAttack.isEmpty && filtersOptional.nonEmpty)
  }
}

