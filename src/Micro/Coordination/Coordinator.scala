package Micro.Coordination

import Micro.Coordination.Pathing.GridPathOccupancy

class Coordinator {
  
  val explosions = new ExplosionTracker
  val gridPathOccupancy = new GridPathOccupancy
  val ridesharing = new Ridesharing

  def runPerAgentCycle(): Unit = {
    gridPathOccupancy.update()
    ridesharing.run()
  }

  def runPerTask() {
    explosions.run()
  }
}
