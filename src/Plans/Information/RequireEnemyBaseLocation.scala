package Plans.Information

import Plans.Generic.Compound.AbstractPlanFulfillRequirements

class RequireEnemyBaseLocation
  extends AbstractPlanFulfillRequirements {
  
  var _checker = new PlanCheckKnowingEnemyBaseLocation
  var _fulfiller = new PlanFulfillKnowingEnemyBaseLocation
  
  override def _getChecker() = { _checker }
  override def _getFulfiller() = { _fulfiller }
}
