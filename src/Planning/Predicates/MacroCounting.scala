package Planning.Predicates

import Information.Fingerprinting.Fingerprint
import Information.Geography.Types.Base
import Lifecycle.With
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchOr, UnitMatcher}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade
import Strategery.StarCraftMap
import Strategery.Strategies.Strategy
import bwapi.Race

/**
  * Standardized ways of counting things for use in macro plans
  */
trait MacroCounting {

  def frame: Int = With.frame
  def gas: Int = With.self.gas
  def minerals: Int = With.self.minerals
  def supplyUsed200: Int = (With.self.supplyUsed + 1) / 2
  def supplyTotal200: Int = With.self.supplyTotal / 2
  def supplyBlocked: Boolean = With.self.supplyUsed >= With.self.supplyTotal

  def bases: Int = With.geography.ourBases.size

  def isMiningBase(base: Base) = base.minerals.size >= 5 && base.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut
  def miningBases: Int = With.geography.ourBases.view.filter(_.townHall.isDefined).count(isMiningBase)
  def mineralOnlyBase: Boolean = With.geography.ourBases.exists(base => base.gas.isEmpty && base.mineralsLeft > With.configuration.minimumMineralsBeforeMinedOut)
  def gasPumps: Int = With.geography.ourBases.map(_.gas.view.filter(_.isOurs).count(_.gasLeft > 300)).sum

  def techStarted(tech: Tech): Boolean = {
    techComplete(tech, tech.researchFrames)
  }

  def techComplete(tech: Tech, withinFrames: Int = 0): Boolean = {
    With.self.hasTech(tech) || (withinFrames >= 0 && With.units.ours.exists(unit =>
      unit.teching && unit.techingType == tech && unit.remainingTechFrames <= withinFrames))
  }

  def upgradeStarted(upgrade: Upgrade, level: Int = 1): Boolean = {
    upgradeComplete(upgrade, level, upgrade.upgradeFrames(level))
  }

  def upgradeComplete(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0): Boolean = {
    With.self.getUpgradeLevel(upgrade) >= level  || (
      With.self.getUpgradeLevel(upgrade) == level - 1
      && withinFrames > 0
      && With.units.ours.exists(unit =>
        unit.upgrading
        && unit.upgradingType == upgrade
        && unit.remainingUpgradeFrames <= withinFrames))
  }

  def units(matchers: UnitMatcher*): Int = (
    With.units.countOurs(MatchOr(matchers: _*)) +
      (if (With.self.isZerg) With.units.ours.map(u => if (matchers.contains(u.buildType)) u.buildType.copiesProduced else 0).sum else 0)
  )

  def unitsComplete(matchers: UnitMatcher*): Int = {
    With.units.countOurs(MatchAnd(MatchComplete, MatchOr(matchers: _*)))
  }

  def framesUntilUnit(unitClass: UnitClass): Int = {
    With.projections.unit(unitClass)
  }

  def framesUntilTech(tech: Tech): Int = {
    With.projections.tech(tech)
  }

  def framesUntilUpgrade(upgrade: Upgrade, level: Int = 1): Int = {
    With.projections.upgrade(upgrade, level)
  }

  def haveGasForTech(tech: Tech): Boolean = {
    gas >= tech.gasPrice || techComplete(tech)
  }

  def haveGasForUpgrade(upgrade: Upgrade, level: Int): Boolean = {
    gas >= upgrade.gasPrice(level) || upgradeComplete(upgrade, level)
  }

  def haveGasForUnit(unitClass: UnitClass, quantity: Int): Boolean = {
    gas >= quantity * unitClass.gasPrice - units(unitClass)
  }

  def haveMineralsForUnit(unitClass: UnitClass, quantity: Int): Boolean = {
    minerals >= quantity * unitClass.mineralPrice - units(unitClass)
  }

  def safeAtHome: Boolean = {
    safeToMoveOut || With.battles.global.globalSafeToDefend
  }

  def safeToMoveOut: Boolean = {
    With.blackboard.safeToMoveOut()
  }

  def gasCapsUntouched: Boolean = (
    ! With.blackboard.gasWorkerCeiling.isSet
    && ! With.blackboard.gasWorkerFloor.isSet
    && ! With.blackboard.gasLimitFloor.isSet
    && ! With.blackboard.gasLimitCeiling.isSet
    && ! With.blackboard.gasWorkerRatio.isSet
  )

  def employing(strategies: Strategy*): Boolean = {
    strategies.exists(_.activate())
  }

  def onMap(map: StarCraftMap*): Boolean = map.exists(_.matches)

  def starts: Int = With.geography.startLocations.size

  def enemies(matchers: UnitMatcher*): Int = {
    With.units.countEnemy(MatchOr(matchers: _*))
  }

