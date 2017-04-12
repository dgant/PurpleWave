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
  
  override def getChildren: Iterable[Plan] = Vector(terran.get, protoss.get, zerg.get, random.get)
  
  override def update() =
    With.game.enemies.asScala.headOption.foreach(
      _.getRace match {
        case Race.Terran    => terran.get.update()
        case Race.Protoss   => protoss.get.update()
        case Race.Zerg      => zerg.get.update()
        case _              => random.get.update()
      })
}
