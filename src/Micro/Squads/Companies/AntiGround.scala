package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.UnitClasses.UnitClass

class AntiGround(squad: Squad) extends Company {
  
  override def allowed(unitClass: UnitClass): Boolean = unitClass.attacksGround
  
}
