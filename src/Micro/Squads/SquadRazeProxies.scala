package Micro.Squads

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

class SquadRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends Squad {
  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = PurpleMath.centroid(proxyPixels)
  private val centroidUnit  = ByOption.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(With.scouting.threatOrigin.pixelCenter)

  override def run() {
    units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = if (With.units.existsEnemy(MatchWarriors)) None else assignee
      unit.agent.intend(this, new Intention {
        toTravel  = Some(assignee.map(_.pixel).getOrElse(centroidUnit))
        toAttack  = attackTarget
        canFlee   = assignments.keys.forall( ! _.unitClass.isWorker)
      })
    })
  }

  override val toString: String = "Raze proxies"
}