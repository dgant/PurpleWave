package ProxyBwapi.Upgrades

import ProxyBwapi.UnitClass.UnitClasses
import bwapi.UpgradeType

case class Upgrade(val baseType: UpgradeType) {

  lazy val levels               = (1 to baseType.maxRepeats).toVector
  lazy val getRace              = baseType.getRace
  lazy val mineralPriceFactor   = baseType.mineralPriceFactor
  lazy val gasPriceFactor       = baseType.gasPriceFactor
  lazy val upgradeTimeFactor    = baseType.upgradeTimeFactor
  lazy val mineralPrice         = levels.map(i => (i, baseType.mineralPrice(i))).toMap
  lazy val whatUpgrades         = UnitClasses.get(baseType.whatUpgrades)
  lazy val asString             = baseType.toString.replaceAll("_", " ")
  
  lazy val gasPrice             = levels.map(i => (i, baseType.gasPrice(i))).toMap
  lazy val upgradeTime          = levels.map(i => (i, baseType.upgradeTime(i))).toMap
  lazy val whatsRequired        = levels.map(i => (i, UnitClasses.get(baseType.whatsRequired(i)))).toMap
  
  override def toString:String = asString
}
