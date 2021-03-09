package ProxyBwapi.Techs

import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.TechType

case class Tech(bwapiTech: TechType) {
  lazy val id               = bwapiTech.id
  lazy val energyCost       = bwapiTech.energyCost
  lazy val getOrder         = bwapiTech.getOrder
  lazy val gasPrice         = bwapiTech.gasPrice
  lazy val getRace          = bwapiTech.getRace
  lazy val getWeapon        = bwapiTech.getWeapon
  lazy val mineralPrice     = bwapiTech.mineralPrice
  lazy val researchFrames   = bwapiTech.researchTime
  lazy val requiredUnit     = UnitClasses.get(bwapiTech.requiredUnit)
  lazy val targetsPixel     = bwapiTech.targetsPosition
  lazy val targetsUnits     = bwapiTech.targetsUnit
  lazy val whatResearches   = UnitClasses.get(bwapiTech.whatResearches)
  lazy val asString         = bwapiTech.toString.replaceAll("_", " ")
  
  override def toString: String = asString
}
