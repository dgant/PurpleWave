package ProxyBwapi.Upgrades

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.UpgradeType

case class Upgrade(val base:UpgradeType) {

  def levels              = new CacheForever(() => (1 to base.maxRepeats).toList).get
  def getRace             = new CacheForever(() => base.getRace).get
  def mineralPrice        = new CacheForever(() => levels.map(i => (i, base.mineralPrice(i))).toMap).get
  def mineralPriceFactor  = new CacheForever(() => base.mineralPriceFactor).get
  def gasPrice            = new CacheForever(() => levels.map(i => (i, base.gasPrice(i))).toMap).get
  def gasPriceFactor      = new CacheForever(() => base.gasPriceFactor).get
  def upgradeTime         = new CacheForever(() => levels.map(i => (i, base.upgradeTime(i))).toMap).get
  def upgradeTimeFactor   = new CacheForever(() => base.upgradeTimeFactor).get
  def whatsRequired       = new CacheForever(() => levels.map(i => (i, UnitClasses.get(base.whatsRequired(i)))).toMap).get
  def whatUpgrades        = new CacheForever(() => UnitClasses.get(base.whatUpgrades)).get
  
  override def toString:String = base.toString.replaceAll("_", " ")
}
