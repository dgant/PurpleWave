package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass

class Transport(squad: Squad) extends Company {
  
  val classes = Vector(
    Terran.Dropship,
    Protoss.Shuttle,
    Zerg.Overlord
  )
  
  override def allowed(unitClass: UnitClass): Boolean = classes.contains(unitClass)
  
}
