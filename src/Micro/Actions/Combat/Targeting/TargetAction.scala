package Micro.Actions.Combat.Targeting

import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Filters._
import Micro.Heuristics.EvaluateTargets
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class TargetAction(val additionalFiltersRequired: TargetFilter*) extends Action {
  
  val additionalFiltersOptional: Vector[TargetFilter] = Vector.empty
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.canFight
    && unit.agent.toAttack.isEmpty
    && unit.canAttack
    && unit.matchups.targets.nonEmpty
  )

  val filtersAlwaysRequired = Vector(
    TargetFilterPossible,
    TargetFilterLarvaAndEgg,
    TargetFilterLeash,
    TargetFilterFocus,
    TargetFilterStayCloaked,
    TargetFilterFutility,
    TargetFilterScourge,
    TargetFilterReaver,
    TargetFilterCrowded,
    TargetFilterTankFodder,
    TargetFilterRush)

  def filtersRequired = filtersAlwaysRequired ++ additionalFiltersRequired

  def legalTargetsRequired(unit: FriendlyUnitInfo): Iterable[UnitInfo] = {
    unit.matchups.targets.view.filter(target => filtersRequired.forall(_.legal(unit, target)))
  }

  def legalTargets(unit: FriendlyUnitInfo): Seq[UnitInfo] = {
    val filtersOptional = additionalFiltersOptional ++ Vector(TargetFilterCombatants, TargetFilterIgnoreScouts)

    def audit =
      unit.matchups.targets.map(target =>
        (target, (filtersOptional ++ filtersRequired).map(filter =>
          (filter.legal(unit, target), filter))))

    val targetsRequired = legalTargetsRequired(unit).toVector
    var output: Seq[UnitInfo] = Seq.empty
    for (filtersOptionalToDrop <- 0 to filtersOptional.length) {
      if (output.isEmpty && unit.agent.toAttack.isEmpty) {
        val filtersOptionalActive = filtersOptional.drop(filtersOptionalToDrop)
        output = targetsRequired.filter(target => filtersOptionalActive.forall(_.legal(unit, target)))
      }
    }

    output
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    val bestTarget = EvaluateTargets.best(unit, legalTargets(unit))
    unit.agent.toAttack = bestTarget
  }
}

