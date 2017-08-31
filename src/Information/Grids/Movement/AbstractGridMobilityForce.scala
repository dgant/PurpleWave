package Information.Grids.Movement

import Information.Grids.AbstractGrid
import Mathematics.Physics.{ForceMath, Force}
import Mathematics.Points.Tile
import Mathematics.PurpleMath

abstract class AbstractGridMobilityForce extends AbstractGrid[Force] {
  
  protected def underlyingGrid: AbstractGrid[Int]
  
  override def defaultValue: Force = new Force
  
  override def get(i: Int): Force = {
    val tile          = new Tile(i)
    val forces        = tile.adjacent8.map(neighbor => singleMobilityForce(tile, neighbor))
    val forceTotal    = ForceMath.sum(forces)
    val forceNormal   = forceTotal.normalize
    forceNormal
  }
  
  protected def singleMobilityForce(here: Tile, there: Tile): Force = {
    val mobilityHere  = if (here.valid)   underlyingGrid.get(here)  else 0
    val mobilityThere = if (there.valid)  underlyingGrid.get(there) else 0
    val magnitude     = PurpleMath.signum(mobilityHere - mobilityThere)
    val output        = ForceMath.fromPixels(there.pixelCenter, here.pixelCenter, magnitude.toInt)
    output
  }
}
