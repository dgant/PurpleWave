package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClasses.UnitClass

class Siege(squad: Squad) extends Company {
  
  val classes = Vector(
    Terran.SiegeTankSieged,
    Terran.SiegeTankUnsieged,
    Terran.Battlecruiser,
    Protoss.Reaver,
    Protoss.Carrier,
    Zerg.Guardian
  )
  
  override def allowed(unitClass: UnitClass): Boolean = classes.contains(unitClass)
  
}
