package Plans.Generic.Movement

import Plans.Generic.Allocation.PlanAcquireUnitsGreedily
import Plans.Generic.Compound.PlanCompleteAllInParallel
import Strategies.UnitMatchers.UnitMatchWarriors

class PlanAttackEnemyBase extends PlanCompleteAllInParallel {
  var _requireArmy = new PlanAcquireUnitsGreedily(new UnitMatchWarriors)
  
}
