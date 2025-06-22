package Macro.Scheduling

import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass

object AddonSubstitution {
  def apply(unitClass: UnitClass): Boolean = unitClass == Terran.MachineShop ||  unitClass == Terran.ControlTower
  def fromReal(unitClass: UnitClass): UnitClass = {
    if      (unitClass == Terran.MachineShop)   Terran.FakeMachineShop
    else if (unitClass == Terran.ControlTower)  Terran.FakeControlTower
    else                                        unitClass
  }
  def toReal(unitClass: UnitClass): UnitClass = {
    if      (unitClass == Terran.FakeMachineShop)   Terran.MachineShop
    else if (unitClass == Terran.FakeControlTower)  Terran.ControlTower
    else                                            unitClass
  }
}
