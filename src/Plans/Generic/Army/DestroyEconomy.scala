package Plans.Generic.Army

import Plans.Generic.Allocation.LockUnitsGreedily
import Plans.Generic.Compound.{AbstractPlanFulfillRequirements, AllSerial}
import Plans.Information.KnowEnemyBaseLocationChecker
import Strategies.UnitMatchers.UnitMatchWarriors

class DestroyEconomy extends AbstractPlanFulfillRequirements {
  
  var _fighters = new LockUnitsGreedily {
    unitMatcher.set(new UnitMatchWarriors)
  }
  
  checker.set(new AllSerial { children.set(List(
    new KnowEnemyBaseLocationChecker,
    _fighters
  )) })
  
  fulfiller.set(new DestroyEconomyFulfiller {
    
  })
}
