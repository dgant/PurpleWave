package ProxyBwapi.Techs

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.TechType

case class Tech(val baseType:TechType) {
  def energyCost      = new CacheForever(() => baseType.energyCost).get
  def getOrder        = new CacheForever(() => baseType.getOrder).get
  def gasPrice        = new CacheForever(() => baseType.gasPrice).get
  def getRace         = new CacheForever(() => baseType.getRace).get
  def getWeapon       = new CacheForever(() => baseType.getWeapon).get
  def mineralPrice    = new CacheForever(() => baseType.mineralPrice).get
  def researchTime    = new CacheForever(() => baseType.researchTime).get
  def requiredUnit    = new CacheForever(() => UnitClasses.get(baseType.requiredUnit)).get
  def targetsPosition = new CacheForever(() => baseType.targetsPosition).get
  def targetsUnits    = new CacheForever(() => baseType.targetsUnit).get
  def whatResearches  = new CacheForever(() => UnitClasses.get(baseType.whatResearches)).get
  
  override def toString:String = baseType.toString.replaceAll("_", " ")
}
