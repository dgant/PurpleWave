package ProxyBwapi.Techs

import ProxyBwapi.UnitClass.UnitClasses
import bwapi.TechType

case class Tech(base:TechType) {
  val energyCost      = base.energyCost
  val getOrder        = base.getOrder
  val gasPrice        = base.gasPrice
  val getRace         = base.getRace
  val getWeapon       = base.getWeapon
  val mineralPrice    = base.mineralPrice
  val researchTime    = base.researchTime
  val requiredUnit    = UnitClasses.get(base.requiredUnit)
  val targetsPosition = base.targetsPosition
  val targetsUnits    = base.targetsUnit
  val whatResearches  = UnitClasses.get(base.whatResearches)
}
