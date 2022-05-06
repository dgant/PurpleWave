package Placement.Walls

import Debugging.SimpleString

object WallSpans {
  trait WallSpan extends SimpleString
  object TerrainTerrain extends WallSpan
  object TerrainGas     extends WallSpan
  object TerrainHall    extends WallSpan
}
