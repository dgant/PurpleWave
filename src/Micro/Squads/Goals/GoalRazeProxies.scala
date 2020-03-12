package Micro.Squads.Goals

import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends SquadGoalBasic {

  private val proxyPixels   = assignments.values.toSeq.map(_.pixelCenter).distinct
  private val centroidPixel = PurpleMath.centroid(proxyPixels)
  private val centroidUnit  = ByOption.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(super.destination)

  override def destination: Pixel = centroidUnit
  override def inherentValue: Double = GoalValue.defendBase

  override def toString: String = "Raze proxies"

  override def run() {
    squad.units.foreach(unit => {
      val assignee = assignments.get(unit)
      unit.agent.intend(squad.client, new Intention {
        toTravel  = Some(assignee.map(_.pixelCenter).getOrElse(destination))
        toAttack  = assignee
        canFlee   = assignments.keys.forall( ! _.unitClass.isWorker)
      })
    })
  }
}