package Micro.Squads.Companies

import Micro.Squads.Squad
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitClass.UnitClass

class SplashGround(squad: Squad) extends Company{
  
  val classes = Vector(
    Terran.Valkyrie,
    Terran.ScienceVessel,
    Protoss.Corsair,
    Protoss.HighTemplar,
    Protoss.Archon,
    Zerg.Devourer,
    Zerg.Defiler
  )
  
  override def allowed(unitClass: UnitClass): Boolean = classes.contains(unitClass)
}
