package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Planning.Plan
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.Forever

class PlacementRequest(
  val blueprint: Blueprint,
  var tile: Option[Tile] = None,
  var child: Option[PlacementRequest] = Option.empty,
  var task: () => PlacementPolicy = null) {

  if (task == null) {
    task = () => new PlacementPolicyClassic(this)
  }

  var lastPlacementFrame: Int = - Forever()

  def failed: Boolean = tile.isEmpty && lastPlacementFrame >= 0
  def unitClass: UnitClass = blueprint.building
  def plan: Option[Plan] = With.groundskeeper.getRequestHolder(this)
}