package Micro.Squads.Companies
import Micro.Squads.Squad
import ProxyBwapi.UnitClass.UnitClass

class Detectors(squad: Squad) extends Company {
  
  override def allowed(unitClass: UnitClass): Boolean = unitClass.isDetector && unitClass.canMove
  
}
