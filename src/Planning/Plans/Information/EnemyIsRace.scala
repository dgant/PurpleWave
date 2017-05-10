package Planning.Plans.Information

import Lifecycle.With
import Planning.Plan
import bwapi.Race

class EnemyIsRace(val race:Race) extends Plan {
  override def isComplete: Boolean = With.enemies.exists(_.race == Race.Terran)
}

class EnemyIsTerran   extends EnemyIsRace(Race.Terran)
class EnemyIsProtoss  extends EnemyIsRace(Race.Protoss)
class EnemyIsZerg     extends EnemyIsRace(Race.Zerg)
class EnemyIsRandom   extends EnemyIsRace(Race.Unknown)