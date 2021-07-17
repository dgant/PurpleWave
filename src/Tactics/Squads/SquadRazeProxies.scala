package Tactics.Squads

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
import Planning.UnitMatchers.MatchWarriors
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class SquadRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends Squad {
  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = Maff.centroid(proxyPixels)
  private val centroidUnit  = Maff.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(With.scouting.threatOrigin.center)

  override def launch(): Unit = {}

  override def run() {
    units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = if (With.units.existsEnemy(MatchWarriors)) None else assignee
      unit.intend(this, new Intention {
        toTravel  = Some(assignee.map(_.pixel).getOrElse(centroidUnit))
        toAttack  = attackTarget
        canFlee   = assignments.keys.forall( ! _.unitClass.isWorker)
      })
    })
  }

  override val toString: String = "Raze proxies"
}