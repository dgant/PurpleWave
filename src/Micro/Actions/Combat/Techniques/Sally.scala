package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Decisionmaking.Engage
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Sally extends ActionTechnique {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.agent.toForm.isDefined

  override val applicabilityBase: Double = 1.0
  override def applicabilitySelf(unit: FriendlyUnitInfo): Double = if (unit.matchups.targetsInRange.isEmpty && unit.matchups.targets.nonEmpty) 1.0 else 0.0

  override def applicabilityOther(unit: FriendlyUnitInfo, other: UnitInfo): Option[Double] = {
    if (other.isFriendly) {
      if (other.unitClass.isBuilding && ! other.canAttack) return None
      if (other.matchups.threats.isEmpty) return None
      if ( ! other.visibleToOpponents) return Some(0.0)
      if (other.friendly.flatMap(_.agent.toForm).map(_.tile).exists( ! With.grids.enemyVision.isSet(_))) return Some(0.0)
      return Some(if (other.matchups.threats.exists(t => t.inRangeToAttack(other, other.friendly.flatMap(_.agent.toForm).getOrElse(t.pixelCenter)))) 1.0 else 0.0)
    } else if (other.isEnemy) {
      if (other.matchups.targetsInRange.exists(_.friendly.exists(_.agent.toForm.isDefined)) && other.effectiveRangePixels > unit.effectiveRangePixels) {
        return Some(1.0)
      }
    }
    None
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.matchups.targets.nonEmpty) {
      Engage.delegate(unit)
    } else {
      Hunker.delegate(unit)
    }
  }
}
