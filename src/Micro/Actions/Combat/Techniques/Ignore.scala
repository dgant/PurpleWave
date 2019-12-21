package Micro.Actions.Combat.Techniques

import Lifecycle.With
import Micro.Actions.Combat.Techniques.Common.ActionTechnique
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Ignore extends ActionTechnique {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    lazy val adjacentTiles    = unit.tileIncludingCenter.toRectangle.expand(1, 1).tiles.filter(_.valid)
    lazy val ground           = ! unit.flying
    lazy val blockingChoke    = ground && adjacentTiles.exists(tile => tile.zone.edges.exists(_.contains(tile.pixelCenter)))
    lazy val blockingPath     = ground && adjacentTiles.exists(With.coordinator.gridPathOccupancy.get(_) > 0)
    lazy val blockingNeighbor = ground && adjacentTiles.exists(With.grids.units.get(_).exists(neighbor => neighbor.friendly.exists(ally => ! ally.flying && ally.canBeIgnorantOfCombat)))
    lazy val canIgnore        = unit.canBeIgnorantOfCombat
    val output                = unit.canMove && canIgnore && ! blockingChoke && ! blockingNeighbor
    output
  }

  def threatBuffer(unit: FriendlyUnitInfo, threat: UnitInfo): Double = {
    val safetyBuffer  = 32 * 3
    val distance      = threat.pixelDistanceEdge(unit)
    val range         = threat.pixelRangeAgainst(unit)
    val gapClosed     = 24 * Math.max(0, threat.topSpeed - unit.topSpeed)
    val output        = distance - range - gapClosed - safetyBuffer
    output
  }

  def ignoranceBuffer(unit: FriendlyUnitInfo): Double = {
    val threatBuffers   = unit.matchups.threats.view.map(threatBuffer(unit, _))
    val threatBufferMin = ByOption.min(threatBuffers).getOrElse(0.0)
    val output          = threatBufferMin - unit.effectiveRangePixels / 3
    output
  }

  override protected def perform(unit: FriendlyUnitInfo): Unit = {}
}
