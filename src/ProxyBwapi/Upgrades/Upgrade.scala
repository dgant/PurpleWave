package ProxyBwapi.Upgrades

import ProxyBwapi.Players.PlayerInfo
import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.UpgradeType

case class Upgrade(bwapiType: UpgradeType) {
  val id                   = bwapiType.id
  val levels               = (1 to bwapiType.maxRepeats).toVector
  val getRace              = bwapiType.getRace
  val mineralPriceFactor   = bwapiType.mineralPriceFactor
  val gasPriceFactor       = bwapiType.gasPriceFactor
  val upgradeTimeFactor    = bwapiType.upgradeTimeFactor
  val mineralPrice         = levels.map(i => (i, bwapiType.mineralPrice(i))).toMap
  lazy val whatUpgrades    = UnitClasses.get(bwapiType.whatUpgrades)
  val gasPrice             = levels.map(i => (i, bwapiType.gasPrice(i))).toMap
  val upgradeFrames        = levels.map(i => (i, bwapiType.upgradeTime(i))).toMap
  lazy val whatsRequired   = levels.map(i => (i, UnitClasses.get(bwapiType.whatsRequired(i)))).toMap

  def apply(player: PlayerInfo, level: Int = 1): Boolean = player.getUpgradeLevel(this) >= level
  
  override val toString :String =  bwapiType.toString.replaceAll("_", " ")
}
