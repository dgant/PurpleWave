package Micro.Coordination

import Lifecycle.With
import Micro.Coordination.Pathing.GridPathOccupancy
import Micro.Coordination.Pushing.Pushes

class Coordinator {

  val damageTracker = new DamageTracker
  val gridPathOccupancy = new GridPathOccupancy
  val pushes = new Pushes

  def onAgentCycle(): Unit = {
    damageTracker.update()
    gridPathOccupancy.update()
    pushes.onAgentCycle()
    With.units.playerOwned.foreach(_.resetTargeting())
  }
}
