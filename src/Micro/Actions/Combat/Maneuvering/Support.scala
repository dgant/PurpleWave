package Micro.Actions.Combat.Maneuvering

import Mathematics.PurpleMath
import Micro.Actions.Action
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Support extends Action {

  def isSupport(unit: UnitInfo): Boolean = ! unit.canAttack || unit.unitClass.isWorker

  override def allowed(unit: FriendlyUnitInfo): Boolean = isSupport(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    // If useless, just go home
    //if (unit.)


    val supportables = unit.teammates.view.filterNot(isSupport)
    if (supportables.isEmpty) return
    val destination = PurpleMath.centroid(supportables.map(_.pixelCenter))


    // Retreat to help
    unit.agent.toReturn = Some(destination)

    // Travel to team
    if (unit.isAny(Terran.Medic, Protoss.Arbiter, Protoss.DarkArchon, Protoss.HighTemplar, Zerg.Defiler, Zerg.Queen)) {
      unit.agent.toTravel = Some(destination)
    }
  }
}
