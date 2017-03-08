package Types.Buildable

import Development.TypeDescriber
import bwapi.{TechType, UnitType}

class BuildableTech(tech: TechType) extends Buildable {
  
  override def techOption       : Option[TechType]  = Some(tech)
  override def toString         : String            = TypeDescriber.tech(tech)
  override def minerals         : Int               = tech.mineralPrice
  override def gas              : Int               = tech.gasPrice
  override def frames           : Int               = tech.researchTime
  
  override def buildersOccupied: Iterable[BuildableUnit] = {
    List(new BuildableUnit(tech.whatResearches))
  }
  override def prerequisites: Iterable[BuildableUnit] = {
    if (tech.requiredUnit != UnitType.None) {
      List(new BuildableUnit(tech.requiredUnit))
    }
    else {
      List.empty
    }
  }
}
