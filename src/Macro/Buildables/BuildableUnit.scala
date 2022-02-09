package Macro.Buildables

import ProxyBwapi.UnitClasses._

case class BuildableUnit(unit: UnitClass) extends Buildable {
  override def unitOption       : Option[UnitClass]   = Some(unit)
  override def toString         : String              = unit.toString
  override def frames           : Int                 = unit.buildFrames
}
