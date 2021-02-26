package Micro.Squads.Goals

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class GoalRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends SquadGoal {

  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = PurpleMath.centroid(proxyPixels)
  private val centroidUnit  = ByOption.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(With.scouting.threatOrigin.pixelCenter)

  override def toString: String = "Raze proxies"

  override def run() {
    squad.units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = if (With.units.existsEnemy(MatchWarriors)) None else assignee
      unit.agent.intend(this, new Intention {
        toTravel  = Some(assignee.map(_.pixel).getOrElse(centroidUnit))
        toAttack  = attackTarget
        canFlee   = assignments.keys.forall( ! _.unitClass.isWorker)
      })
    })
  }
}