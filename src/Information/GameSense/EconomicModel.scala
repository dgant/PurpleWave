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
import Utilities.Time.{GameTime, Minutes}
import Utilities.UnitFilters.{IsHatchlike, IsWorker}

trait EconomicModel {

  private var _enemyMinedMinerals         : Double = 650.0
  private var _enemyMinedGas              : Double = 0.0
  private var _enemyLarva                 : Double = 3.0
  private var _ourProductionFrames        : CountMap[UnitClass] = new CountMap
  private var _enemyProductionFrames      : CountMap[UnitClass] = new CountMap
  private var _enemyWorkerSnapshotFrame   : Int = 0
  private var _enemyWorkerSnapshotDead    : Int = 0
  private var _enemyWorkerSnapshot        : Int = 4
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
  private var _ourWarUnitMinerals         : Double = 0.0
  private var _ourWarUnitGas              : Double = 0.0
  private var _enemyWarUnitMinerals       : Double = 0.0
  private var _enemyWarUnitGas            : Double = 0.0
  private var _ourProductionMinerals      : Double = 0.0
  private var _ourProductionGas           : Double = 0.0
  private var _enemyProductionMinerals    : Double = 0.0
  private var _enemyProductionGas         : Double = 0.0
  private var _ourScienceMinerals         : Double = 0.0
  private var _ourScienceGas              : Double = 0.0
  private var _enemyScienceMinerals       : Double = 0.0
  private var _enemyScienceGas            : Double = 0.0
  private var _ourBaseMinerals            : Double = 0.0
  private var _enemyBaseMinerals          : Double = 0.0
  private var _ourSupplyMinerals          : Double = 0.0
  private var _enemySupplyMinerals        : Double = 0.0
  private var _ourDefenseMinerals         : Double = 0.0
  private var _enemyDefenseMinerals       : Double = 0.0
  private var _ourWarMinerals             : Double = 0.0
  private var _ourWarGas                  : Double = 0.0
  private var _enemyWarMineralsFloor      : Double = 0.0
  private var _enemyWarGasFloor           : Double = 0.0
  private var _enemyWarMineralsCeiling    : Double = 0.0
  private var _enemyWarGasCeiling         : Double = 0.0
  private var _enemySecretMinerals        : Double = 0.0
  private var _enemySecretGas             : Double = 0.0

