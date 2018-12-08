package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

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
}
