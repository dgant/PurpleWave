package Macro.Architecture

import Information.Grids.ArrayTypes.AbstractGridVersionedValue
import Mathematics.Points.Tile

class GridExclusion extends AbstractGridVersionedValue[Option[Exclusion]] {
    override val defaultValue: Option[Exclusion] = None
    override protected var values: Array[Option[Exclusion]] = Array.fill(length)(defaultValue)
    def excludes(tile: Tile, request: Option[PlacementRequest] = None): Boolean = {
      if ( ! tile.valid) return true
      val i = tile.i
      isSet(i) && request != values(tile.i).flatMap(_.request)
    }
  }