package ProxyBwapi.Players

import Lifecycle.With
import Mathematics.Points.Tile
import Performance.Cache
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
import ProxyBwapi.Upgrades.Upgrade
import Utilities.CountMap

import scala.collection.JavaConverters._
import scala.collection.mutable

abstract class PlayerProxy(bwapiPlayer: bwapi.Player) {

  lazy val id             : Int         = bwapiPlayer.getID
  lazy val name           : String      = bwapiPlayer.getName
  lazy val raceInitial    : bwapi.Race  = bwapiPlayer.getRace
  lazy val isUs           : Boolean     = this == With.self
  lazy val startTile      : Tile        = new Tile(bwapiPlayer.getStartLocation)
  lazy val townHallClass  : UnitClass   = UnitClasses.get(raceInitial.getResourceDepot)
  lazy val gasClass       : UnitClass   = UnitClasses.get(raceInitial.getRefinery)
  lazy val supplyClass    : UnitClass   = UnitClasses.get(raceInitial.getSupplyProvider)
  lazy val transportClass : UnitClass   = UnitClasses.get(raceInitial.getTransport)
  lazy val workerClass    : UnitClass   = UnitClasses.get(raceInitial.getWorker)

  def isNeutral   : Boolean   = bwapiPlayer.isNeutral
  def isAlly      : Boolean   = bwapiPlayer.isAlly(With.game.self) && ! isUs
  def isEnemy     : Boolean   = bwapiPlayer.isEnemy(With.game.self)
  def isDefeated  : Boolean   = bwapiPlayer.isDefeated || bwapiPlayer.leftGame

  def gas               : Int = bwapiPlayer.gas
  def minerals          : Int = bwapiPlayer.minerals
  def gatheredGas       : Int = bwapiPlayer.gatheredGas
  def gatheredMinerals  : Int = bwapiPlayer.gatheredMinerals
  def supplyUsed400     : Int = supplyUsedCache()
  def supplyTotal400    : Int = supplyTotalCache()
  def supplyUsed200     : Int = supplyUsed400 / 2
  def supplyTotal200    : Int = supplyTotal400 / 2

  private val upgradeLevelCaches = new mutable.HashMap[Upgrade, Cache[Int]]
  private lazy val maxUpgradeLevels = new CountMap[Upgrade]
  def getUpgradeLevel(upgrade: Upgrade): Int = {
    if ( ! upgradeLevelCaches.contains(upgrade)) {
      upgradeLevelCaches.put(upgrade, new Cache(() => recalculateUpgradeLevel(upgrade)))
    }
    upgradeLevelCaches(upgrade)()
  }
  private def recalculateUpgradeLevel(upgrade: Upgrade): Int = {
    // You can only see upgrades of enemy units that are *currently visible*
    // So let's add a ratchet.
    ratchetUpgradeLevel(upgrade, bwapiPlayer.getUpgradeLevel(upgrade.bwapiType))
    maxUpgradeLevels(upgrade)
  }
  def ratchetUpgradeLevel(upgrade: Upgrade, to: Int): Unit = {
    maxUpgradeLevels.increaseTo(upgrade, to)
  }

  private val techsResearchedCaches = new mutable.HashMap[Tech, Cache[Boolean]]
  def hasTech(tech: Tech):Boolean = {
    // Further optimization: Stop expiring when researched
    if ( ! techsResearchedCaches.contains(tech)) {
      techsResearchedCaches.put(tech, new Cache(() => bwapiPlayer.hasResearched(tech.bwapiTech)))
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
