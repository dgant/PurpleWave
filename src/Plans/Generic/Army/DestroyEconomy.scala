package Plans.Generic.Army

import Plans.Generic.Allocation.LockUnitsGreedily
import Plans.Generic.Compound.AllSerial
import Plans.Information.RequireEnemyBaseLocation
import Strategies.UnitMatchers.UnitMatchWarriors

class DestroyEconomy extends AllSerial {
  
  val meDE = this
  var _fighters = new LockUnitsGreedily {
    unitMatcher.set(new UnitMatchWarriors)
  }
  
  children.set(List(
    new AllSerial { children.set(List(
      new RequireEnemyBaseLocation {
        this.scoutPlan.set(meDE._fighters)
      },
      _fighters
    )) },
    new DestroyEconomyFulfiller {
      fighters.set(_fighters)
    }
  ))
}
