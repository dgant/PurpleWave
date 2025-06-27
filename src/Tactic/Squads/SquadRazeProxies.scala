package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.UnitFilters.IsWarrior

class SquadRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends Squad {
  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = Maff.centroid(proxyPixels)
  private val centroidUnit  = Maff.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(With.scouting.enemyThreatOrigin.center)

  override def launch(): Unit = {}

  override def run(): Unit = {
    units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = ?(With.units.existsEnemy(IsWarrior), None, assignee)
      unit.intend(this)
        .setCanFlee(unit.matchups.threats.exists(IsWarrior))
        .setTerminus(assignee.map(_.pixel).getOrElse(centroidUnit))
        .setAttack(attackTarget)
    })
  }

  override val toString: String = "Raze proxies"
}