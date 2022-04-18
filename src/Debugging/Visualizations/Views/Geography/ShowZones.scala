package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowZones extends DebugView {
  
  override def renderMap(): Unit = {
    With.geography.zones.foreach(zone => {
      zone.edges.foreach(edge => {
        val owner = edge.zones.find( ! _.owner.isNeutral).map(_.owner).getOrElse(With.neutral)
        DrawMap.line(
          edge.sidePixels.head,
          edge.sidePixels.last,
          owner.colorDark
        )
      })
    })
  }
}
