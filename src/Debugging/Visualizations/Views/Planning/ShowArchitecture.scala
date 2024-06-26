package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import Placement.Architecture.GridExclusion
import bwapi.Color

object ShowArchitecture extends DebugView {

  override def renderMap(): Unit = {
    With.viewport.areaTiles.tiles.foreach(tile => {
      if (tile.valid) {
        val reservation = With.groundskeeper.tileReservations(tile.i)
        if (reservation.renewed) {
          DrawMap.tile(tile, color = Colors.NeonYellow)
          DrawMap.label(reservation.owner.toString, tile.center)
        }
        if (With.architecture.unbuildable.get(tile).isDefined) {
          DrawMap.tile(tile, 12, Colors.DarkRed)
        }
        if (With.architecture.unwalkable.get(tile).isDefined) {
          DrawMap.tile(tile, 10, Colors.DarkGreen)
        }
        if (With.architecture.untownhallable.get(tile).isDefined) {
          DrawMap.tile(tile, 7, Colors.DarkTeal)
        }
        if (With.architecture.ungassable.get(tile).isDefined) {
          DrawMap.tile(tile, 4, Colors.DarkIndigo)
        }
        if (With.architecture.powerFrame2Height.get(tile) <= With.frame) {
          DrawMap.circle(tile.center, 2, Colors.BrightYellow)
        }
        if (With.architecture.powerFrame3Height.get(tile) <= With.frame) {
          DrawMap.circle(tile.center, 4, Colors.BrightTeal)
        }
      }
    })
  }

  def renderArchitectureMap(): Unit = {
    val tiles = With.viewport.areaTiles.tiles.filter(_.valid)

    def drawExclusions(exclusions: GridExclusion, color: Color, radius: Int): Unit = {
      tiles.map(exclusions.get).filter(_.nonEmpty).distinct.foreach(exclusion =>
        DrawMap.box(
          exclusion.get.areaExcluded.startPixel.add(radius, radius),
          exclusion.get.areaExcluded.endPixelInclusive.subtract(radius, radius),
          color))
    }

    drawExclusions(With.architecture.unbuildable, Colors.MediumRed, 4)
    drawExclusions(With.architecture.unwalkable, Colors.MediumGreen, 2)
    drawExclusions(With.architecture.untownhallable, Colors.MediumBlue, 6)
    drawExclusions(With.architecture.ungassable, Colors.MediumYellow, 8)
  }
}
