package Planning.Plans.Information

import Planning.Plan
import Startup.With
import bwapi.Race

import scala.collection.JavaConverters._

class EnemyIsRace(val race:Race) extends Plan {
  override def isComplete: Boolean = With.game.enemies.asScala.headOption.exists(_.getRace == Race.Terran)
}

class EnemyIsTerran extends EnemyIsRace(Race.Terran)
class EnemyIsProtoss extends EnemyIsRace(Race.Protoss)
class EnemyIsZerg extends EnemyIsRace(Race.Zerg)
class EnemyIsRandom extends EnemyIsRace(Race.Unknown)