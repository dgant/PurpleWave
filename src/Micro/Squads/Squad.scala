package Micro.Squads

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Squads.Goals.{GoalChill, SquadGoal}
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.EnrichPixel.EnrichedPixelCollection

import scala.collection.mutable.ArrayBuffer

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
  var recruits: ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty
  
  def update() {
    if (recruits.nonEmpty) {
      goal.squad = this
      goal.run()
    }
  }
  
  def conscript(units: Iterable[FriendlyUnitInfo]) {
    recruits.clear()
    recruits ++= units
    With.squads.commission(this)
  }
  
  def recruit(unit: FriendlyUnitInfo) {
    recruits += unit
    With.squads.addUnit(this, unit)
  }
  
  def centroid: Pixel = {
    if (recruits.isEmpty)
      With.geography.home.pixelCenter
    else
      recruits.map(_.pixelCenter).centroid
  }
}
