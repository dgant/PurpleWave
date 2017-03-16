package ProxyBwapi.Upgrades

import ProxyBwapi.UnitClass.UnitClasses
import bwapi.UpgradeType

case class Upgrade(base:UpgradeType) {

  val levels              = (1 to base.maxRepeats).toList
  val getRace             = base.getRace
  val mineralPrice        = levels.map(i => (i, base.mineralPrice(i))).toMap
  val mineralPriceFactor  = base.mineralPriceFactor
  val gasPrice            = levels.map(i => (i, base.gasPrice(i))).toMap
  val gasPriceFactor      = base.gasPriceFactor
  val upgradeTime         = levels.map(i => (i, base.upgradeTime(i))).toMap
  val upgradeTimeFactor   = base.upgradeTimeFactor
  val whatsRequired       = levels.map(i => (i, UnitClasses.get(base.whatsRequired(i)))).toMap
  val whatUpgrades        = UnitClasses.get(base.whatUpgrades)
}
