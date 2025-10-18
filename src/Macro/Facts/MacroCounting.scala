package Macro.Facts

import Information.Fingerprinting.Fingerprint
import Information.Geography.Types.Base
import Lifecycle.With
import Performance.Cache
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.Techs.Tech
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.Upgrades.Upgrade
import Strategery.StarCraftMap
import Strategery.Strategies.Strategy
import Utilities.?
import Utilities.Time.FrameCount
import Utilities.UnitFilters._
import bwapi.Race

/**
  * Standardized ways of counting things for use in macro plans
  */
trait MacroCounting {

  def frame: Int = With.frame
  def after   (gameTime: FrameCount): Boolean = frame > gameTime()
  def before  (gameTime: FrameCount): Boolean = frame < gameTime()

  def gas               : Int = With.self.gas
  def minerals          : Int = With.self.minerals
  def gatheredGas       : Int = With.self.gatheredGas
  def gatheredMinerals  : Int = With.self.gatheredMinerals
  def supplyUsed400     : Int = With.self.supplyUsed400
  def supplyTotal400    : Int = With.self.supplyTotal400
  def supplyUsed200     : Int = (supplyUsed400 + 1) / 2
  def supplyTotal200    : Int = supplyTotal400 / 2
  def supplyBlocked: Boolean = supplyUsed200 >= supplyTotal200
  def saturated: Boolean = units(IsWorker) >= Math.min(60, With.geography.ourBases.view.map(b => b.minerals.size* 2 + b.gas.size * 3).sum)

  private val _armySupply200 = new Cache(() => With.units.ours.filter(u => IsWarrior(u) && u.complete).map(_.unitClass.supplyRequired).sum / 2)
  def armySupply200: Int = _armySupply200()

  // Mineout threshold is  "how much we can mine from a patch in the time it takes to build a new town hall"
  // 7 patch/base * (200mins/patch = (75secs building + 20secs travel)/base * 24frames/sec * 0.044minerals/worker * 2workers/patch)
  private val minedOutThreshold = 7 * 200
  def bases: Int = With.geography.ourBases.size
  def miningBases: Int = With.geography.ourMiningBases.size
  def ourBaseTownHalls: Iterable[FriendlyUnitInfo] = With.units.ours.filter(IsTownHall).filter(u => u.base.map(_.townHallTile).exists(u.tileTopLeft.tileDistanceManhattan(_) <= 3))
  def isMiningBase(base: Base): Boolean = base.minerals.size >= 5 && base.mineralsLeft > minedOutThreshold
  def mineralOnlyBase: Boolean = With.geography.ourMiningBases.exists(_.gas.isEmpty)
  def gasPumps: Int = With.geography.ourBases.map(_.gas.view.filter(_.isOurs).count(_.gasLeft > 300)).sum

  def techStarted(tech: Tech): Boolean = {
    techComplete(tech, tech.researchFrames)
  }

  def techComplete(tech: Tech, withinFrames: Int = 0): Boolean = {
    tech() || (withinFrames >= 0 && With.units.ours.exists(u => u.teching && u.techingType == tech && u.remainingTechFrames <= withinFrames))
  }

  def anyUpgradeStarted(upgrades: Upgrade*): Boolean = {
    upgrades.exists(upgradeStarted(_, 1))
  }

  def upgradeStarted(upgrade: Upgrade, level: Int = 1): Boolean = {
    upgradeComplete(upgrade, level, upgrade.upgradeFrames.getOrElse(level, upgrade.upgradeFrames.values.last))
  }

  def upgradeComplete(upgrade: Upgrade, level: Int = 1, withinFrames: Int = 0): Boolean = {
    upgrade(level) || (
      upgrade(level - 1)
      && withinFrames > 0
      && With.units.ours.exists(unit =>
        unit.upgrading
        && unit.upgradingType == upgrade
        && unit.remainingUpgradeFrames <= withinFrames))
  }

  def units(matchers: UnitFilter*): Int = (
    With.units.countOurs(IsAny(matchers: _*)) +
      ?(
        With.self.isZerg,
        With.units.ours.map(u => ?(matchers.contains(u.buildType), u.buildType.copiesProduced, 0)).sum,
        0)
  )

  def unitsComplete(matchers: UnitFilter*): Int = {
    With.units.countOurs(IsAll(IsComplete, IsAny(matchers: _*)))
  }

  def unitsCompleteFor(frames: Int, matchers: UnitFilter*): Int = {
    With.units.countOurs(IsAll(IsCompleteFor(frames), IsAny(matchers: _*)))
  }

  def unitsEver(matchers: UnitFilter*): Int = {
    With.units.countEverOurs(_.isAny(matchers: _*))
  }

