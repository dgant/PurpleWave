package Plans.Information

import Plans.Plan
import Startup.With

class KnowEnemyBaseLocationChecker extends Plan {
  
  description.set(Some("Do we know where an enemy base is?"))
  
  override def isComplete(): Boolean = {
    With.intelligence.mostBaselikeEnemyBuilding.isDefined
  }
}
