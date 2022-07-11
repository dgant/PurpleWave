package Information.GameSense

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import ProxyBwapi.Techs.Techs
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrades
import Utilities.CountMap
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsProxied, IsWorker}

trait EconomicModel {

  private var _enemyMinedMinerals         : Double = 0.0
  private var _enemyMinedGas              : Double = 0.0
  private var _enemyLarva                 : Double = 0.0
  private var _ourProductionFrames        : CountMap[UnitClass] = new CountMap
  private var _enemyProductionFrames      : CountMap[UnitClass] = new CountMap
  private var _enemyWorkerSnapshotFrame   : Int = 0
  private var _enemyWorkerSnapshotDeadOld : Int = 0
  private var _enemyWorkerSnapshotDeadNew : Int = 0
  private var _enemyWorkerSnapshot        : Int = 0
  private var _ourUpgradeMinerals         : Int = 0
  private var _ourUpgradeGas              : Int = 0
  private var _enemyUpgradeMinerals       : Int = 0
  private var _enemyUpgradeGas            : Int = 0
  private var _ourTechMinerals            : Int = 0
  private var _ourTechGas                 : Int = 0
  private var _enemyTechMinerals          : Int = 0
  private var _enemyTechGas               : Int = 0
  private var _ourLostMinerals            : Double = 0.0
  private var _ourLostGas                 : Double = 0.0
  private var _enemyLostMinerals          : Double = 0.0
  private var _enemyLostGas               : Double = 0.0
  private var _ourPeaceMinerals           : Double = 0.0
  private var _ourPeaceGas                : Double = 0.0
  private var _enemyPeaceMinerals         : Double = 0.0
  private var _enemyPeaceGas              : Double = 0.0
  private var _ourWarUnitMinerals         : Double = 0.0
  private var _ourWarUnitGas              : Double = 0.0
  private var _enemyWarUnitMinerals       : Double = 0.0
  private var _enemyWarUnitGas            : Double = 0.0
  private var _ourWarMinerals             : Double = 0.0
  private var _ourWarGas                  : Double = 0.0
  private var _enemyWarMineralsFloor      : Double = 0.0
  private var _enemyWarGasFloor           : Double = 0.0
  private var _enemyWarMineralsCeiling    : Double = 0.0
  private var _enemyWarGasCeiling         : Double = 0.0
  private var _enemySecretMinerals        : Double = 0.0
  private var _enemySecretGas             : Double = 0.0

  def ourMinedMinerals        : Double = With.self.gatheredMinerals
  def ourMinedGas             : Double = With.self.gatheredGas
  def enemyMinedMinerals      : Double = _enemyMinedMinerals
  def enemyMinedGas           : Double = _enemyMinedGas
  def ourUpgradeMinerals      : Double = _ourUpgradeMinerals
  def ourUpgradeGas           : Double = _ourUpgradeGas
  def enemyUpgradeMinerals    : Double = _enemyUpgradeMinerals
  def enemyUpgradeGas         : Double = _enemyUpgradeGas
  def ourTechMinerals         : Double = _ourTechMinerals
  def ourTechGas              : Double = _ourTechGas
  def enemyTechMinerals       : Double = _enemyTechMinerals
  def enemyTechGas            : Double = _enemyTechGas
  def ourLostMinerals         : Double = _ourLostMinerals
  def ourLostGas              : Double = _ourLostGas
  def enemyLostMinerals       : Double = _enemyLostMinerals
  def enemyLostGas            : Double = _enemyLostGas
  def ourPeaceMinerals        : Double = _ourPeaceMinerals
  def ourPeaceGas             : Double = _ourPeaceGas
  def enemyPeaceMinerals      : Double = _enemyPeaceMinerals
  def enemyPeaceGas           : Double = _enemyPeaceGas
  def ourWarUnitMinerals      : Double = _ourWarUnitMinerals
  def ourWarUnitGas           : Double = _ourWarUnitGas
  def ourWarMinerals          : Double = _ourWarMinerals
  def ourWarGas               : Double = _ourWarGas
  def enemyWarUnitMinerals    : Double = _enemyWarUnitMinerals
  def enemyWarUnitGas         : Double = _enemyWarUnitGas
  def enemyWarMineralsFloor   : Double = _enemyWarMineralsFloor
  def enemyWarGasFloor        : Double = _enemyWarGasFloor
  def enemyWarMineralsCeiling : Double = _enemyWarMineralsCeiling
  def enemyWarGasCeiling      : Double = _enemyWarGasCeiling
  def enemySecretMinerals     : Double = _enemySecretMinerals
  def enemySecretGas          : Double = _enemySecretGas

