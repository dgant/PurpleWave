package Planning.Plans.Information

import Planning.Plan
import Startup.With

class FoundEnemyBase extends Plan {
  
  description.set("Do we know where an enemy base is?")
  
  override def isComplete: Boolean = With.intelligence.enemyBases.isDefined || With.units.enemy.exists(e => e.utype.isBuilding && e.possiblyStillThere)
}
