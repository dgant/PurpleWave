package Planning.ResourceLocks

import Lifecycle.With
import Mathematics.Points.Tile
import Planning.Prioritized

class LockTiles(val owner: Prioritized) {

  private var _tiles: Seq[Tile] = Seq.empty
  var satisfied: Boolean = false

  def tiles: Seq[Tile] = _tiles

  def acquireTiles(tiles: Seq[Tile]): Boolean = {
    release()
    _tiles = tiles
    owner.prioritize()
    With.groundskeeper.satisfy(this)
  }

  def release(): Unit = {
    With.groundskeeper.release(this)
  }
}
