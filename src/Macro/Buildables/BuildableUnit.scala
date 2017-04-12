package Macro.Buildables

import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitClass._

case class BuildableUnit(val unit: UnitClass) extends Buildable {
  
  override def unitOption       : Option[UnitClass]   = Some(unit)
  override def unitsProduced    : Int                 = if (unit.isTwoUnitsInOneEgg) 2 else 1
  override def toString         : String              = unit.toString
  override def minerals         : Int                 = unit.mineralPrice
  override def gas              : Int                 = unit.gasPrice
  override def supplyRequired   : Int                 = unit.supplyRequired
  override def supplyProvided   : Int                 = unit.supplyProvided
  override def frames           : Int                 = unit.buildTime
  
  override def buildersOccupied : Iterable[BuildableUnit] = {
    Vector.fill(unit.whatBuilds._2)(unit.whatBuilds._1).map(new BuildableUnit(_))
  }
  override def requirements: Iterable[BuildableUnit] = {
    unit.requiredUnits.flatten(pair => Vector.fill(pair._2)(pair._1)).map(new BuildableUnit(_)) ++
      (if(unit.requiresPsi) Vector(new BuildableUnit(Protoss.Pylon)) else Vector.empty)
  }
}
