package Micro.Coordination

import Lifecycle.With
import Mathematics.Maff
import Micro.Coordination.Pathing.GridPathOccupancy
import Micro.Coordination.Pushing.Pushes
import ProxyBwapi.Orders
import ProxyBwapi.Races.Terran

class Coordinator {

  val damageTracker = new DamageTracker
  val gridPathOccupancy = new GridPathOccupancy
  val pushes = new Pushes

  def onAgentCycle(): Unit = {
    damageTracker.update()
    gridPathOccupancy.update()
    pushes.onAgentCycle()
    With.units.playerOwned.foreach(_.resetTargeting())

    With.units.playerOwned.foreach(u => { u.healers.clear(); u.presumedHealing = None })
    With.units.playerOwned.foreach(u =>
      if (u.complete
        && u.alive
        && u.isAny(Terran.SCV, Terran.Medic)
        && ! u.orderTarget.exists(_.isNeutral)
        &&   u.order != Orders.ReturnGas
        &&   u.order != Orders.ReturnMinerals
      ) {
        val organicTarget = Terran.Medic(u)
        u.presumedHealing = u.presumptiveTarget
          .filter(_.player ==  u.player)
          .orElse(
            Maff.minBy(
              u.tileArea.expand(2).tiles.flatMap(_.units).filter(t =>
                ! t.unitClass.isBuilding
                  &&    t.unitClass.orderable
                  &&    t.unitClass.isOrganic == organicTarget
                  &&    t.player == u.player))(_.pixelDistanceCenter(u)))
        u.presumedHealing.foreach(_.healers.add(u))
      })
  }
}
