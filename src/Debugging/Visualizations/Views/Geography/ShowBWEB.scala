package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Views.View
import jbweb.JBWEB

object ShowBWEB extends View {

  override def renderMap(): Unit = {
    JBWEB.draw()
  }
}
