package Placement.Templating

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Mathematics.Points.{Point, Tile}

case class TemplatePoint(point: Point, requirement: TemplatePointRequirement) {
  def drawMap(origin: Tile): Unit = {
    val tileStart   = origin.add(point)
    val tileEnd     = tileStart.add(requirement.width, requirement.height)
    val pixelStart  = tileStart.topLeftPixel.add(2, 2)
    val pixelEnd    = tileEnd.topLeftPixel.subtract(3, 3)
    val color =
      if (requirement.buildableAfter) Colors.MediumGray
      else if (requirement.walkableAfter) Colors.NeonRed
      else if (requirement.buildings.nonEmpty) Colors.NeonBlue
      else if (requirement.buildableBefore) Colors.NeonGreen
      else Colors.MediumViolet
    DrawMap.box(pixelStart, pixelEnd, color)
    if (requirement.width > 1) {
      DrawMap.labelBox(requirement.toString.lines, pixelStart.midpoint(pixelEnd))
    }
  }
}
