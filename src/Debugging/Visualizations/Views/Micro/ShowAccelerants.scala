package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import bwapi.Color

object ShowAccelerants extends View {

  override def renderMap(): Unit = {
    val radius = With.gathering.accelerantPixelRadius
    val minerals =
    With.units.neutral.filter(_.mineralsLeft > 0)
    minerals.foreach(u => DrawMap.box(u.topLeft, u.bottomRight, Color.Teal))
    minerals.filter(With.gathering.getAccelerantPixel(_).isDefined).foreach(mineral => {
      val accelerantPixel = With.gathering.getAccelerantPixel(mineral).get
      DrawMap.circle(accelerantPixel, radius, Color.Red)
      DrawMap.line(mineral.pixel, accelerantPixel.project(mineral.pixel, radius), Color.Red)
    })

    With.gathering.accelerantMinerals.foreach(p =>
      DrawMap.arrow(p._2.pixel, p._1.pixel, Color.Yellow)
    )

    With.units.ours
      .filter(u => u.orderTarget.exists(_.unitClass.isMinerals) && u.agent.intent.toGather.exists(With.gathering.onAccelerant(u, _)))
      .foreach(u => {
        val accelerantPixel = With.gathering.getAccelerantPixel(u.agent.intent.toGather.get).get
        val distance = u.pixelDistanceCenter(accelerantPixel)
        val color = if (distance < 1) Colors.NeonRed else if (distance < 4) Colors.NeonOrange else Colors.NeonYellow
        DrawMap.circle(u.pixel, radius, color, solid = true)
      })
  }
}