  private def isWar(unit: UnitInfo): Boolean = (
    unit.unitClass.supplyProvided == 0
      && unit.unitClass.attacksOrCastsOrDetectsOrTransports
      && ! unit.unitClass.isWorker
      && ( ! unit.unitClass.isBuilding || IsProxied(unit)))

  private var lastUpdate: Int = 0
  protected def updateEconomicModel(): Unit = {
    val deadEnemyWorkers = With.units.deadEnemy.count(IsWorker)
    if (With.geography.enemyBases.nonEmpty && With.geography.enemyBases.forall(_.resources.forall(r => With.framesSince(r.lastSeen) < 4 * Terran.SCV.buildFrames))) {
      _enemyWorkerSnapshotDeadOld += deadEnemyWorkers
      _enemyWorkerSnapshotDeadNew = 0
      _enemyWorkerSnapshot        = With.units.countEnemy(IsWorker)
      _enemyWorkerSnapshotFrame   = With.frame
    }
    _ourUpgradeMinerals       = Upgrades.all.view.filter(_()).flatMap(u => (1 to With.self.getUpgradeLevel(u)).map(u.mineralPrice(_))).sum
    _ourUpgradeGas            = Upgrades.all.view.filter(_()).flatMap(u => (1 to With.self.getUpgradeLevel(u)).map(u.gasPrice(_))).sum
    _enemyUpgradeMinerals     = Upgrades.all.view.filter(_()).flatMap(u => With.enemies.flatMap(e => (1 to e.getUpgradeLevel(u))).map(u.mineralPrice(_))).sum
    _enemyUpgradeGas          = Upgrades.all.view.filter(_()).flatMap(u => With.enemies.flatMap(e => (1 to e.getUpgradeLevel(u))).map(u.gasPrice(_))).sum
    _ourTechMinerals          = Techs.all.view.filter(_()).map(_.mineralPrice).sum
    _ourTechGas               = Techs.all.view.filter(_()).map(_.gasPrice).sum
    _enemyTechMinerals        = With.enemies.flatMap(e => Techs.all.view.filter(_(e))).map(_.mineralPrice).sum
    _enemyTechGas             = With.enemies.flatMap(e => Techs.all.view.filter(_(e))).map(_.gasPrice).sum
    _ourLostMinerals          = With.units.deadOurs .map(_.unitClass.mineralValue).sum
    _ourLostGas               = With.units.deadOurs .map(_.unitClass.gasValue).sum
    _enemyLostMinerals        = With.units.deadEnemy.map(_.unitClass.mineralValue).sum
    _enemyLostGas             = With.units.deadEnemy.map(_.unitClass.gasValue).sum
    _ourPeaceMinerals         = With.units.everOurs   .filterNot(isWar).map(_.unitClass.mineralPrice).sum
    _ourPeaceGas              = With.units.everOurs   .filterNot(isWar).map(_.unitClass.gasPrice).sum
    _enemyPeaceMinerals       = With.units.everEnemy  .filterNot(isWar).map(_.unitClass.mineralPrice).sum
    _enemyPeaceGas            = With.units.everEnemy  .filterNot(isWar).map(_.unitClass.gasPrice).sum
    _ourWarMinerals           = With.units.everOurs   .filter   (isWar).map(_.unitClass.mineralPrice).sum
    _ourWarGas                = With.units.everOurs   .filter   (isWar).map(_.unitClass.gasPrice).sum
    _enemyWarUnitMinerals     = With.units.everEnemy  .filter   (isWar).map(_.unitClass.mineralPrice).sum
    _enemyWarUnitGas          = With.units.everEnemy  .filter   (isWar).map(_.unitClass.gasPrice).sum
    _enemyWarMineralsFloor    = _enemyWarUnitMinerals + _enemyUpgradeMinerals + enemyTechMinerals
    _enemyWarGasFloor         = _enemyWarUnitGas      + _enemyUpgradeGas      + enemyTechGas
    _enemySecretMinerals      = enemyMinedMinerals  - enemyLostMinerals - enemyPeaceMinerals  - enemyWarMineralsFloor
    _enemySecretGas           = enemyMinedGas       - enemyLostGas      - enemyPeaceGas       - enemyWarGasFloor
    _enemyWarMineralsCeiling  = enemyWarMineralsFloor + Math.max(0, enemySecretMinerals)
    _enemyWarGasCeiling       = enemyWarGasFloor      + Math.max(0, enemySecretGas)

    val deltaFrames           = With.framesSince(lastUpdate)
    val deltaFrameSnapshot    = With.framesSince(_enemyWorkerSnapshotFrame)
    val ourBaseCount          = MacroFacts.miningBases
    val enemyBaseCount        = Math.max(Maff.fromBoolean(With.geography.startBases.exists(!_.scoutedByUs)), With.geography.enemyBases.count(MacroFacts.isMiningBase))
    val workersPar            = 4 + With.frame * Terran.SCV.buildFrames
    val ourWorkerCount        = With.units.countOurs        (IsWorker)
    val enemyWorkerCount      = With.units.countEnemy       (IsWorker)
    val ourWorkerDeaths       = With.units.deadOurs.count   (IsWorker)
    val enemyWorkerDeaths     = With.units.deadEnemy.count  (IsWorker)
    val ourWorkerDelta        = Maff.max(With.strategy.selected.view.map(_.workerDelta)).getOrElse(0) + With.blackboard.workerDelta()
    val enemyWorkerDelta      = Maff.max(With.fingerprints.all.view.filter(_()).map(_.workerDelta)).getOrElse(0)
    val enemyBaseMinerals     = With.geography.enemyBases.view.flatMap(_.minerals).size
    val enemyBaseGas          = With.geography.enemyBases.view.flatMap(_.gas).count(u => u.isEnemy && u.complete && u.gasLeft > 0)
    val enemyWorkerCap        = Maff.clamp(21, 75, 2 + enemyBaseMinerals + 3 * enemyBaseGas)
    val enemyWorkerProject    = _enemyWorkerSnapshot + Math.min(Minutes(5)(), deltaFrameSnapshot) / Terran.SCV.buildFrames + _enemyWorkerSnapshotDeadOld - deadEnemyWorkers
    val enemyWorkerEstimate   = Math.min(enemyWorkerCap, Math.max(enemyWorkerProject, enemyWorkerCount))
    var enemyGasMiners        = Math.min(3 * enemyBaseGas,      enemyWorkerEstimate * 8 / 3)
    val enemyMineralMiners    = Math.min(2 * enemyBaseMinerals, enemyWorkerEstimate - enemyGasMiners)
    enemyGasMiners            = Math.min(3 * enemyBaseGas,      enemyWorkerEstimate - enemyMineralMiners)
    val enemyBaseMineralsGone = With.geography.enemyBases.view.filterNot(_.allTimeOwners.exists(_.isFriendly)).map(b => b.startingMinerals - b.mineralsLeft).sum
    _enemyMinedMinerals       += deltaFrames * enemyMineralMiners * With.accounting.workerIncomePerFrameMinerals
    _enemyMinedGas            += deltaFrames * enemyGasMiners     * With.accounting.workerIncomePerFrameGas
    _enemyMinedMinerals       = Math.max(_enemyMinedMinerals, enemyBaseMineralsGone)

    lastUpdate = With.frame
  }
}
