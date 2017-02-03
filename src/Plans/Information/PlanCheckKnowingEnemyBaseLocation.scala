package Plans.Information

import Plans.Plan
import Startup.With

class PlanCheckKnowingEnemyBaseLocation extends Plan {
  override def isComplete(): Boolean = {
    With.scout.enemyBaseLocationPosition.isDefined
  }
}
