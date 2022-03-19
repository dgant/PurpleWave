package ProxyBwapi.Techs

import ProxyBwapi.BuildableType
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import bwapi.{Order, Race, TechType, WeaponType}

case class Tech(bwapiTech: TechType) extends BuildableType{
  val id                  : Int         = bwapiTech.id
  val energyCost          : Int         = bwapiTech.energyCost
  val getOrder            : Order       = bwapiTech.getOrder
  val gasPrice            : Int         = bwapiTech.gasPrice
  val race                : Race        = bwapiTech.getRace
  val getWeapon           : WeaponType  = bwapiTech.getWeapon
  val mineralPrice        : Int         = bwapiTech.mineralPrice
  val researchFrames      : Int         = bwapiTech.researchTime
  lazy val requiredUnit   : UnitClass   = UnitClasses.get(bwapiTech.requiredUnit)
  val targetsPixel        : Boolean     = bwapiTech.targetsPosition
  val targetsUnits        : Boolean     = bwapiTech.targetsUnit
  lazy val whatResearches : UnitClass   = UnitClasses.get(bwapiTech.whatResearches)

  def apply(player: PlayerInfo): Boolean = player.hasTech(this)
  
  override val toString: String = bwapiTech.toString.replaceAll("_", " ")

  override def productionFrames(quantity: Int)  : Int = researchFrames
  override def mineralCost(quantity: Int)       : Int = mineralPrice
  override def gasCost(quantity: Int)           : Int = gasPrice
  override def supplyProvided                   : Int = 0
  override def supplyRequired                   : Int = 0
}
