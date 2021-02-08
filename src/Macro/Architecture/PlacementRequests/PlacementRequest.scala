package Macro.Architecture.PlacementRequests

import Lifecycle.With
import Macro.Architecture.Blueprint
import Mathematics.Points.Tile
import Planning.Prioritized
import ProxyBwapi.UnitClasses.UnitClass
import Utilities.Forever

class PlacementRequest(
  val blueprint: Blueprint,
  var tile: Option[Tile] = None,
  var requiredFrame: Int = With.frame, // Frame this placement needs to be valid by; concerns future availability of Pylon power
  var child: Option[PlacementRequest] = Option.empty,
  var policy: () => PlacementPolicy = null) {

  if (policy == null) {
    policy = () => new PlacementPolicyClassic(this)
  }

  var lastPlacementFrame: Int = - Forever()

  def unitClass: UnitClass = blueprint.building
  def plan: Option[Prioritized] = With.groundskeeper.getRequestHolder(this)

  // Attempted performance improvement
  final override val hashCode: Int = super.hashCode()

  override def toString: String = "PlacementRequest: " + tile + " for " + blueprint + " in " + (requiredFrame - With.frame) + " frames"
}