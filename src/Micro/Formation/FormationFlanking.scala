package Micro.Formation

import Information.Geography.Pathfinding.Types.{ZonePath, ZonePathNode}

object FormationFlanking {
  case class Route(path: Option[ZonePath], flanking: Boolean)

  def choose(direct: Option[ZonePath], alternatives: => Seq[ZonePath], approachCost: ZonePathNode => Double): Route = {
    val choke = direct.flatMap(_.steps.lastOption)
    if ( ! choke.exists(approachCost(_) > 1.5)) return Route(direct, flanking = false)
    // Keep the original route as a fallback, including when no alternate is reachable.
    val candidates = direct.toSeq ++ alternatives.filter(_.steps.nonEmpty)
    val best = candidates.minBy(p => p.length * approachCost(p.steps.last))
    Route(Some(best), best.steps.last.edge != choke.get.edge)
  }
}
