package Information.Geography

import Information.Geography.Pathfinding.{PathfindProfile, Paths}
import Information.Geography.Pathfinding.Types.TilePath
import Lifecycle.With
import Mathematics.Points.Tile

import scala.collection.mutable

// Only for the fixed expansion profile below; other path costs need different cache inputs.
private[Geography] final class ExpansionPathCache {
  private var paths: Paths = _
  private var topologyVersion = -1L
  private var walkable = Array.empty[Boolean]
  private val results = new mutable.HashMap[(Tile, Tile, Boolean), TilePath]

  def refresh(): Unit = {
    var changed = (paths ne With.paths) || topologyVersion != With.paths.groundTopologyVersion
    if (walkable.length != With.mapTileArea) {
      walkable = new Array[Boolean](With.mapTileArea)
      changed = true
    }
    var i = 0
    while (i < walkable.length) {
      val current = With.grids.walkable.getUnchecked(i)
      if (walkable(i) != current) {
        walkable(i) = current
        changed = true
      }
      i += 1
    }
    if (changed) results.clear()
    paths = With.paths
    topologyVersion = paths.groundTopologyVersion
  }

  def find(from: Tile, to: Tile): TilePath = {
    // This profile's threatMaximum checks only its adjusted endpoint. It has no
    // threat cost or threatMaximumAlongPath; movement elsewhere cannot change it.
    val endThreatened = With.grids.enemyRangeAirGround.get(to.walkableTile) > 0
    results.getOrElseUpdate((from, to, endThreatened), {
      val profile = new PathfindProfile(from, Some(to))
      profile.threatMaximum = Some(0)
      profile.employGroundDist = true
      profile.lengthMaximum = Some(64)
      profile.find
    })
  }
}
