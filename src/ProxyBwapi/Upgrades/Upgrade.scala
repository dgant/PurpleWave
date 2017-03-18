package ProxyBwapi.Upgrades

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.UpgradeType

case class Upgrade(val baseType:UpgradeType) {

  def levels              = new CacheForever(() => (1 to baseType.maxRepeats).toList).get
  def getRace             = new CacheForever(() => baseType.getRace).get
  def mineralPrice        = new CacheForever(() => levels.map(i => (i, baseType.mineralPrice(i))).toMap).get
  def mineralPriceFactor  = new CacheForever(() => baseType.mineralPriceFactor).get
  def gasPrice            = new CacheForever(() => levels.map(i => (i, baseType.gasPrice(i))).toMap).get
  def gasPriceFactor      = new CacheForever(() => baseType.gasPriceFactor).get
  def upgradeTime         = new CacheForever(() => levels.map(i => (i, baseType.upgradeTime(i))).toMap).get
  def upgradeTimeFactor   = new CacheForever(() => baseType.upgradeTimeFactor).get
  def whatsRequired       = new CacheForever(() => levels.map(i => (i, UnitClasses.get(baseType.whatsRequired(i)))).toMap).get
  def whatUpgrades        = new CacheForever(() => UnitClasses.get(baseType.whatUpgrades)).get
  
  override def toString:String = baseType.toString.replaceAll("_", " ")
}
