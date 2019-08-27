package Micro.Actions.Protoss

import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Actions.Combat.Targeting.Target
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Paradrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.transport.isDefined && unit.isAny(Protoss.Reaver, Protoss.HighTemplar)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    val readyToDrop = unit.scarabCount > 0 || unit.energy >= 75
    val centroid = unit
      .squad.map(_.centroid)
      .orElse(unit.battle.filter(_ => unit.matchups.threats.nonEmpty).map(_.teamOf(unit).centroid))

    val home = centroid.getOrElse(unit.agent.origin)

    var target: Option[Pixel] = None
    if (readyToDrop) {
      if (unit.is(Protoss.Reaver)) {
        Target.consider(unit)
        target = unit.agent.toAttack.map(_.pixelCenter)
      }
      target = target.orElse(Some(unit.agent.destination))
    }
  }
}
