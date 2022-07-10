package Information.GameSense

import Information.GameSense.GameSenses._
import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.CountMap
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsProxied, IsWorker}

import scala.collection.mutable

class GameSensor {

  EconAdvantage.setOpposite(EconDisadvantage)
  EconDisadvantage.setOpposite(EconAdvantage)
  MuscleAdvantage.setOpposite(MuscleDisadvantage)
  MuscleDisadvantage.setOpposite(MuscleAdvantage)
  MapControl.setOpposite(MapContained)
  MapContained.setOpposite(MapControl)

  private case class GameSenseEvent(sense: GameSense, untilFrame: Int)

  private val currentEvents = new mutable.HashMap[GameSense, GameSenseEvent]
  private var _enemyMinedMinerals         : Double = 0.0
  private var _enemyMinedGas              : Double = 0.0
  private var _enemyLarva                 : Double = 0.0
  private var _ourProductionFrames        : CountMap[UnitClass] = new CountMap
  private var _enemyProductionFrames      : CountMap[UnitClass] = new CountMap
  private var _enemyWorkerSnapshotFrame   : Int = 0
  private var _enemyWorkerSnapshotDeadOld : Int = 0
  private var _enemyWorkerSnapshotDeadNew : Int = 0
  private var _enemyWorkerSnapshot        : Int = 0
  private var _ourLostMinerals            : Double = 0.0
  private var _ourLostGas                 : Double = 0.0
  private var _enemyLostMinerals          : Double = 0.0
  private var _enemyLostGas               : Double = 0.0
  private var _ourPeaceMinerals           : Double = 0.0
  private var _ourPeaceGas                : Double = 0.0
  private var _enemyPeaceMinerals         : Double = 0.0
  private var _enemyPeaceGas              : Double = 0.0
  private var _ourWarMinerals             : Double = 0.0
  private var _ourWarGas                  : Double = 0.0
  private var _enemyWarMinerals           : Double = 0.0
  private var _enemyWarGas                : Double = 0.0
  private var _enemySecretMinerals        : Double = 0.0
  private var _enemySecretGas             : Double = 0.0
  
  def senses: Iterable[GameSense] = currentEvents.keys
  def ourMinedMinerals    : Double = With.self.gatheredMinerals
  def ourMinedGas         : Double = With.self.gatheredGas
  def enemyMinedMinerals  : Double = _enemyMinedMinerals
  def enemyMinedGas       : Double = _enemyMinedGas
  def ourLostMinerals     : Double = _ourLostMinerals
  def ourLostGas          : Double = _ourLostGas
  def enemyLostMinerals   : Double = _enemyLostMinerals
  def enemyLostGas        : Double = _enemyLostGas
  def ourPeaceMinerals    : Double = _ourPeaceMinerals
  def ourPeaceGas         : Double = _ourPeaceGas
  def enemyPeaceMinerals  : Double = _enemyPeaceMinerals
  def enemyPeaceGas       : Double = _enemyPeaceGas
  def ourWarMinerals      : Double = _ourWarMinerals
  def ourWarGas           : Double = _ourWarGas
  def enemyWarMinerals    : Double = _enemyWarMinerals
  def enemyWarGas         : Double = _enemyWarGas
  def enemySecretMinerals : Double = _enemySecretMinerals
  def enemySecretGas      : Double = _enemySecretGas

  private var lastUpdate: Int = 0
  def update(): Unit = {
    detectEvents()
    currentEvents --= currentEvents.filter(_._2.untilFrame <= With.frame).keys
    lastUpdate = With.frame
  }

