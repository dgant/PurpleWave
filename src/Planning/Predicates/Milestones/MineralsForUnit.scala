package Planning.Predicates.Milestones

import Planning.Plans.Compound.Or
import Planning.Predicates.Economy.MineralsAtLeast
import ProxyBwapi.UnitClasses.UnitClass

class MineralsForUnit(unitClass: UnitClass, quantity: Int = 1) extends Or(
  new MineralsAtLeast(unitClass.mineralPrice),
  new UnitsAtLeast(quantity, unitClass))
