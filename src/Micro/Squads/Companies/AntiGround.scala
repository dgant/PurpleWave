package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.UnitClass.UnitClass

class AntiGround(squad: Squad) extends Company {
  
  override def allowed(unitClass: UnitClass): Boolean = unitClass.attacksGround
  
}
