package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowFormations extends View {
  override def renderMap(): Unit = {
    With.squads.all.foreach(_.formation.foreach(_.renderMap()))
  }
}
