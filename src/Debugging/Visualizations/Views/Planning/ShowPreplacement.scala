package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowPreplacement extends View {

  override def renderMap(): Unit = {
    With.preplacement.fits.foreach(_.drawMap())
  }
}
