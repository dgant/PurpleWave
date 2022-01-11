package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowFormations extends View {

  override def renderMap() {
    With.squads.all.foreach(_.formations.foreach(_.renderMap()))
  }
}
