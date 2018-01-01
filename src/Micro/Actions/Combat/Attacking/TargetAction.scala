package Micro.Actions.Combat.Attacking

import Micro.Actions.Action
import Micro.Actions.Combat.Attacking.Filters._
import Planning.Yolo
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

abstract class TargetAction extends Action {
  
  val additionalFilters: Vector[TargetFilter] = Vector.empty
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.agent.canFight           &&
    unit.agent.toAttack.isEmpty   &&
    unit.canAttack                &&
    unit.matchups.targets.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    var filtersOptional = Vector(TargetFilterCombatants, TargetFilterIgnoreScouts)
    val filtersRequired = Vector(TargetFilterStayCloaked, TargetFilterMission, TargetFilterAlmostAnything) ++ additionalFilters
    
    do {
      val filters: Vector[TargetFilter] = if (Yolo.active) Vector.empty else filtersOptional ++ filtersRequired
      ApplyTargetFilters(unit, filters)
      filtersOptional = filtersOptional.drop(1)
    }
    while (unit.agent.toAttack.isEmpty && filtersOptional.nonEmpty)
    
    val target = unit.agent.toAttack
  }
}

