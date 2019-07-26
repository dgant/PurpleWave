package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Performance.Cache
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.CountMap

trait SquadWithGoal {
  private var ourGoal: SquadGoal = _
  def goal: SquadGoal = ourGoal
  def setGoal(goal: SquadGoal) {
    ourGoal = goal
    goal.squad = this.asInstanceOf[Squad]
  }
  setGoal(new GoalChill)
}

class Squad(val client: Plan) extends SquadWithGoal {
  
  var enemies: Seq[UnitInfo] = Seq.empty
  var previousUnits: Set[FriendlyUnitInfo] = Set.empty
  def units: Set[FriendlyUnitInfo] = With.squads.units(this)
  
  def update() {
    age --= age.keys.filterNot(units.contains)
    units.foreach(age.add(_, 1))
    goal.run()
  }
  
  def commission(): Unit = {
    With.squads.commission(this)
  }
  
  def recruit(unit: FriendlyUnitInfo) {
    With.squads.addUnit(this, unit)
  }
  
  def centroid: Pixel = {
    if (units.isEmpty)
      With.geography.home.pixelCenter
    else
      PurpleMath.centroid(units.map(_.pixelCenter))
  }

  val age = new CountMap[FriendlyUnitInfo]
  def leader(unitClass: UnitClass): Option[FriendlyUnitInfo] = leaders().get(unitClass)
  private val leaders: Cache[Map[UnitClass, FriendlyUnitInfo]] = new Cache(() =>
    units.toSeq.groupBy(_.unitClass).map(group => (group._1, group._2.maxBy(unit => age.getOrElse(unit, 0))))
  )
}