  def existsEver(matchers: UnitFilter*): Boolean = {
    matchers.exists(unitsEver(_) > 0)
  }

  def have(matchers: UnitFilter*): Boolean = {
    With.units.existsOurs(matchers: _*)
  }

  def haveComplete(matchers: UnitFilter*): Boolean = {
    With.units.existsOurs(IsAll(IsComplete, IsAny(matchers: _*)))
  }

  def haveEver(matchers: UnitFilter*): Boolean = {
    matchers.exists(With.units.existsEverOurs(_))
  }

  def haveEverComplete(matchers: UnitFilter*): Boolean = {
    matchers.exists(m => With.units.existsOurs(u => m(u) && u.complete))
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

  def haveGasForUpgrade(upgrade: Upgrade, level: Int = 1): Boolean = {
    gas >= upgrade.gasCost(level) || upgradeComplete(upgrade, level, upgrade.upgradeFrames(level))
  }

  def haveGasForUnit(unitClass: UnitClass, quantity: Int = 1): Boolean = {
    gas >= quantity * unitClass.gasPrice - units(unitClass)
  }

  def haveMineralsForUnit(unitClass: UnitClass, quantity: Int = 1): Boolean = {
    minerals >= quantity * unitClass.mineralPrice - units(unitClass)
  }

  def safeDefending: Boolean = {
    confidenceDefending11 >= 0.0
  }

  def safePushing: Boolean = {
    confidenceSlugging11 + Math.max(0, confidenceSkirmishing11) > 0.2 || killPotential
  }

  def killPotential: Boolean = {
    confidenceAttacking11 > 0.5
  }

  def safeSkirmishing: Boolean = {
    confidenceSkirmishing11 > 0.25 || skirmishBetterThanDefense
  }

  def skirmishBetterThanDefense: Boolean = {
    confidenceSkirmishing11 > confidenceDefending11 + 0.25
  }

  def confidenceDefending11   : Double = With.battles.globalDefend  .judgement.map(_.confidence11Total).getOrElse(0.0)
  def confidenceAttacking11   : Double = With.battles.globalAttack  .judgement.map(_.confidence11Total).getOrElse(0.0)
  def confidenceSlugging11    : Double = With.battles.globalSlug    .judgement.map(_.confidence11Total).getOrElse(0.0)
  def confidenceSkirmishing11 : Double = With.battles.globalSkirmish.judgement.map(_.confidence11Total).getOrElse(0.0)
  def confidenceDefending01   : Double = With.battles.globalDefend  .judgement.map(_.confidence01Total).getOrElse(0.0)
  def confidenceAttacking01   : Double = With.battles.globalAttack  .judgement.map(_.confidence01Total).getOrElse(0.0)
  def confidenceSlugging01    : Double = With.battles.globalSlug    .judgement.map(_.confidence01Total).getOrElse(0.0)
  def confidenceSkirmishing01 : Double = With.battles.globalSkirmish.judgement.map(_.confidence01Total).getOrElse(0.0)

  def attacking: Boolean = {
    With.tactics.attackSquad.units.nonEmpty
  }

  def enemyProximity  : Double = With.scouting.enemyProximity
  def ourProximity    : Double = With.scouting.ourProximity

  def gasCapsUntouched: Boolean = (
    ! With.blackboard.gasWorkerCeiling.isSet
    && ! With.blackboard.gasWorkerFloor.isSet
    && ! With.blackboard.gasLimitFloor.isSet
    && ! With.blackboard.gasLimitCeiling.isSet
    && ! With.blackboard.gasWorkerRatio.isSet
  )

  def employing(strategies: Strategy*): Boolean = strategies.exists(_())

  def onMap(map: StarCraftMap*): Boolean = map.exists(_())

  def starts: Int = With.geography.startLocations.size

  def enemies(matchers: UnitFilter*): Int =  With.units.countEnemy(IsAny(matchers: _*))

  def enemiesHave(matchers: UnitFilter*): Boolean = matchers.exists(With.units.existsEnemy(_))

  def enemiesComplete(matchers: UnitFilter*): Int = With.units.countEnemy(IsAll(IsComplete, IsAny(matchers: _*)))

  def enemiesCompleteFor(frames: Int, matchers: UnitFilter*): Int = With.units.countEnemy(IsAll(IsCompleteFor(frames), IsAny(matchers: _*)))

  def enemiesHaveComplete(matchers: UnitFilter*): Boolean = matchers.exists(m => With.units.existsEnemy(IsAll(IsComplete, m)))

  def enemiesEver(matchers: UnitFilter*): Boolean = {
    matchers.exists(With.units.existsEverEnemy(_))
  }

  def enemiesShown(unitClasses: UnitClass*): Int = unitClasses.view.map(With.unitsShown.allEnemies(_)).sum

  def enemyHasShown(unitClasses: UnitClass*): Boolean = unitClasses.exists(enemiesShown(_) > 0)

  def enemyHasTech(techs: Tech*): Boolean = techs.exists(t => With.enemies.exists(_.hasTech(t)))

  def enemyHasUpgrade(upgrade: Upgrade, level: Int = 1): Boolean = With.enemies.exists(_.getUpgradeLevel(upgrade) >= level)

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
    With.fingerprints.dtRush()
    || enemyHasUpgrade(Protoss.ZealotSpeed)
    || enemiesShown(Protoss.DarkTemplar, Protoss.HighTemplar, Protoss.Archon, Protoss.DarkArchon, Protoss.TemplarArchives, Protoss.ArbiterTribunal, Protoss.Arbiter) > 0
  )

