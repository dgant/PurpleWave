package Macro.Buildables

import ProxyBwapi.UnitClasses._

case class BuildableUnit(val unit: UnitClass) extends Buildable {
  
  override def unitOption       : Option[UnitClass]   = Some(unit)
  override def unitsProduced    : Int                 = if (unit.isTwoUnitsInOneEgg) 2 else 1
  override def toString         : String              = unit.toString
  override def minerals         : Int                 = unit.mineralPrice
  override def gas              : Int                 = unit.gasPrice
  override def supplyRequired   : Int                 = unit.supplyRequired
  override def supplyProvided   : Int                 = unit.supplyProvided
  override def frames           : Int                 = unit.buildFrames
  
  override lazy val buildersOccupied: Iterable[BuildableUnit] = {
    unit.buildUnitsBorrowed.map(BuildableUnit)
  }
  
  override lazy val buildersConsumed: Iterable[BuildableUnit] = {
    unit.buildUnitsSpent.map(BuildableUnit)
  }
}
