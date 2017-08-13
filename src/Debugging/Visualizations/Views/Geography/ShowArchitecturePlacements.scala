package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowArchitecturePlacements extends View {
  
  override def renderScreen() {
    val headers = Vector("Blueprint", "", "", "", "", "Tile", "Minutes", "Frames", "MS", "Considered", "Evaluated")
    
    val table =
      With.groundskeeper.placementArchive
        .values
        .toVector
        .sortBy(-_.totalNanoseconds)
        .map(placement =>
          Vector(
            placement.blueprint,
            "",
            "",
            "",
            "",
            placement.tile.map(_.toString).getOrElse(""),
            placement.frameStarted / 24 / 60,
            placement.frameFinished - placement.frameStarted,
            placement.totalNanoseconds / 1000000,
            placement.candidates,
            placement.evaluated)
          .map(_.toString))
    
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall, Vector(headers) ++ table)
  }
  override def renderMap() {
  
    With.architecture.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.areaExcluded, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.areaExcluded.midPixel)
    })
  
    With.architecture.unwalkable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(8, 8),
      tile.topLeftPixel.add(24, 24),
      Colors.MediumOrange,
      solid = false))
  
    With.architecture.unbuildable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(4, 4),
      tile.topLeftPixel.add(28, 28),
      Colors.MediumTeal,
      solid = false))
  }
}
