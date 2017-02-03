package Plans.Information

import Plans.Plan
import Startup.With

class PlanKnowEnemyBaseLocation extends Plan {
  override def isComplete(): Boolean = {
    With.scout.enemyBaseLocationPosition.isDefined
  }
}
