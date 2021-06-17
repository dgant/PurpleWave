package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.Maff
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
    var supportables = unit.alliesSquadThenBattle.view.map(_.filterNot(isSupport)).find(_.nonEmpty).getOrElse(Vector.empty)
    if (supportables.isEmpty) return
    val destination = unit.battle
      .map(_.us.vanguard())
      .getOrElse({
        val centroid = Maff.centroid(supportables.map(_.pixel))
        supportables.minBy(_.pixelDistanceTravelling(unit.agent.destination)).pixel
      })

    // Retreat to help
    if ( ! unit.visibleToOpponents) {
      unit.agent.toReturn = Some(destination)
    }

    // Travel to team
    if (unit.isAny(Terran.Medic, Protoss.Arbiter, Protoss.DarkArchon, Protoss.HighTemplar, Zerg.Defiler, Zerg.Queen)) {
      unit.agent.toTravel = Some(destination)
    }
  }
}
