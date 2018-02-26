package Debugging.Visualizations.Views.Planning

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowHistory extends View {
  
  override def renderScreen(): Unit = {
    DrawScreen.column(5,
      With.visualization.lineHeightSmall * 7
      , With.history.games.toVector.sortBy(-_.timestamp).take(30).map(_.toString))
  }
  
}
