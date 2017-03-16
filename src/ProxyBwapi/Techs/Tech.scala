package ProxyBwapi.Techs

import Performance.Caching.CacheForever
import ProxyBwapi.UnitClass.UnitClasses
import bwapi.TechType

case class Tech(val base:TechType) {
  def energyCost      = new CacheForever(() => base.energyCost).get
  def getOrder        = new CacheForever(() => base.getOrder).get
  def gasPrice        = new CacheForever(() => base.gasPrice).get
  def getRace         = new CacheForever(() => base.getRace).get
  def getWeapon       = new CacheForever(() => base.getWeapon).get
  def mineralPrice    = new CacheForever(() => base.mineralPrice).get
  def researchTime    = new CacheForever(() => base.researchTime).get
  def requiredUnit    = new CacheForever(() => UnitClasses.get(base.requiredUnit)).get
  def targetsPosition = new CacheForever(() => base.targetsPosition).get
  def targetsUnits    = new CacheForever(() => base.targetsUnit).get
  def whatResearches  = new CacheForever(() => UnitClasses.get(base.whatResearches)).get
}
