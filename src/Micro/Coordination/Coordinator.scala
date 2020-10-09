package Micro.Coordination

import Micro.Coordination.Pathing.{GridPathOccupancy, GridsPush}

class Coordinator {
  
  val explosions = new ExplosionTracker
  val gridPathOccupancy = new GridPathOccupancy
  val ridesharing = new Ridesharing
  val gridsPush = new GridsPush

  def onAgentCycle(): Unit = {
    explosions.onAgentCycle()
    ridesharing.onAgentCycle()
    gridPathOccupancy.update()
    gridsPush.onAgentCycle()
  }
}
