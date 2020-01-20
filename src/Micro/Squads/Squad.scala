package Micro.Squads

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

  def units: Set[FriendlyUnitInfo] = _unitsCache()
  def previousUnits: Set[FriendlyUnitInfo] = _previousunitsCache()
  private var _units: mutable.Set[FriendlyUnitInfo] = mutable.Set.empty
  private var _previousUnits: mutable.Set[FriendlyUnitInfo] = mutable.Set.empty
  private val _unitsCache = new Cache(() => _units.toSet)
  private val _previousunitsCache = new Cache(() => _previousUnits.toSet)

  final def clearUnits(): Unit = {
    _previousUnits = _units
    _units = mutable.Set.empty
  }

  final def addUnits(unit: Seq[FriendlyUnitInfo]): Unit = {
    _units ++= unit
  }
}