  private def isWar(unit: UnitInfo): Boolean = unit.unitClass.supplyProvided == 0 && unit.unitClass.attacksOrCastsOrDetectsOrTransports && ( ! unit.unitClass.isBuilding || IsProxied(unit))
  private def deadEnemyWorkers: Int = With.units.deadEnemy.count(u => u.unitClass.isWorker)
  private def detectEvents(): Unit = {
    if (With.geography.enemyBases.nonEmpty && With.geography.enemyBases.forall(_.resources.forall(r => With.framesSince(r.lastSeen) < 4 * Terran.SCV.buildFrames))) {
      _enemyWorkerSnapshotDeadOld = _enemyWorkerSnapshotDeadOld + deadEnemyWorkers
      _enemyWorkerSnapshotDeadNew = 0
      _enemyWorkerSnapshot = With.units.countEnemy(IsWorker)
      _enemyWorkerSnapshotFrame = With.frame
    }
    _ourLostMinerals        = With.units.deadOurs .map(_.unitClass.mineralValue).sum
    _ourLostGas             = With.units.deadOurs .map(_.unitClass.gasValue).sum
    _enemyLostMinerals      = With.units.deadEnemy.map(_.unitClass.mineralValue).sum
    _enemyLostGas           = With.units.deadEnemy.map(_.unitClass.gasValue).sum
    _ourPeaceMinerals       = With.units.everOurs.filterNot(isWar).map(_.unitClass.mineralPrice).sum
    _ourPeaceGas            = With.units.everOurs.filterNot(isWar).map(_.unitClass.gasPrice).sum
    _enemyPeaceMinerals     = With.units.everEnemy.filterNot(isWar).map(_.unitClass.mineralPrice).sum
    _enemyPeaceGas          = With.units.everEnemy.filterNot(isWar).map(_.unitClass.gasPrice).sum
    _ourWarMinerals         = With.units.everOurs.filter(isWar).map(_.unitClass.mineralPrice).sum
    _ourWarGas              = With.units.everOurs.filter(isWar).map(_.unitClass.gasPrice).sum
    _enemyWarMinerals       = With.units.everEnemy.filter(isWar).map(_.unitClass.mineralPrice).sum
    _enemyWarGas            = With.units.everEnemy.filter(isWar).map(_.unitClass.gasPrice).sum
    
    val deltaFrames         = With.framesSince(lastUpdate)
    val deltaFrameSnapshot  = With.framesSince(_enemyWorkerSnapshotFrame)
    val ourBaseCount        = MacroFacts.miningBases
    val enemyBaseCount      = Math.max(Maff.fromBoolean(With.geography.startBases.exists(!_.scoutedByUs)), With.geography.enemyBases.count(MacroFacts.isMiningBase))
    val workersPar          = 4 + With.frame * Terran.SCV.buildFrames
    val ourWorkerCount      = With.units.countOurs(IsWorker)
    val enemyWorkerCount    = With.units.countEnemy(IsWorker)
    val ourWorkerDeaths     = With.units.deadOurs.count(IsWorker)
    val enemyWorkerDeaths   = With.units.deadEnemy.count(IsWorker)
    val ourWorkerDelta      = Maff.max(With.strategy.selected.view.map(_.workerDelta)).getOrElse(0) + With.blackboard.workerDelta()
    val enemyWorkerDelta    = Maff.max(With.fingerprints.all.view.filter(_()).map(_.workerDelta)).getOrElse(0)
    val ourWorkerCap        = Maff.clamp(21, 75, 2 + With.geography.ourBases  .view.flatMap(_.minerals).size + 3 * With.geography.ourBases  .view.flatMap(_.gas).count(u => u.isOurs   && u.complete && u.gasLeft > 0))
    val enemyWorkerCap      = Maff.clamp(21, 75, 2 + With.geography.enemyBases.view.flatMap(_.minerals).size + 3 * With.geography.enemyBases.view.flatMap(_.gas).count(u => u.isEnemy  && u.complete && u.gasLeft > 0))
    val enemyWorkerProject  = _enemyWorkerSnapshot + Math.min(Minutes(5)(), deltaFrameSnapshot) / Terran.SCV.buildFrames + _enemyWorkerSnapshotDeadOld - deadEnemyWorkers
    var enemyWorkerEstimate = Math.max(enemyWorkerProject, enemyWorkerCount)
  }

  def remove(sense: GameSense): Unit = {
    currentEvents.remove(sense)
  }

  def spawnEvent(event: GameSenseEvent): Unit = {
    if (event.untilFrame <= With.frame) return
    val previous = currentEvents.get(event.sense)
    val canonical = (previous.toSeq :+ event).maxBy(_.untilFrame)


    currentEvents(event.sense) = canonical
    canonical.sense.opposite.foreach(currentEvents.remove)
  }
}
