package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Information.Geography.Types.Base
import Lifecycle.With

object ShowExpansions extends DebugView {
  override def renderMap(): Unit = {
    renderExpansions(With.geography.preferredExpansionsOurs,  isOurs = true)
    renderExpansions(With.geography.preferredExpansionsEnemy, isOurs = false)
  }

  private def renderExpansions(expos: Vector[Base], isOurs: Boolean): Unit = {
    expos.zipWithIndex.foreach(p => {
      DrawMap.label(
        f"${if (isOurs)"Our" else "Enemy"} expansion #${p._2 + 1}",
        p._1.townHallArea.topCenterPixel.subtract(0, With.visualization.lineHeightSmall * (if (isOurs) 3 else 5) / 2))
    })
  }
}
