package ProxyBwapi.Techs

import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.TechType

case class Tech(bwapiTech: TechType) {
  val id               = bwapiTech.id
  val energyCost       = bwapiTech.energyCost
  val getOrder         = bwapiTech.getOrder
  val gasPrice         = bwapiTech.gasPrice
  val getRace          = bwapiTech.getRace
  val getWeapon        = bwapiTech.getWeapon
  val mineralPrice     = bwapiTech.mineralPrice
  val researchFrames   = bwapiTech.researchTime
  lazy val requiredUnit     = UnitClasses.get(bwapiTech.requiredUnit)
  val targetsPixel     = bwapiTech.targetsPosition
  val targetsUnits     = bwapiTech.targetsUnit
  lazy val whatResearches   = UnitClasses.get(bwapiTech.whatResearches)
  val asString         = bwapiTech.toString.replaceAll("_", " ")
  
  override def toString: String = asString
}
