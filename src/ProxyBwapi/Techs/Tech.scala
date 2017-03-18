package ProxyBwapi.Techs

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.TechType

case class Tech(val baseType:TechType) {
  
  private val energyCostCache      = new CacheForever(() => baseType.energyCost)
  private val getOrderCache        = new CacheForever(() => baseType.getOrder)
  private val gasPriceCache        = new CacheForever(() => baseType.gasPrice)
  private val getRaceCache         = new CacheForever(() => baseType.getRace)
  private val getWeaponCache       = new CacheForever(() => baseType.getWeapon)
  private val mineralPriceCache    = new CacheForever(() => baseType.mineralPrice)
  private val researchTimeCache    = new CacheForever(() => baseType.researchTime)
  private val requiredUnitCache    = new CacheForever(() => UnitClasses.get(baseType.requiredUnit))
  private val targetsPositionCache = new CacheForever(() => baseType.targetsPosition)
  private val targetsUnitsCache    = new CacheForever(() => baseType.targetsUnit)
  private val whatResearchesCache  = new CacheForever(() => UnitClasses.get(baseType.whatResearches))
  private val asStringCache        = new CacheForever(() => baseType.toString.replaceAll("_", " "))
  
  def energyCost      = energyCostCache.get
  def getOrder        = getOrderCache.get
  def gasPrice        = gasPriceCache.get
  def getRace         = getRaceCache.get
  def getWeapon       = getWeaponCache.get
  def mineralPrice    = mineralPriceCache.get
  def researchTime    = researchTimeCache.get
  def requiredUnit    = requiredUnitCache.get
  def targetsPosition = targetsPositionCache.get
  def targetsUnits    = targetsUnitsCache.get
  def whatResearches  = whatResearchesCache.get
  def asString        = asStringCache.get
  
  override def toString:String = asString
}
