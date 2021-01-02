package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.UnitMatchWarriors
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends SquadGoalBasic {

  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = PurpleMath.centroid(proxyPixels)
  private val centroidUnit  = ByOption.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(super.destination)

  override def destination: Pixel = centroidUnit
  override def inherentValue: Double = GoalValue.defendBase

  override def toString: String = "Raze proxies"

  override def run() {
    squad.units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = if (With.units.existsEnemy(UnitMatchWarriors)) None else assignee
      unit.agent.intend(squad.client, new Intention {
        toTravel  = Some(assignee.map(_.pixel).getOrElse(destination))
        toAttack  = attackTarget
        canFlee   = assignments.keys.forall( ! _.unitClass.isWorker)
      })
    })
  }
}