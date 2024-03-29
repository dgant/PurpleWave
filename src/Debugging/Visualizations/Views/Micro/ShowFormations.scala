package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.DebugView
import Lifecycle.With

object ShowFormations extends DebugView {

  override def renderMap(): Unit = {
    With.squads.all.foreach(_.formations.foreach(_.renderMap()))
  }
}
