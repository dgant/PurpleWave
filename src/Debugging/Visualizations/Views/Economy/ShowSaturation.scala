package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import bwapi.Color

object ShowSaturation extends View {
  override def renderMap(): Unit = {
    val gathererCounts = With.units.ours.view
      .filter(_.agent.toGather.isDefined)
      .groupBy(_.agent.toGather.get)
      .map(p => (p._1, p._2.size))

    val resourcesSaturated = With.units.ours
      .withFilter(unit =>
        unit.velocityX == 0 &&
        unit.velocityY == 0 &&
        unit.target.exists(_.unitClass.isResource))
      .flatMap(_.target)
      .toSet

    With.geography.ourBases
      .view
      .flatMap(base => base.resources)
      .foreach(resource => {
        if (resourcesSaturated.contains(resource)) {
          DrawMap.circle(resource.pixel, 16, Colors.DarkRed, solid = true)
        } else {
          DrawMap.circle(resource.pixel, 16, Color.Black, solid = true)
        }
        DrawMap.label(gathererCounts.getOrElse(resource, 0).toString, resource.pixel)
      })
  }
}
