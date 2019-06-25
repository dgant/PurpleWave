package Macro.Architecture.PlacementRequests

import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass

class PlacementRequest(
  val blueprint: Blueprint,
  var tile: Option[Tile] = None,
  var plan: Option[Plan] = None,
  var child: Option[PlacementRequest] = Option.empty,
  var task: () => PlacementPolicy = null) {

  if (task == null) {
    task = () => new PlacementPolicyClassic(this)
  }

  var result: Option[PlacementResult] = None

  def unitClass: UnitClass = blueprint.building
}