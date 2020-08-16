package ProxyBwapi.Players

import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.{Upgrade, Upgrades}
import bwapi.{Player, Race, Unit}

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class PlayerProxy(base: Player) {
  
  lazy val id             : Int       = base.getID
  lazy val name           : String    = base.getName
  lazy val raceInitial    : Race      = base.getRace
  lazy val isUs           : Boolean   = this == With.self
  lazy val isNeutral      : Boolean   = base.isNeutral
  lazy val isAlly         : Boolean   = base.isAlly(With.game.self) && ! isUs
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
  
  def rawUnits: Vector[Unit] = unitsCache()
  
  private lazy val maxUpgradeLevels = new mutable.HashMap[Upgrade, Int] ++ Upgrades.all.map(upgrade => (upgrade, 0))
  def getUpgradeLevel(upgrade: Upgrade): Int = {
    if ( ! upgradeLevelCaches.contains(upgrade)) {
      upgradeLevelCaches.put(upgrade, new Cache(() => recalculateUpgradeLevel(upgrade)))
    }
    upgradeLevelCaches(upgrade)()
  }
  private def recalculateUpgradeLevel(upgrade: Upgrade): Int = {
    // You can only see upgrades of enemy units that are *currently visible*
    // So let's add a ratchet.
    val reportedLevel = base.getUpgradeLevel(upgrade.baseType)
    val previousLevel = maxUpgradeLevels(upgrade)
    val currentLevel  = Math.max(reportedLevel, previousLevel)
    maxUpgradeLevels(upgrade) = currentLevel
    currentLevel
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
  private val unitsCache              = new Cache(() => base.getUnits.asScala.toVector)
  private val upgradeLevelCaches      = new mutable.HashMap[Upgrade, Cache[Int]]
  private val techsResearchedCaches   = new mutable.HashMap[Tech, Cache[Boolean]]

  private val supplyUsedCache = new Cache(() => if (!isUs) 0 else
    With.units.ours
      .view
      .withFilter(u => u.unitClass.race == raceInitial)
      .map(u => if (u.morphing) u.buildType.supplyRequired * (if (u.buildType.isTwoUnitsInOneEgg) 2 else 1) else u.unitClass.supplyRequired)
      .sum)

  private val supplyTotalCache = new Cache(() => if (!isUs) 0 else
    Math.min(
      400,
      With.units.ours
        .view
        .withFilter(u => u.complete && u.unitClass.race == raceInitial)
        .map(_.unitClass.supplyProvided)
        .sum))

  override val hashCode: Int = id
}
