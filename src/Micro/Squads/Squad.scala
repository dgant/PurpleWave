package Micro.Squads

import Lifecycle.With
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Performance.Cache
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

import scala.collection.mutable

class Squad {
  
  var enemies: Seq[UnitInfo] = Seq.empty
  
  def run() {
    unitAges --= unitAges.keys.view.filterNot(units.contains)
    units.foreach(unitAges.add(_, 1))
    goal.run()
  }

  val unitAges: CountMap[FriendlyUnitInfo] = new CountMap[FriendlyUnitInfo]
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.toSeq.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => unit.id + 10000 * unitAges.getOrElse(unit, 0))))
  )

  def commission(): Unit = { With.squads.commission(this) }

  //////////////////
  // Goal aspects //
  //////////////////

  private var _ourGoal: SquadGoal = _
  final def goal: SquadGoal = _ourGoal
  final def setGoal(goal: SquadGoal) {
    _ourGoal = goal
    goal.setSquad(this.asInstanceOf[Squad])
  }
  setGoal(new GoalChill)

  //////////////////
  // Unit aspects //
  //////////////////

  def units: Set[FriendlyUnitInfo] = _units
  private var _units: Set[FriendlyUnitInfo] = Set.empty
  private var _conscripts   : mutable.ArrayBuffer[FriendlyUnitInfo] = mutable.ArrayBuffer.empty
  private var _freelancers  : mutable.ArrayBuffer[FriendlyUnitInfo] = mutable.ArrayBuffer.empty

  final def recalculateRosters(): Unit = {
    _units = (_conscripts.view ++ _freelancers).toSet
    _conscripts = mutable.ArrayBuffer.empty
    _freelancers = mutable.ArrayBuffer.empty
  }

  final def addFreelancers(units: Iterable[FriendlyUnitInfo]): Unit = {
    _freelancers ++= units
  }

  final def addConscripts(units: Iterable[FriendlyUnitInfo]): Unit = {
    _conscripts ++= units
  }

  override def toString: String = f"Squad to $goal"
}