  def enemiesComplete(matchers: UnitMatcher*): Int = {
    With.units.countEnemy(MatchAnd(MatchComplete, MatchOr(matchers: _*)))
  }

  def enemiesShown(unitClasses: UnitClass*): Int = {
    unitClasses.view.map(With.unitsShown.allEnemies(_)).sum
  }

  def enemyHasShown(unitClasses: UnitClass*): Boolean = {
    unitClasses.exists(enemiesShown(_) > 0)
  }

  def enemyHasTech(techs: Tech*): Boolean = {
    techs.exists(t => With.enemies.exists(_.hasTech(t)))
  }

  def enemyHasUpgrade(upgrade: Upgrade, level: Int = 1): Boolean = {
    With.enemies.exists(_.getUpgradeLevel(upgrade) >= level)
  }

  def enemyShownCloakedThreat: Boolean = (
    With.enemy.hasTech(Terran.WraithCloak) || enemyHasShown(
      Terran.SpiderMine,
      Protoss.Arbiter,
      Protoss.ArbiterTribunal,
      Protoss.DarkTemplar,
      Protoss.TemplarArchives,
      Zerg.LurkerEgg,
      Zerg.Lurker))

  def enemyDarkTemplarLikely: Boolean = (
    With.fingerprints.dtRush.matches
    || enemyHasUpgrade(Protoss.ZealotSpeed)
    || enemiesShown(Protoss.HighTemplar, Protoss.Archon, Protoss.DarkArchon, Protoss.TemplarArchives, Protoss.ArbiterTribunal, Protoss.Arbiter) > 0
  )

  def enemyRobo: Boolean = {
    enemyHasShown(Protoss.RoboticsFacility, Protoss.RoboticsSupportBay, Protoss.Observatory, Protoss.Shuttle, Protoss.Reaver, Protoss.Observer)
  }

  def enemyLurkersLikely: Boolean = {
    enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg) || (enemyHasShown(Zerg.Hydralisk, Zerg.HydraliskDen) && enemyHasShown(Zerg.Lair, Zerg.Hive, Zerg.Spire, Zerg.Mutalisk))
  }

  def enemyMutalisksLikely: Boolean = {
    enemyHasShown(Zerg.Mutalisk, Zerg.Spire) || enemyHasShown(Zerg.Lair) && ! enemyHasShown(Zerg.Hydralisk, Zerg.HydraliskDen, Zerg.Lurker, Zerg.LurkerEgg)
  }

  def enemyCarriersLikely: Boolean = {
    enemyHasShown(Protoss.Carrier, Protoss.Interceptor, Protoss.FleetBeacon) || enemyHasUpgrade(Protoss.CarrierCapacity)
  }

  def enemyWalledIn: Boolean = {
    With.geography.zones.exists(z => z.walledIn && ! z.owner.isUs)
  }

  def scoutCleared: Boolean = {
    With.scouting.enemyScouts().isEmpty || (
      With.scouting.enemyScouts().forall( ! _.likelyStillThere)
      && With.scouting.basesToLookForEnemyScouts().forall(_.zone.tiles.forall(_.explored)))
  }

  def enemyBases: Int = With.geography.enemyBases.size
  def enemyMiningBases: Int = With.geography.enemyBases.count(isMiningBase)
  def foundEnemyBase: Boolean = enemyBases > 0
  def enemyNaturalConfirmed: Boolean = With.geography.enemyBases.exists(b => b.isNaturalOf.isDefined && b.townHall.isDefined)

  def enemyIs(race: Race): Boolean = With.enemies.exists(_.raceCurrent == race)
  def enemyIsTerran: Boolean = enemyIs(Race.Terran)
  def enemyIsProtoss: Boolean = enemyIs(Race.Protoss)
  def enemyIsZerg: Boolean = enemyIs(Race.Zerg)
  def enemyIsRandom: Boolean = enemyIs(Race.Unknown)
  def enemyRaceKnown: Boolean = enemyIsTerran || enemyIsProtoss || enemyIsZerg

  def enemyStrategy(fingerprints: Fingerprint*): Boolean = {
    fingerprints.exists(_.matches)
  }

  def enemyRecentStrategy(fingerprints: Fingerprint*): Boolean = {
    enemyStrategy(fingerprints: _*) || fingerprints.map(_.toString).exists(With.strategy.enemyRecentFingerprints.contains)
  }

  def trackRecordLacks(fingerprints: Fingerprint*): Boolean = {
    gamesAgainst >= With.configuration.recentFingerprints && ! enemyRecentStrategy(fingerprints: _*)
  }

  def gamesAgainst: Int = With.history.gamesVsEnemies.size
}