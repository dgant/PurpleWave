package Macro.Buildables

import Debugging.TypeDescriber
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.UnitClasses

case class BuildableTech(tech: Tech) extends Buildable {
  
  override def techOption       : Option[Tech]      = Some(tech)
  override def toString         : String            = TypeDescriber.tech(tech)
  override def minerals         : Int               = tech.mineralPrice
  override def gas              : Int               = tech.gasPrice
  override def frames           : Int               = tech.researchTime
  
  override def buildersOccupied: Iterable[BuildableUnit] = {
    List(new BuildableUnit(tech.whatResearches))
  }
  override def requirements: Iterable[BuildableUnit] = {
    if (tech.requiredUnit != UnitClasses.None) {
      List(new BuildableUnit(tech.requiredUnit))
    }
    else {
      List.empty
    }
  }
}
