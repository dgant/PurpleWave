package Tactics.Squads

import Debugging.ToString
import Information.Battles.Types.GroupCentroid
import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Formation.Formation
import Tactics.Squads.Qualities.QualityCounter
import Performance.Cache
import Planning.Prioritized
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ArrayBuffer

trait Squad extends Prioritized {
  var batchId: Int = Int.MinValue
  var vicinity: Pixel = SpecificPoints.middle
  var formation: Option[Formation] = None
  val lock: LockUnits = new LockUnits(this)
  var targetQueue: Option[Seq[UnitInfo]] = None

  private var _unitsNow = new ArrayBuffer[FriendlyUnitInfo]
  private var _unitsNext = new ArrayBuffer[FriendlyUnitInfo]
  private var _enemiesNow = new ArrayBuffer[UnitInfo]
  private var _enemiesNext = new ArrayBuffer[UnitInfo]
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

  def units: Seq[FriendlyUnitInfo] = _unitsNow
  def unitsNext: Seq[FriendlyUnitInfo] = _unitsNext
  @inline final def addUnits(units: Iterable[FriendlyUnitInfo]): Unit = units.foreach(addUnit)
  @inline final def addUnit(unit: FriendlyUnitInfo): Unit = {
    commission()
    _unitsNext += unit
    _qualityCounter.countUnit(unit)
  }

  def enemies: Seq[UnitInfo] = _enemiesNow
  @inline final def addEnemies(enemies: Iterable[UnitInfo]): Unit = {
    commission()
    enemies.foreach(_includeEnemy)
  }
  @inline final def addEnemy(enemy: UnitInfo): Unit = {
    commission()
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
  }
  def run(): Unit

  val centroidAir = new Cache(() => GroupCentroid.air(units))
  val centroidGround = new Cache(() => GroupCentroid.ground(units))
  val area = new Cache(() => units.view.map(_.unitClass.area).sum)
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => 100000L * unit.squadAge - unit.frameDiscovered)))
  )

  override def toString: String = ToString(this).replace("Squad", "")
}