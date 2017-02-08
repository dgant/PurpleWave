package Plans.Generic.Army

import Plans.Generic.Allocation.LockUnitsGreedily
import Plans.Generic.Compound.AllSerial
import Plans.Information.RequireEnemyBaseLocation
import Strategies.UnitMatchers.UnitMatchWarriors

class DestroyEconomy extends AllSerial {
  
  var _fighters = new LockUnitsGreedily {
    unitMatcher.set(new UnitMatchWarriors)
  }
  
  children.set(List(
    new AllSerial { children.set(List(
      new RequireEnemyBaseLocation,
      _fighters
    )) },
    new DestroyEconomyFulfiller {
      fighters.set(_fighters)
    }
  ))
}
