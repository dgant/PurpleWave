package Planning.Plans.Information

import Planning.Plan
import Startup.With

class FoundEnemyBase extends Plan {
  
  description.set("Do we know where an enemy base is?")
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty ||
    //TODO -- once we understand implicit base positions, remove this
    With.units.enemy.exists(e => e.unitClass.isBuilding && e.possiblyStillThere)
}
