package Micro.Squads

import Debugging.ToString
import Information.Battles.Types.GroupCentroid
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Squads.Qualities.QualityCounter
import Performance.Cache
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

trait Squad extends Prioritized {
  var batchId: Int = Int.MinValue
  var vicinity: Pixel = SpecificPoints.middle
  val lock: LockUnits = new LockUnits(this)
  private var _unitsNow = new ArrayBuffer[FriendlyUnitInfo]
  private var _unitsNext = new ArrayBuffer[FriendlyUnitInfo]
  private var _enemiesNow = new ArrayBuffer[UnitInfo]
  private var _enemiesNext = new ArrayBuffer[UnitInfo]
  private var _targetsEnemiesNow = false
  private var _targetsEnemiesNext = false
  private val _qualityCounter = new QualityCounter

  private def commission(): Unit = {
    if ( ! With.squads.isCommissioned(this)) {
      With.squads.commission(this)
      _enemiesNow.clear()
      _qualityCounter.clear()
    }
  }

  def candidateValue(candidate: FriendlyUnitInfo): Double = {
    commission()
    _qualityCounter.utility(candidate)
  }

  def units: Iterable[FriendlyUnitInfo] = _unitsNow
  @inline final def addUnits(units: Iterable[FriendlyUnitInfo]): Unit = units.foreach(addUnit)
  @inline final def addUnit(unit: FriendlyUnitInfo): Unit = {
    commission()
    _unitsNext += unit
    _qualityCounter.countUnit(unit)
  }

  def enemies: Iterable[UnitInfo] = _enemiesNow
  @inline final def targetsEnemies: Boolean = enemies.nonEmpty || _targetsEnemiesNow
  @inline final def addEnemies(enemies: Iterable[UnitInfo]): Unit = {
    commission()
    _targetsEnemiesNext = true
    enemies.foreach(_includeEnemy)
  }
  @inline final def addEnemy(enemy: UnitInfo): Unit = {
    commission()
    _targetsEnemiesNext = true
    _includeEnemy(enemy)
  }
  private def _includeEnemy(enemy: UnitInfo): Unit = {
    _enemiesNext += enemy
    _qualityCounter.countUnit(enemy)
  }

  def clearUnits(): Unit = {
    _unitsNow.clear()
    _enemiesNow.clear()
  }
  def swapUnits(): Unit = {
    val swapUnits = _unitsNow
    _unitsNow = _unitsNext
    _unitsNext = swapUnits
    _unitsNow.foreach(_.setSquad(Some(this)))
    val swapEnemies = _enemiesNow
    _enemiesNow = _enemiesNext
    _enemiesNext = swapEnemies
    _enemiesNow.foreach(_.foreign.foreach(_.addSquad(this)))
    _targetsEnemiesNow = _targetsEnemiesNext
    _targetsEnemiesNext = false
  }
  def run(): Unit

  val centroidAir = new Cache(() => GroupCentroid.air(units))
  val centroidGround = new Cache(() => GroupCentroid.ground(units))
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => unit.id + 10000 * unit.squadAge)))
  )

  override def toString: String = ToString(this)
}