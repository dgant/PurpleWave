package ProxyBwapi.Players

import Lifecycle.With
import Performance.Caching.CacheFrame
import ProxyBwapi.Techs.Tech
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Player, Race}

import scala.collection.mutable
import scala.collection.JavaConverters._

abstract class PlayerProxy(base:Player) {
  
  lazy val  id          : Int     = base.getID
  lazy val  name        : String  = base.getName
  lazy val  race        : Race    = base.getRace
  lazy val  isUs        : Boolean = this == With.self
  lazy val  isNeutral   : Boolean = base.isNeutral
  lazy val  isAlly      : Boolean = base.isAlly(With.game.self)
  lazy val  isEnemy     : Boolean = base.isEnemy(With.game.self)
  lazy val  supplyUsed  : Int     = base.supplyUsed
  lazy val  supplyTotal : Int     = base.supplyTotal
  
  def gas               = gasCache.get
  def minerals          = mineralsCache.get
  def gatheredGas       = gatheredGasCache.get
  def gatheredMinerals  = gatheredMineralsCache.get
  def rawUnits          = unitsCache.get
  
  def getUpgradeLevel(upgrade: Upgrade):Int = {
    //Further optimization: Stop expiring when at max level
    if ( ! upgradeLevelCaches.contains(upgrade)) {
      upgradeLevelCaches.put(upgrade, new CacheFrame(() => base.getUpgradeLevel(upgrade.baseType)))
    }
    upgradeLevelCaches(upgrade).get
  }
  
  def hasResearched(tech: Tech):Boolean = {
    //Further optimization: Stop expiring when researched
    if ( ! techsResearchedCaches.contains(tech)) {
      techsResearchedCaches.put(tech, new CacheFrame(() => base.hasResearched(tech.baseType)))
    }
    techsResearchedCaches(tech).get
  }
  
  
  
  private val gasCache                = new CacheFrame(() => base.gas)
  private val mineralsCache           = new CacheFrame(() => base.minerals)
  private val gatheredGasCache        = new CacheFrame(() => base.gatheredGas)
  private val gatheredMineralsCache   = new CacheFrame(() => base.gatheredMinerals)
  private val unitsCache              = new CacheFrame(() => base.getUnits.asScala)
  private val upgradeLevelCaches      = new mutable.HashMap[Upgrade, CacheFrame[Int]]
  private val techsResearchedCaches   = new mutable.HashMap[Tech, CacheFrame[Boolean]]
  
  
}
