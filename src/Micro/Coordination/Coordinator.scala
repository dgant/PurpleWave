package Micro.Coordination

import Lifecycle.With
import Micro.Coordination.Pathing.GridPathOccupancy
import Micro.Coordination.Pushing.Pushes

class Coordinator {

  val damageTracker = new DamageTracker
  val occupancy     = new GridPathOccupancy
  val pushes        = new Pushes
  val healing       = new Healing

  def onAgentCycle(): Unit = {
    damageTracker.update()
    occupancy.update()
    pushes.update()
    With.units.playerOwned.foreach(_.resetTargeting())
    healing.update()
  }
}
