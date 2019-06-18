package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass

class PlacementRequest(
   val blueprint: Blueprint,
   var tile: Option[Tile] = None,
   var plan: Option[Plan] = None,
   var task: () => PlacementTask = null) {
  if (task == null) {
    task = () => new PlacementTaskClassic(this)
  }

  var placementResult: Option[PlacementResult] = None

  def unitClass: UnitClass = blueprint.building

  def place(): Unit = {
    With.placement.place(this)
  }
}