package Micro.Coordination.Pathing

import Information.Grids.ArrayTypes.GridItems
import Mathematics.Points.Tile
import Micro.Coordination.Pushing.Push

class GridsPush {

  private var current   : GridItems[Push] = new GridItems[Push]
  private var previous  : GridItems[Push] = new GridItems[Push]

  def onAgentCycle(): Unit = {
    val swap = previous
    previous = current
    current = swap
    current.update()
  }

  def put(push: Push, tile: Tile): Unit = {
    current.addItem(push, tile)
  }

  def get(tile: Tile): Seq[Push] = {
    current.get(tile).view ++ previous.get(tile)
  }
}
