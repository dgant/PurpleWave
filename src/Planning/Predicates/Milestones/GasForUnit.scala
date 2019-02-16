package Planning.Predicates.Milestones

import Planning.Plans.Compound.Or
import Planning.Predicates.Economy.GasAtLeast
import ProxyBwapi.UnitClasses.UnitClass

class GasForUnit(unitClass: UnitClass, quantity: Int = 1) extends Or(
  new GasAtLeast(unitClass.gasValue),
  new UnitsAtLeast(quantity, unitClass))
