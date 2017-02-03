package Plans.Information

import Plans.Generic.Compound.PlanFulfillRequirements

class PlanRequireEnemyBaseLocation extends PlanFulfillRequirements {
  requirement = new PlanKnowEnemyBaseLocation
  fulfiller = new PlanDiscoverEnemyBaseLocation
}
