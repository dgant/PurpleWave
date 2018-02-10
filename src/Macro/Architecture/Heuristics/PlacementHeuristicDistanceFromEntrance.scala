package Macro.Architecture.Heuristics

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Utilities.ByOption

object PlacementHeuristicDistanceFromEntrance extends PlacementHeuristic {
  
  override def evaluate(blueprint: Blueprint, candidate: Tile): Double = {
    val zone      = candidate.zone
    val entrances = zone.edges.filter(edge => edge.otherSideof(zone).exit.contains(edge))
    val distances = entrances.map(_.pixelCenter.pixelDistanceFast(With.geography.home.pixelCenter))
    val distance  = ByOption.min(distances).getOrElse(0.0)
    val output    = Math.max(32.0, distance)
    output
  }
}
