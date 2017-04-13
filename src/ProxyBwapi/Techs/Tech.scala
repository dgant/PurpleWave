package ProxyBwapi.Techs

import ProxyBwapi.UnitClass.UnitClasses
import bwapi.TechType

case class Tech(val baseType:TechType) {
  
  lazy val energyCost       = baseType.energyCost
  lazy val getOrder         = baseType.getOrder
  lazy val gasPrice         = baseType.gasPrice
  lazy val getRace          = baseType.getRace
  lazy val getWeapon        = baseType.getWeapon
  lazy val mineralPrice     = baseType.mineralPrice
  lazy val researchTime     = baseType.researchTime
  lazy val requiredUnit     = UnitClasses.get(baseType.requiredUnit)
  lazy val targetsPixel     = baseType.targetsPosition
  lazy val targetsUnits     = baseType.targetsUnit
  lazy val whatResearches   = UnitClasses.get(baseType.whatResearches)
  lazy val asString         = baseType.toString.replaceAll("_", " ")
  
  override def toString:String = asString
}
