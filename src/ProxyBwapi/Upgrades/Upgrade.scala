package ProxyBwapi.Upgrades

import Lifecycle.With
import ProxyBwapi.BuildableType
import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import bwapi.{Race, UpgradeType}

case class Upgrade(bwapiType: UpgradeType) extends BuildableType {
  val id                  : Int                 = bwapiType.id
  val levels              : Vector[Int]         = (1 to bwapiType.maxRepeats).toVector
  val race                : Race                = bwapiType.getRace
  val mineralPriceFactor  : Int                 = bwapiType.mineralPriceFactor
  val gasPriceFactor      : Int                 = bwapiType.gasPriceFactor
  val upgradeTimeFactor   : Int                 = bwapiType.upgradeTimeFactor
  val mineralPrice        : Map[Int, Int]       = levels.map(i => (i, bwapiType.mineralPrice(i))).toMap
  lazy val whatUpgrades   : UnitClass           = UnitClasses.get(bwapiType.whatUpgrades)
  val gasPrice            : Map[Int, Int]       = levels.map(i => (i, bwapiType.gasPrice(i))).toMap
  val upgradeFrames       : Map[Int, Int]       = levels.map(i => (i, bwapiType.upgradeTime(i))).toMap
  lazy val whatsRequired  : Map[Int, UnitClass] = levels.map(i => (i, UnitClasses.get(bwapiType.whatsRequired(i)))).toMap

  override def productionFrames(quantity: Int)  : Int = upgradeFrames.getOrElse(quantity, 0)
  override def mineralCost(quantity: Int)       : Int = mineralPrice.getOrElse(quantity, 0)
  override def gasCost(quantity: Int)           : Int = gasPrice.getOrElse(quantity, 0)
  override def supplyProvided                   : Int = 0
  override def supplyRequired                   : Int = 0

  def apply(): Boolean = apply(1)
  def apply(level: Int): Boolean = apply(With.self, level)
  def apply(player: PlayerInfo): Boolean = apply(player, 1)
  def apply(player: PlayerInfo, level: Int): Boolean = player.getUpgradeLevel(this) >= level

  override val toString: String =  bwapiType.toString.replaceAll("_", " ")
}
