package Types.Buildable

import Development.TypeDescriber
import bwapi.UnitType

import scala.collection.JavaConverters._

class BuildableUnit(val unit: UnitType) extends Buildable {
  
  override def unitOption       : Option[UnitType]  = Some(unit)
  override def toString         : String            = TypeDescriber.unit(unit)
  override def minerals         : Int               = unit.mineralPrice
  override def gas              : Int               = unit.gasPrice
  override def supplyRequired   : Int               = unit.supplyRequired
  override def supplyProvided   : Int               = unit.supplyProvided
  override def frames           : Int               = unit.buildTime
  
  override def buildersOccupied : Iterable[BuildableUnit] = {
    List.fill(unit.whatBuilds.second)(unit.whatBuilds.first).map(new BuildableUnit(_))
  }
  
  override def buildersConsumed: Iterable[BuildableUnit] = {
    if (List(UnitType.Zerg_Lair, UnitType.Zerg_Hive).contains(unit)) {
      buildersOccupied
    }
    else {
      super.buildersConsumed
    }
  }
  override def prerequisites: Iterable[BuildableUnit] = {
    unit.requiredUnits.asScala.flatten(pair => List.fill(pair._2)(pair._1)).map(new BuildableUnit(_))
  }
}
