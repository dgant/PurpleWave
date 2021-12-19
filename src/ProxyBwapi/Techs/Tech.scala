package ProxyBwapi.Techs

import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.TechType

case class Tech(bwapiTech: TechType) {
  val id                  = bwapiTech.id
  val energyCost          = bwapiTech.energyCost
  val getOrder            = bwapiTech.getOrder
  val gasPrice            = bwapiTech.gasPrice
  val getRace             = bwapiTech.getRace
  val getWeapon           = bwapiTech.getWeapon
  val mineralPrice        = bwapiTech.mineralPrice
  val researchFrames      = bwapiTech.researchTime
  lazy val requiredUnit   = UnitClasses.get(bwapiTech.requiredUnit)
  val targetsPixel        = bwapiTech.targetsPosition
  val targetsUnits        = bwapiTech.targetsUnit
  lazy val whatResearches = UnitClasses.get(bwapiTech.whatResearches)

  def apply(player: PlayerInfo): Boolean = player.hasTech(this)
  
  override val toString: String = bwapiTech.toString.replaceAll("_", " ")
}
