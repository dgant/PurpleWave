package Micro.Coordination

import Micro.Coordination.Pathing.GridPathOccupancy

class Coordinator {
  
  val explosions = new ExplosionTracker

  val gridPathOccupancy = new GridPathOccupancy
  //val gridPsionicStorm = new GridPsionicStorm

  def clear(): Unit = {
    gridPathOccupancy.update()
    //gridPsionicStorm.update()
  }

  def run() {
    explosions.run()
  }
}
