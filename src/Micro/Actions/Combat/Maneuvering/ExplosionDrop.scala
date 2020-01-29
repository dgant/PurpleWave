package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ExplosionDrop extends Action {

  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.agent.hoppingExplosion
    && ! ExplosionHop.explodeysTargeting(unit)
    && ! unit.matchups.threats.exists(threat => threat.is(Terran.SpiderMine) && threat.pixelDistanceEdge(unit) < 128)
  )

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.hoppingExplosion = false
    unit.transport.foreach(transport => {
      transport.agent.releasePassenger(unit)
      With.commander.unload(transport, unit)
    })
  }
}
