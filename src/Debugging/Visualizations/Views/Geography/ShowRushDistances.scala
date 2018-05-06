package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.PurpleMath

object ShowRushDistances extends View {
  
  /*
  Known distances:
  
  Andromeda: 4940 - 5404 - 6083
  Benzene: 6140
  Circuit Breakers: 4396 - 5098 - 5696
  Destination:
  Empire of the Sun: 4211 - 4894 - 5600
  Fighting Spirit: 4563 - 5043 - 5776
  Heartbreak Ridge: 5066
  Icarus: 4492 - 4900 - 5278
  Jade: 4559 - 5102 - 5634
  La Mancha: 4441 - 4950 - 5715; 4441 - 4998 - 6003
  Luna: 4559 - 5102 - 5634
  Neo Moon Glaive: 4687 - 4750 - 4846
  Python: 4096 -  4458 - 5312
  Roadrunner: 4567 - 4858 - 5402
  Tau Cross: 5433 - 5800 - 6236
  */
  override def renderScreen() {
    val x = 5
    val y = 1 * With.visualization.lineHeightSmall
    val distances = With.geography.rushDistances
    DrawScreen.table(
      x,
      y,
      Vector(
        Vector(
          "Rush: ",
          distances.min.toInt.toString,
          PurpleMath.mean(distances).toInt.toString,
          distances.max.toInt.toString
        )))
  }
  
}
