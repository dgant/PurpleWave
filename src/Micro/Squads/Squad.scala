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
  val lock: LockUnits = new LockUnits
  var batchId: Int = Int.MinValue
  var vicinity: Pixel = SpecificPoints.middle
  protected val _units = new ArrayBuffer[FriendlyUnitInfo]
  protected val _enemies = new ArrayBuffer[UnitInfo]
  protected val _qualityCounter = new QualityCounter

  private def commission(): Unit = {
    if ( ! With.squads.isCommissioned(this)) {
      With.squads.commission(this)
      _units.clear()
      _enemies.clear()
      _qualityCounter.clear()
    }
  }

  def candidateValue(candidate: FriendlyUnitInfo): Double = {
    commission()
    _qualityCounter.utility(candidate)
  }

  def units: Iterable[FriendlyUnitInfo] = _units
  @inline final def addUnits(units: Iterable[FriendlyUnitInfo]): Unit = units.foreach(addUnit)
  @inline final def addUnit(unit: FriendlyUnitInfo): Unit = {
    commission()
    _units += unit
    _qualityCounter.countUnit(unit)
  }

  def enemies: Iterable[UnitInfo] = _enemies
  @inline final def addEnemies(enemies: Iterable[UnitInfo]): Unit = enemies.foreach(addEnemy)
  @inline final def addEnemy(enemy: UnitInfo): Unit = {
    commission()
    // TODO: This process changes the squad's enemies on the fly while the friendly units update all at once.
    _enemies += enemy
    _qualityCounter.countUnit(enemy)
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