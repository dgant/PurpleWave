package Micro.Coordination

import Micro.Coordination.Pathing.{GridPathOccupancy, Pushes}

class Coordinator {
  
  val explosions = new ExplosionTracker
  val gridPathOccupancy = new GridPathOccupancy
  val ridesharing = new Ridesharing
  val pushes = new Pushes

  def onAgentCycle(): Unit = {
    explosions.onAgentCycle()
    ridesharing.onAgentCycle()
    gridPathOccupancy.update()
    pushes.onAgentCycle()
  }
}
