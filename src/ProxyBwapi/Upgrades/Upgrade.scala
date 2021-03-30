package ProxyBwapi.Upgrades

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
  val asString             = bwapiType.toString.replaceAll("_", " ")

  val gasPrice             = levels.map(i => (i, bwapiType.gasPrice(i))).toMap
  val upgradeFrames        = levels.map(i => (i, bwapiType.upgradeTime(i))).toMap
  lazy val whatsRequired        = levels.map(i => (i, UnitClasses.get(bwapiType.whatsRequired(i)))).toMap
  
  override def toString:String = asString
}
