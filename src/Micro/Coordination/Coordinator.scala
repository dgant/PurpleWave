package Micro.Coordination

import Micro.Coordination.Pathing.GridPathOccupancy
import Micro.Coordination.Pushing.Pushes

class Coordinator {

  val gridPathOccupancy = new GridPathOccupancy
  val ridesharing = new Ridesharing
  val pushes = new Pushes

  def onAgentCycle(): Unit = {
    ridesharing.onAgentCycle()
    gridPathOccupancy.update()
    pushes.onAgentCycle()
  }
}
