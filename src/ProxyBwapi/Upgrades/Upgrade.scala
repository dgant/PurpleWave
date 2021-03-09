package ProxyBwapi.Upgrades

import ProxyBwapi.UnitClasses.UnitClasses
import bwapi.UpgradeType

case class Upgrade(bwapiType: UpgradeType) {
  lazy val id                   = bwapiType.id
  lazy val levels               = (1 to bwapiType.maxRepeats).toVector
  lazy val getRace              = bwapiType.getRace
  lazy val mineralPriceFactor   = bwapiType.mineralPriceFactor
  lazy val gasPriceFactor       = bwapiType.gasPriceFactor
  lazy val upgradeTimeFactor    = bwapiType.upgradeTimeFactor
  lazy val mineralPrice         = levels.map(i => (i, bwapiType.mineralPrice(i))).toMap
  lazy val whatUpgrades         = UnitClasses.get(bwapiType.whatUpgrades)
  lazy val asString             = bwapiType.toString.replaceAll("_", " ")

  lazy val gasPrice             = levels.map(i => (i, bwapiType.gasPrice(i))).toMap
  lazy val upgradeFrames        = levels.map(i => (i, bwapiType.upgradeTime(i))).toMap
  lazy val whatsRequired        = levels.map(i => (i, UnitClasses.get(bwapiType.whatsRequired(i)))).toMap
  
  override def toString:String = asString
}
