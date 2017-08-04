package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClass.UnitClass

class Healers(squad: Squad) extends Company {
  
  override def allowed(unitClass: UnitClass): Boolean = unitClass == Terran.Medic
  
}
