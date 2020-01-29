package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Support extends Action {

  def isSupport(unit: UnitInfo): Boolean = ! unit.canAttack || unit.unitClass.isWorker

  override def allowed(unit: FriendlyUnitInfo): Boolean = isSupport(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {

    // If useless, just go home as normal
    if ( ! unit.unitClass.isTransport
      && unit.unitClass.spells.forall(tech => ! With.self.hasTech(tech) || unit.energy < tech.energyCost)) {
      return
    }

    // Who can we support?
    var supportables = unit.matchups.allies.filterNot(isSupport)
    if (supportables.isEmpty) supportables = unit.squad.map(_.units.view.filterNot(isSupport).toVector).getOrElse(Vector.empty)
    if (supportables.isEmpty) return
    val destination = unit.battle
      .map(_.us.vanguard)
      .getOrElse({
        val centroid = PurpleMath.centroid(supportables.map(_.pixelCenter))
        supportables.minBy(_.pixelDistanceTravelling(unit.agent.destination)).pixelCenter
      })

    // Retreat to help
    if (!unit.visibleToOpponents) {
      unit.agent.toReturn = Some(destination)
    }

    // Travel to team
    if (unit.isAny(Terran.Medic, Protoss.Arbiter, Protoss.DarkArchon, Protoss.HighTemplar, Zerg.Defiler, Zerg.Queen)) {
      unit.agent.toTravel = Some(destination)
    }
  }
}
