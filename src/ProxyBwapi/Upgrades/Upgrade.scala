package ProxyBwapi.Upgrades

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.UpgradeType

case class Upgrade(val baseType:UpgradeType) {

  private val levelsCache              = new CacheForever(() => (1 to baseType.maxRepeats).toList)
  private val getRaceCache             = new CacheForever(() => baseType.getRace)
  private val mineralPriceCache        = new CacheForever(() => levels.map(i => (i, baseType.mineralPrice(i))).toMap)
  private val mineralPriceFactorCache  = new CacheForever(() => baseType.mineralPriceFactor)
  private val gasPriceCache            = new CacheForever(() => levels.map(i => (i, baseType.gasPrice(i))).toMap)
  private val gasPriceFactorCache      = new CacheForever(() => baseType.gasPriceFactor)
  private val upgradeTimeCache         = new CacheForever(() => levels.map(i => (i, baseType.upgradeTime(i))).toMap)
  private val upgradeTimeFactorCache   = new CacheForever(() => baseType.upgradeTimeFactor)
  private val whatsRequiredCache       = new CacheForever(() => levels.map(i => (i, UnitClasses.get(baseType.whatsRequired(i)))).toMap)
  private val whatUpgradesCache        = new CacheForever(() => UnitClasses.get(baseType.whatUpgrades))
  private val asStringCache            = new CacheForever(() => baseType.toString.replaceAll("_", " "))
  
  def levels              = levelsCache.get
  def getRace             = getRaceCache.get
  def mineralPrice        = mineralPriceCache.get
  def mineralPriceFactor  = mineralPriceFactorCache.get
  def gasPrice            = gasPriceCache.get
  def gasPriceFactor      = gasPriceFactorCache.get
  def upgradeTime         = upgradeTimeCache.get
  def upgradeTimeFactor   = upgradeTimeFactorCache.get
  def whatsRequired       = whatsRequiredCache.get
  def whatUpgrades        = whatUpgradesCache.get
  def asString            = asStringCache.get
  
  override def toString:String = asString
}
