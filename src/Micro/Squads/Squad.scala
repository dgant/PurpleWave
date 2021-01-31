package Micro.Squads

import Lifecycle.With
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Performance.Cache
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

import scala.collection.mutable

class Squad(val client: Plan) {
  
  var enemies: Seq[UnitInfo] = Seq.empty
  
  def run() {
    age --= age.keys.filterNot(units.contains)
    units.foreach(age.add(_, 1))
    goal.run()
  }

  val age: CountMap[FriendlyUnitInfo] = new CountMap[FriendlyUnitInfo]
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.toSeq.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => age.getOrElse(unit, 0))))
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
  private var _conscripts: mutable.Set[FriendlyUnitInfo] = mutable.Set.empty
  private var _freelancers: mutable.Set[FriendlyUnitInfo] = mutable.Set.empty

  final def clearFreelancers(): Unit = {
    _freelancers = mutable.Set.empty
    updateUnits()
  }

  final def clearConscripts(): Unit = {
    _conscripts.clear()
    updateUnits()
  }

  final def addFreelancers(units: Iterable[FriendlyUnitInfo]): Unit = {
    _freelancers ++= units
    updateUnits()
  }

  final def addConscripts(units: Iterable[FriendlyUnitInfo]): Unit = {
    _conscripts ++= units
    updateUnits()
  }

  final private def updateUnits(): Unit = {
    _units = _conscripts.toSet ++ _freelancers
  }
}
