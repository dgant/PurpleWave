package Micro.Squads

import Debugging.Decap
import Information.Battles.Types.GroupCentroid
import Lifecycle.With
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Performance.Cache
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

import scala.collection.mutable

class Squad {
  def this(goal: SquadGoal) {
    this()
    setGoal(goal)
  }

  private var _enemies: Option[Iterable[UnitInfo]] = None
  def enemies: Iterable[UnitInfo] = _enemies.getOrElse(Seq.empty)
  def targetsEnemies: Boolean = _enemies.isDefined
  def setEnemies(value: Iterable[UnitInfo]): Unit = { _enemies = Some(value) }

  def run() {
    unitAges --= unitAges.keys.view.filterNot(units.contains)
    units.foreach(unitAges.add(_, 1))
    goal.run()
  }

  val unitAges: CountMap[FriendlyUnitInfo] = new CountMap[FriendlyUnitInfo]
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => unit.id + 10000 * unitAges.getOrElse(unit, 0))))
  )

  def commission(): Squad = {
    _units.clear()
    With.squads.commission(this);
    goal.onSquadCommission()
    this
  }

  //////////////////
  // Goal aspects //
  //////////////////

  private var _ourGoal: SquadGoal = _
  final def goal: SquadGoal = _ourGoal
  final def setGoal(goal: SquadGoal) {
    _ourGoal = goal
    goal.squad = this
    goal.onSquadCommission()
  }
  setGoal(new GoalChill)

  //////////////////
  // Unit aspects //
  //////////////////

  def units: Seq[FriendlyUnitInfo] = _units
  private val _units: mutable.ArrayBuffer[FriendlyUnitInfo] = new mutable.ArrayBuffer[FriendlyUnitInfo]()

  final def addUnit(unit: FriendlyUnitInfo): Unit = {
    _units += unit
    goal.addUnit(unit)
  }

  final def addUnits(units: Iterable[FriendlyUnitInfo]): Unit = {
    units.foreach(addUnit)
  }

  val centroidAir = new Cache(() => GroupCentroid.air(units))
  val centroidGround = new Cache(() => GroupCentroid.ground(units))

  override def toString: String = f"Squad to ${Decap(goal)}"
}
