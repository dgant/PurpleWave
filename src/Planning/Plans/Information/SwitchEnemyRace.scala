package Planning.Plans.Information

import Planning.Composition.Property
import Planning.Plan
import Lifecycle.With
import bwapi.Race

import scala.collection.JavaConverters._

class SwitchEnemyRace extends Plan {
  val terran = new Property[Plan](new Plan)
  val protoss = new Property[Plan](new Plan)
  val zerg = new Property[Plan](new Plan)
  val random = new Property[Plan](new Plan)
  
  override def getChildren: Iterable[Plan] = List(terran.get, protoss.get, zerg.get, random.get)
  
  override def onFrame() =
    With.game.enemies.asScala.headOption.foreach(
      _.getRace match {
        case Race.Terran    => terran.get.onFrame()
        case Race.Protoss   => protoss.get.onFrame()
        case Race.Zerg      => zerg.get.onFrame()
        case _              => random.get.onFrame()
      })
}
