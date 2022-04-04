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
  lazy val startTile      : Tile      = new Tile(base.getStartLocation)
  lazy val townHallClass  : UnitClass = UnitClasses.get(raceInitial.getResourceDepot)
  lazy val gasClass       : UnitClass = UnitClasses.get(raceInitial.getRefinery)
  lazy val supplyClass    : UnitClass = UnitClasses.get(raceInitial.getSupplyProvider)
  lazy val transportClass : UnitClass = UnitClasses.get(raceInitial.getTransport)
  lazy val workerClass    : UnitClass = UnitClasses.get(raceInitial.getWorker)

  def isNeutral   : Boolean   = base.isNeutral
  def isAlly      : Boolean   = base.isAlly(With.game.self) && ! isUs
  def isEnemy     : Boolean   = base.isEnemy(With.game.self)
  def isDefeated  : Boolean   = base.isDefeated || base.leftGame

  def gas               : Int = base.gas
  def minerals          : Int = base.minerals
  def gatheredGas       : Int = base.gatheredGas
  def gatheredMinerals  : Int = base.gatheredMinerals
  def supplyUsed400     : Int = supplyUsedCache()
  def supplyTotal400    : Int = supplyTotalCache()
  
  def rawUnits: Vector[Unit] = unitsCache()
  private val unitsCache = new Cache(() => base.getUnits.asScala.toVector)

  private val upgradeLevelCaches = new mutable.HashMap[Upgrade, Cache[Int]]
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
    val reportedLevel = base.getUpgradeLevel(upgrade.bwapiType)
    val previousLevel = maxUpgradeLevels(upgrade)
    val currentLevel  = Math.max(reportedLevel, previousLevel)
    maxUpgradeLevels(upgrade) = currentLevel
    currentLevel
  }

  private val techsResearchedCaches = new mutable.HashMap[Tech, Cache[Boolean]]
  def hasTech(tech: Tech):Boolean = {
    // Further optimization: Stop expiring when researched
    if ( ! techsResearchedCaches.contains(tech)) {
      techsResearchedCaches.put(tech, new Cache(() => base.hasResearched(tech.bwapiTech)))
    }
    techsResearchedCaches(tech)()
  }

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
}
