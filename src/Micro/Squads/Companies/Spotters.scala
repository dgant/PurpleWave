package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.UnitClass.UnitClass

class Spotters(squad: Squad) extends Company {
  
  override def allowed(unitClass: UnitClass): Boolean = unitClass.isFlyer || unitClass.isFlyingBuilding
  
}