  def enemyRobo: Boolean = {
    enemyHasShown(Protoss.RoboticsFacility, Protoss.RoboticsSupportBay, Protoss.Observatory, Protoss.Shuttle, Protoss.Reaver, Protoss.Observer)
  }

  def enemyLurkersLikely: Boolean = {
    enemyHasShown(Zerg.Lurker, Zerg.LurkerEgg) || (With.self.isTerran && enemyHasShown(Zerg.Hydralisk, Zerg.HydraliskDen) && enemyHasShown(Zerg.Lair, Zerg.Hive, Zerg.Spire, Zerg.Mutalisk))
  }

  def enemyHydralisksLikely: Boolean = {
    enemyHasShown(Zerg.Hydralisk, Zerg.HydraliskDen)
  }

  def enemyMutalisksLikely: Boolean = {
    enemyHasShown(Zerg.Mutalisk, Zerg.Spire) || (enemyHasShown(Zerg.Lair) && ! enemyHasShown(Zerg.Hydralisk, Zerg.HydraliskDen, Zerg.Lurker, Zerg.LurkerEgg))
  }

  def enemyCarriersLikely: Boolean = {
    enemyHasShown(Protoss.Carrier, Protoss.Interceptor, Protoss.FleetBeacon) || enemyHasUpgrade(Protoss.CarrierCapacity)
  }

  def enemyArbitersLikely: Boolean = {
    enemyHasShown(Protoss.Arbiter, Protoss.ArbiterTribunal) ||
      (enemyHasShown(Protoss.Stargate)&& enemyHasShown(Protoss.DarkTemplar, Protoss.HighTemplar, Protoss.Archon, Protoss.DarkArchon, Protoss.TemplarArchives))
  }

  def scoutCleared: Boolean = {
    With.scouting.enemyScouts().isEmpty || (
      With.scouting.enemyScouts().forall( ! _.likelyStillThere)
      && With.scouting.zonesToLookForEnemyScouts().forall(_.tiles.forall(_.explored)))
  }

  def enemyBases: Int = With.geography.enemyBases.size
  def enemyMiningBases: Int = With.geography.enemyBases.count(isMiningBase)
  def foundEnemyBase        : Boolean = enemyBases > 0
  def enemyNaturalConfirmed : Boolean = With.geography.enemyBases.exists(b => b.naturalOf.isDefined && b.townHall.isDefined)
  def enemyCrossSpawn       : Boolean = With.scouting.enemyMain.exists(_.isCross)

  def enemyIs(race: Race): Boolean = With.enemies.exists(_.raceCurrent == race)
  def enemyIsTerran   : Boolean = enemyIs(Race.Terran)
  def enemyIsProtoss  : Boolean = enemyIs(Race.Protoss)
  def enemyIsZerg     : Boolean = enemyIs(Race.Zerg)
  def enemyIsRandom   : Boolean = enemyIs(Race.Unknown)
  def enemyRaceKnown  : Boolean = enemyIsTerran || enemyIsProtoss || enemyIsZerg
  def enemyStrategy(fingerprints: Fingerprint*): Boolean = fingerprints.exists(_())

  def enemyRecentStrategy(fingerprints: Fingerprint*): Boolean = {
    enemyStrategy(fingerprints: _*) || fingerprints.exists(With.strategy.enemyRecentFingerprints.contains)
  }

  def trackRecordLacks(fingerprints: Fingerprint*): Boolean = {
    gamesAgainst >= With.configuration.recentFingerprints && ! enemyRecentStrategy(fingerprints: _*)
  }

  def gamesAgainst: Int = With.history.gamesVsEnemies.size
}