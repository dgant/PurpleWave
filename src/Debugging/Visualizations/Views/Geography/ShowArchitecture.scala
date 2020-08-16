package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Macro.Architecture.GridExclusion
import bwapi.Color

object ShowArchitecture extends View {

  override def renderScreen(): Unit = {
    renderGroundskeeperScreen()
  }

  override def renderMap(): Unit = {
    renderGroundskeeperMap()
    renderArchitectureMap()
  }

  def renderGroundskeeperScreen(): Unit = {
    DrawScreen.column(5, 40,
      With.groundskeeper.suggestions.filter(_.tile.isEmpty).map(s =>s.blueprint.toString + s.plan.map(_.toString).getOrElse("")))
  }

  def renderGroundskeeperMap(): Unit = {
    With.viewport.rectangleTight().tiles.foreach(tile => {
      if (tile.valid) {
        val reservation = With.groundskeeper.reserved(tile.i)
        if (reservation.active) {
          DrawMap.tile(tile, color = Colors.NeonYellow)
          DrawMap.label(reservation.plan.toString, tile.pixelCenter)
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
        if (With.architecture.powered2Height.get(tile)) {
          //DrawMap.circle(tile.pixelCenter, 4, Colors.BrightYellow)
        }
        if (With.architecture.powered3Height.get(tile)) {
          DrawMap.circle(tile.pixelCenter, 4, Colors.BrightTeal)
        }


      }
    })
    With.groundskeeper.suggestions.filter(_.tile.isDefined).foreach(suggestion => {
      val tile = suggestion.tile.get
      DrawMap.tileRectangle(suggestion.blueprint.relativeBuildArea.add(tile), Colors.MediumBlue)
      DrawMap.label(suggestion.plan.map(_.toString).getOrElse("X"), tile.pixelCenter)
    })
  }

  def renderArchitectureMap(): Unit = {
    val tiles = With.viewport.rectangleTight().tiles.filter(_.valid)

    def drawExclusions(exclusions: GridExclusion, color: Color, radius: Int): Unit = {
      tiles.map(exclusions.get).filter(_.nonEmpty).distinct.foreach(exclusion =>
        DrawMap.box(
          exclusion.get.areaExcluded.startPixel.add(radius, radius),
          exclusion.get.areaExcluded.endPixel.subtract(radius, radius),
          color))
    }

    drawExclusions(With.architecture.unbuildable, Colors.MediumRed, 4)
    drawExclusions(With.architecture.unwalkable, Colors.MediumGreen, 2)
    drawExclusions(With.architecture.untownhallable, Colors.MediumBlue, 6)
    drawExclusions(With.architecture.ungassable, Colors.MediumYellow, 8)
  }
}
