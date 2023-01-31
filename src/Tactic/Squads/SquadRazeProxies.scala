package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Utilities.UnitFilters.IsWarrior
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class SquadRazeProxies(assignments: Map[FriendlyUnitInfo, UnitInfo]) extends Squad {
  private val proxyPixels   = assignments.values.toSeq.map(_.pixel).distinct
  private val centroidPixel = Maff.centroid(proxyPixels)
  private val centroidUnit  = Maff.minBy(proxyPixels)(_.pixelDistance(centroidPixel)).getOrElse(With.scouting.enemyThreatOrigin.center)

  override def launch(): Unit = {}

  override def run(): Unit = {
    units.foreach(unit => {
      val assignee = assignments.get(unit)
      val attackTarget = if (With.units.existsEnemy(IsWarrior)) None else assignee
      unit.intend(this)
        .setCanFlee(false)
        .setTravel(assignee.map(_.pixel).getOrElse(centroidUnit))
        .setAttack(attackTarget)
    })
  }

  override val toString: String = "Raze proxies"
}