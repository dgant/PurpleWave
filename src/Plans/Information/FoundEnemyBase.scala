package Plans.Information

import Plans.Plan
import Startup.With

class FoundEnemyBase extends Plan {
  
  description.set("Do we know where an enemy base is?")
  
  override def isComplete: Boolean = With.intelligence.enemyBases.isDefined
}
