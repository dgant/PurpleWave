package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Macro.Allocation.TileReservation
import Mathematics.Points.TileRectangle

object ShowArchitecturePlacements extends View {

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
    def drawReservation(reservation: TileReservation, isNew: Boolean): Unit = {
      reservation.tiles.foreach(tile => DrawMap.tileRectangle(TileRectangle(tile, tile.add(1, 1)), if (isNew) Colors.NeonBlue else Colors.MediumBlue))
      DrawMap.tileRectangle(TileRectangle(reservation.target, reservation.target.add(1, 1)), if (isNew) Colors.NeonGreen else Colors.MediumGreen)
      DrawMap.label(reservation.plan.toString, reservation.target.pixelCenter)
    }
    With.groundskeeper.reservationsNow.foreach(drawReservation(_, isNew = true))
    With.groundskeeper.reservationsBefore.foreach(drawReservation(_, isNew = false))
    With.groundskeeper.suggestions.filter(_.tile.isDefined).foreach(suggestion => {
      val tile = suggestion.tile.get
      DrawMap.tileRectangle(TileRectangle(tile, tile.add(1, 1)), Colors.NeonYellow)
      DrawMap.label(suggestion.plan.map(_.toString).getOrElse("X"), tile.pixelCenter)
    })
  }

  def renderArchitectureMap(): Unit = {
    With.architecture.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.areaExcluded, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.areaExcluded.midPixel)
    })

    With.viewport.rectangle().tiles.foreach(tile => {
      if (With.architecture.unbuildable.get(tile)) {
        DrawMap.box(
          tile.topLeftPixel.add(4, 4),
          tile.topLeftPixel.add(28, 28),
          Colors.MediumTeal,
          solid = false)
      }
      if (With.architecture.unwalkable.get(tile)) {
        DrawMap.box(
          tile.topLeftPixel.add(8, 8),
          tile.topLeftPixel.add(24, 24),
          Colors.MediumOrange,
          solid = false)
      }
    })
  }
}
