package ProxyBwapi.Players

import Lifecycle.With
import Mathematics.Pixels.Tile
import Performance.Caching.CacheFrame
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Player, Race, Unit}

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class PlayerProxy(base:Player) {
  
  lazy val id             : Int       = base.getID
  lazy val name           : String    = base.getName
  lazy val race           : Race      = base.getRace
  lazy val isUs           : Boolean   = this == With.self
  lazy val isNeutral      : Boolean   = base.isNeutral
  lazy val isAlly         : Boolean   = base.isAlly(With.game.self)
  lazy val isEnemy        : Boolean   = base.isEnemy(With.game.self)
  lazy val startTile      : Tile      = new Tile(base.getStartLocation)
  lazy val townHallClass  : UnitClass = UnitClasses.get(race.getCenter)
  lazy val gasClass       : UnitClass = UnitClasses.get(race.getRefinery)
  lazy val supplyClass    : UnitClass = UnitClasses.get(race.getSupplyProvider)
  lazy val transportClass : UnitClass = UnitClasses.get(race.getTransport)
  lazy val workerClass    : UnitClass = UnitClasses.get(race.getWorker)
  
  def gas               : Int = gasCache.get
  def minerals          : Int = mineralsCache.get
  def gatheredGas       : Int = gatheredGasCache.get
  def gatheredMinerals  : Int = gatheredMineralsCache.get
  def supplyUsed        : Int = supplyUsedCache.get
  def supplyTotal       : Int = supplyTotalCache.get
  
  def rawUnits: mutable.Buffer[Unit] = unitsCache.get
  
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
  private val supplyUsedCache         = new CacheFrame(() => base.supplyUsed)
  private val supplyTotalCache        = new CacheFrame(() => base.supplyTotal)
  private val unitsCache              = new CacheFrame(() => base.getUnits.asScala)
  private val upgradeLevelCaches      = new mutable.HashMap[Upgrade, CacheFrame[Int]]
  private val techsResearchedCaches   = new mutable.HashMap[Tech, CacheFrame[Boolean]]
  
  
}
