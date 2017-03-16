package Macro.Buildables

import Debugging.TypeDescriber
import ProxyBwapi.UnitClass._

case class BuildableUnit(val unit: UnitClass) extends Buildable {
  
  override def unitOption       : Option[UnitClass]   = Some(unit)
  override def unitsProduced    : Int                 = if (unit.isTwoUnitsInOneEgg) 2 else 1
  override def toString         : String              = TypeDescriber.unit(unit)
  override def minerals         : Int                 = unit.mineralPrice
  override def gas              : Int                 = unit.gasPrice
  override def supplyRequired   : Int                 = unit.supplyRequired
  override def supplyProvided   : Int                 = unit.supplyProvided
  override def frames           : Int                 = unit.buildTime
  
  override def buildersOccupied : Iterable[BuildableUnit] = {
    List.fill(unit.whatBuilds._2)(unit.whatBuilds._1).map(new BuildableUnit(_))
  }
  override def requirements: Iterable[BuildableUnit] = {
    unit.requiredUnits.flatten(pair => List.fill(pair._2)(pair._1)).map(new BuildableUnit(_)) ++
      (if(unit.requiresPsi) List(new BuildableUnit(Protoss.Pylon)) else List.empty)
  }
}
