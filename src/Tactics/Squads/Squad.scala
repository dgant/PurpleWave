package Tactics.Squads

import Debugging.ToString
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Formation.Formation
import Performance.Cache
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactics.Squads.Qualities.QualityCounter
import Tactics.Tactic

import scala.collection.mutable.ArrayBuffer

abstract class Squad extends Tactic with FriendlyUnitGroup {
  var batchId: Int = Int.MinValue
  var vicinity: Pixel = SpecificPoints.middle
  var targetQueue: Option[Seq[UnitInfo]] = None
  var formations: ArrayBuffer[Formation] = ArrayBuffer.empty
  val lock: LockUnits = new LockUnits(this)

  private var _unitsNow = new ArrayBuffer[FriendlyUnitInfo]
  private var _unitsNext = new ArrayBuffer[FriendlyUnitInfo]
  private var _enemiesNow = new ArrayBuffer[UnitInfo]
  private var _enemiesNext = new ArrayBuffer[UnitInfo]
  private val _qualityCounter = new QualityCounter

  def commission(): Unit = {
    if ( ! With.squads.isCommissioned(this)) {
      With.squads.commission(this)
      _enemiesNow.clear()
      _qualityCounter.clear()
    }
  }

  final def candidateValue(candidate: FriendlyUnitInfo): Double = {
    commission()
    _qualityCounter.utility(candidate)
  }

  final def units: Seq[FriendlyUnitInfo] = _unitsNow
  final def unitsNext: Seq[FriendlyUnitInfo] = _unitsNext
  @inline final def addUnits(units: Iterable[FriendlyUnitInfo]): Unit = units.foreach(addUnit)
  @inline final def addUnit(unit: FriendlyUnitInfo): Unit = {
    commission()
    if ( ! _unitsNext.contains(unit)) {
      _unitsNext += unit
      _qualityCounter.countUnit(unit)
    }
  }

  final def enemies: Seq[UnitInfo] = _enemiesNow
  @inline final def addEnemies(enemies: Iterable[UnitInfo]): Unit = enemies.foreach(addEnemy)
  @inline final def addEnemy(enemy: UnitInfo): Unit = {
    commission()
    if ( ! _enemiesNext.contains(enemy)) {
      _enemiesNext += enemy
      _qualityCounter.countUnit(enemy)
    }
  }

  final def clearUnits(): Unit = {
    _unitsNow.clear()
    _enemiesNow.clear()
  }
  final def prepareToRun(): Unit = {
    val swapUnits = _unitsNow
    _unitsNow = _unitsNext
    _unitsNext = swapUnits
    _unitsNow.foreach(_.setSquad(Some(this)))
    val swapEnemies = _enemiesNow
    _enemiesNow = _enemiesNext
    _enemiesNext = swapEnemies
    targetQueue = None
    formations.clear()
  }

  def run(): Unit

  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => 100000L * unit.squadAge - unit.frameDiscovered)))
  )

  final def groupFriendlyUnits: Seq[FriendlyUnitInfo] = units

  override def toString: String = ToString(this).replace("Squad", "")
}