  def ourMinedMinerals        : Double = 600 + With.self.gatheredMinerals
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
  def netDamageMinerals       : Double = _enemyLostMinerals - _ourLostMinerals
  def netDamageGas            : Double = _enemyLostGas - _ourLostGas
  def netDamage               : Double = netDamageMinerals + 1.5 * netDamageGas
  def ourWorkerMinerals       : Double = 50 * With.units.countOurs(IsWorker)
  def enemyWorkerMinerals     : Double = 50 * With.units.countEnemy(IsWorker)
  def ourProductionMinerals   : Double = _ourProductionMinerals
  def ourProductionGas        : Double = _ourProductionGas
  def enemyProductionMinerals : Double = _enemyProductionMinerals
  def enemyProductionGas      : Double = _enemyProductionGas
  def ourScienceMinerals      : Double = _ourScienceMinerals
  def ourScienceGas           : Double = _ourScienceGas
  def enemyScienceMinerals    : Double = _enemyScienceMinerals
  def enemyScienceGas         : Double = _enemyScienceGas
  def ourDefenseMinerals      : Double = _ourDefenseMinerals
  def enemyDefenseMinerals    : Double = _enemyDefenseMinerals
  def ourSupplyMinerals       : Double = _ourSupplyMinerals
  def enemySupplyMinerals     : Double = _enemySupplyMinerals
  def ourBaseMinerals         : Double = _ourBaseMinerals
  def enemyBaseMinerals       : Double = _enemyBaseMinerals
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
      && ( ! unit.unitClass.isBuilding || unit.proxied))

  private var lastUpdate: Int = 0
  protected def updateEconomicModel(): Unit = {
    val deadEnemyWorkers = With.units.deadEnemy.count(IsWorker)
    val unscouted = With.geography.startBases.exists( ! _.scoutedByUs) && ! With.geography.enemyBases.exists(_.isStartLocation)
    if ( ! unscouted && With.geography.enemyBases.nonEmpty && With.geography.enemyBases.forall(_.resources.forall(r => With.framesSince(r.lastSeen) < 4 * Terran.SCV.buildFrames))) {
      _enemyWorkerSnapshotDead    = deadEnemyWorkers
      _enemyWorkerSnapshot        = With.units.countEnemy(IsWorker)
      _enemyWorkerSnapshotFrame   = With.frame
    }
    _ourUpgradeMinerals       = Upgrades.all.view.filter(_()).flatMap(u => (1 to With.self.getUpgradeLevel(u)).map(u.mineralPrice(_))).sum
    _ourUpgradeGas            = Upgrades.all.view.filter(_()).flatMap(u => (1 to With.self.getUpgradeLevel(u)).map(u.gasPrice(_))).sum
    _enemyUpgradeMinerals     = Upgrades.all.view.filter(_()).flatMap(u => With.enemies.flatMap(e => (1 to e.getUpgradeLevel(u))).map(u.mineralPrice(_))).sum
    _enemyUpgradeGas          = Upgrades.all.view.filter(_()).flatMap(u => With.enemies.flatMap(e => (1 to e.getUpgradeLevel(u))).map(u.gasPrice(_))).sum
    _ourTechMinerals          = Techs.nonFree.view.filter(_()).map(_.mineralPrice).sum
    _ourTechGas               = Techs.nonFree.view.filter(_()).map(_.gasPrice).sum
    _enemyTechMinerals        = With.enemies.flatMap(e => Techs.nonFree.view.filter(_(e))).map(_.mineralPrice).sum
    _enemyTechGas             = With.enemies.flatMap(e => Techs.nonFree.view.filter(_(e))).map(_.gasPrice).sum
    _ourLostMinerals          = With.units.deadOurs .map(_.unitClass.mineralValue).sum
    _ourLostGas               = With.units.deadOurs .map(_.unitClass.gasValue).sum
    _enemyLostMinerals        = With.units.deadEnemy.map(_.unitClass.mineralValue).sum
    _enemyLostGas             = With.units.deadEnemy.map(_.unitClass.gasValue).sum
    _ourBaseMinerals          = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Base).map(u => if (IsHatchlike(u)) 350 else 400).sum
    _enemyBaseMinerals        = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Base).map(u => if (IsHatchlike(u)) 350 else 400).sum
    _ourSupplyMinerals        = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Supply).map(_.unitClass.mineralValue).sum
    _enemySupplyMinerals      = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Supply).map(_.unitClass.mineralValue).sum
    _ourDefenseMinerals       = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Defense).map(_.unitClass.mineralValue).sum
    _enemyDefenseMinerals     = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Defense).map(_.unitClass.mineralValue).sum
    _ourWarUnitMinerals       = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.War).map(_.unitClass.mineralValue).sum
    _ourWarUnitGas            = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.War).map(_.unitClass.gasValue).sum
    _enemyWarUnitMinerals     = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.War).map(_.unitClass.mineralValue).sum
    _enemyWarUnitGas          = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.War).map(_.unitClass.gasValue).sum
    _ourProductionMinerals    = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Production).map(_.unitClass.mineralValue).sum
    _enemyProductionMinerals  = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Production).map(_.unitClass.mineralValue).sum
    _ourProductionGas         = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Production).map(_.unitClass.gasValue).sum
    _enemyProductionGas       = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Production).map(_.unitClass.gasValue).sum
    _ourScienceMinerals       = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Science).map(_.unitClass.mineralValue).sum
    _enemyScienceMinerals     = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Science).map(_.unitClass.mineralValue).sum
    _ourScienceGas            = With.units.ours       .filter(_.unitClass.budgetCategory == Budgets.Science).map(_.unitClass.gasValue).sum
    _enemyScienceGas          = With.units.enemy      .filter(_.unitClass.budgetCategory == Budgets.Science).map(_.unitClass.gasValue).sum
    _ourWarMinerals           = ourWarUnitMinerals    + ourTechMinerals       + ourUpgradeMinerals
    _ourWarGas                = ourWarUnitGas         + ourTechGas            + ourUpgradeGas
    _enemyWarMineralsFloor    = enemyWarUnitMinerals  + enemyUpgradeMinerals  + enemyTechMinerals
    _enemyWarGasFloor         = enemyWarUnitGas       + enemyUpgradeGas       + enemyTechGas
    _enemySecretMinerals      = enemyMinedMinerals    - enemyLostMinerals     - enemyWarMineralsFloor - enemyProductionMinerals - enemyScienceMinerals -  enemyWorkerMinerals - enemyBaseMinerals - enemySupplyMinerals - enemyDefenseMinerals
    _enemySecretGas           = enemyMinedGas         - enemyLostGas          - enemyWarGasFloor      - enemyProductionGas      - enemyScienceGas
    _enemyWarMineralsCeiling  = enemyWarMineralsFloor + Math.max(0, enemySecretMinerals)
    _enemyWarGasCeiling       = enemyWarGasFloor      + Math.max(0, enemySecretGas)

    val deltaFrames           = With.framesSince(lastUpdate)
    val deltaFrameSnapshot    = With.framesSince(_enemyWorkerSnapshotFrame)
    val ourBaseCount          = MacroFacts.miningBases
    val enemyBaseCount        = Math.max(Maff.fromBoolean(unscouted), With.geography.enemyBases.count(MacroFacts.isMiningBase))
    val workersPar            = 4 + With.frame * Terran.SCV.buildFrames
    val ourWorkerCount        = With.units.countOurs        (IsWorker)
    val enemyWorkerCount      = With.units.countEnemy       (IsWorker)
    val ourWorkerDeaths       = With.units.deadOurs.count   (IsWorker)
    val enemyWorkerDeaths     = With.units.deadEnemy.count  (IsWorker)
    val ourWorkerDelta        = Maff.max(With.strategy.selected.view.map(_.workerDelta)).getOrElse(0) + With.blackboard.workerDelta()
    val enemyWorkerDelta      = Maff.max(With.fingerprints.all.view.filter(_()).map(_.workerDelta)).getOrElse(0)
    val enemyBasePatchesMins  = (if (unscouted) 9 else 0)                                   + With.geography.enemyBases.view.flatMap(_.minerals).size
    val enemyBasePatchesGas   = (if (unscouted && With.frame > GameTime(2, 5)()) 1 else 0) + With.geography.enemyBases.view.flatMap(_.gas).count(u => u.isEnemy && u.complete && u.gasLeft > 0)
    val enemyWorkerCap        = Maff.clamp(21, 75, 2 + enemyBasePatchesMins + 3 * enemyBasePatchesGas)
    val enemyWorkerProject    = _enemyWorkerSnapshot + Math.min(Minutes(5)(), deltaFrameSnapshot) / Terran.SCV.buildFrames + _enemyWorkerSnapshotDead - deadEnemyWorkers
    val enemyWorkerEstimate   = Math.min(enemyWorkerCap, Math.max(enemyWorkerProject, enemyWorkerCount))
    var enemyGasMiners        = Math.min(3 * enemyBasePatchesGas,   enemyWorkerEstimate / 6)
    val enemyMineralMiners    = Math.min(2 * enemyBasePatchesMins,  enemyWorkerEstimate - enemyGasMiners)
    enemyGasMiners            = Math.min(3 * enemyBasePatchesGas,   enemyWorkerEstimate - enemyMineralMiners)
    val enemyBaseMineralsGone = With.geography.bases.view.filterNot(b => b.allTimeOwners.exists(_.isEnemy) && ! b.allTimeOwners.exists(_.isFriendly)).map(b => b.startingMinerals - b.mineralsLeft).sum
    _enemyMinedMinerals       += deltaFrames * enemyMineralMiners * With.accounting.workerIncomePerFrameMinerals
    _enemyMinedGas            += deltaFrames * enemyGasMiners     * With.accounting.workerIncomePerFrameGas
    _enemyMinedMinerals       = Math.max(_enemyMinedMinerals, enemyBaseMineralsGone)

    // TODO: Increase minerals/gas if observed exceeds predicted
    // TODO: Cut hypothetical workers if there's no budget
    // TODO: Include our in-progress upgrades/tech

    lastUpdate = With.frame
  }
}
