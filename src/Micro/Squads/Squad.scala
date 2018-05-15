package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel.EnrichedPixelCollection

trait SquadWithGoal {
  private var ourGoal: SquadGoal = new GoalChill
  def goal: SquadGoal = ourGoal
  def setGoal(goal: SquadGoal) {
    ourGoal = goal
    goal.squad = this.asInstanceOf[Squad]
  }
}

class Squad(val client: Plan) extends SquadWithGoal {
  
  var enemies: Iterable[UnitInfo] = Iterable.empty
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
      units.map(_.pixelCenter).centroid
  }
}
