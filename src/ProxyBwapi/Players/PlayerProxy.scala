package ProxyBwapi.Players

import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClass.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade
import bwapi.{Player, Race, Unit}

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class PlayerProxy(base:Player) {
  
  lazy val id             : Int       = base.getID
  lazy val name           : String    = base.getName
  lazy val raceInitial    : Race      = base.getRace
  lazy val isUs           : Boolean   = this == With.self
  lazy val isNeutral      : Boolean   = base.isNeutral
  lazy val isAlly         : Boolean   = base.isAlly(With.game.self)
  lazy val isEnemy        : Boolean   = base.isEnemy(With.game.self)
  lazy val startTile      : Tile      = new Tile(base.getStartLocation)
  lazy val townHallClass  : UnitClass = UnitClasses.get(raceInitial.getCenter)
  lazy val gasClass       : UnitClass = UnitClasses.get(raceInitial.getRefinery)
  lazy val supplyClass    : UnitClass = UnitClasses.get(raceInitial.getSupplyProvider)
  lazy val transportClass : UnitClass = UnitClasses.get(raceInitial.getTransport)
  lazy val workerClass    : UnitClass = UnitClasses.get(raceInitial.getWorker)
  
  def gas               : Int = gasCache()
  def minerals          : Int = mineralsCache()
  def gatheredGas       : Int = gatheredGasCache()
  def gatheredMinerals  : Int = gatheredMineralsCache()
  def supplyUsed        : Int = supplyUsedCache()
  def supplyTotal       : Int = supplyTotalCache()
  
  def rawUnits: mutable.Buffer[Unit] = unitsCache()
  
  def getUpgradeLevel(upgrade: Upgrade):Int = {
    //Further optimization: Stop expiring when at max level
    if ( ! upgradeLevelCaches.contains(upgrade)) {
      upgradeLevelCaches.put(upgrade, new Cache(() => base.getUpgradeLevel(upgrade.baseType)))
    }
    upgradeLevelCaches(upgrade)()
  }
  
  def hasTech(tech: Tech):Boolean = {
    //Further optimization: Stop expiring when researched
    if ( ! techsResearchedCaches.contains(tech)) {
      techsResearchedCaches.put(tech, new Cache(() => base.hasResearched(tech.baseType)))
    }
    techsResearchedCaches(tech)()
  }
  
  private val gasCache                = new Cache(() => base.gas)
  private val mineralsCache           = new Cache(() => base.minerals)
  private val gatheredGasCache        = new Cache(() => base.gatheredGas)
  private val gatheredMineralsCache   = new Cache(() => base.gatheredMinerals)
  private val supplyUsedCache         = new Cache(() => base.supplyUsed)
  private val supplyTotalCache        = new Cache(() => base.supplyTotal)
  private val unitsCache              = new Cache(() => base.getUnits.asScala)
  private val upgradeLevelCaches      = new mutable.HashMap[Upgrade, Cache[Int]]
  private val techsResearchedCaches   = new mutable.HashMap[Tech, Cache[Boolean]]
  
  
}
