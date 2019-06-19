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
    With.viewport.rectangle().tiles.foreach(tile => {
      if (tile.valid) {
        val reservation = With.groundskeeper.reserved(tile.i)
        if (reservation.update > With.groundskeeper.updates - 1) {
          DrawMap.box(tile.topLeftPixel, tile.bottomRightPixel, Colors.NeonYellow)
          DrawMap.label(reservation.plan.toString, tile.pixelCenter)
        }
      }
    })
    With.groundskeeper.suggestions.filter(_.tile.isDefined).foreach(suggestion => {
      val tile = suggestion.tile.get
      DrawMap.box(tile.topLeftPixel, tile.bottomRightPixel, Colors.NeonOrange)
      DrawMap.label(suggestion.plan.map(_.toString).getOrElse("X"), tile.pixelCenter)
    })
  }

  def renderArchitectureMap(): Unit = {
    val tiles = With.viewport.rectangle().tiles.filter(_.valid)

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
