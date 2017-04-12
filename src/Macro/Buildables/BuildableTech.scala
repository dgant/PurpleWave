package Macro.Buildables

import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClasses

case class BuildableTech(tech: Tech) extends Buildable {
  
  override def techOption       : Option[Tech]      = Some(tech)
  override def toString         : String            = tech.toString
  override def minerals         : Int               = tech.mineralPrice
  override def gas              : Int               = tech.gasPrice
  override def frames           : Int               = tech.researchTime
  
  override def buildersOccupied: Iterable[BuildableUnit] = {
    Vector(new BuildableUnit(tech.whatResearches))
  }
  override def requirements: Iterable[BuildableUnit] = {
    if (tech.requiredUnit != UnitClasses.None) {
      Vector(new BuildableUnit(tech.requiredUnit))
    }
    else {
      Vector.empty
    }
  }
}
