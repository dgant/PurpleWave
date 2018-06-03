package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.PurpleMath

object ShowRushDistances extends View {
  
  /*
  Known ramp widths
  -----------------
  Andromeda: 160 (because it's cut incorrectly)
  Destination: 30, 28
  Fighting Spirit: 33
  Great Barrier Reef: 36
  Heartbreak Ridge: 40
  Hitchhiker: 44 36
  
  Alchemist: 128 (because it's cut incorrectly)
  Arcadia: 31
  Luna the Final: 38
  Match Point: 92
  Neo Chupung-Ryeong: 32
  Tau Cross: 39
  
  
  Known distances
  ---------------
  Andromeda: 4940 - 5404 - 6083
  Arcadia: 4216 - 5216 - 6008
  Benzene: 6140
  Circuit Breakers: 4396 - 5098 - 5696
  Destination: 5216
  Empire of the Sun: 4211 - 4894 - 5600
  Fighting Spirit: 4563 - 5043 - 5776
  Great Barrier Reef: 4507 - 2679 - 4798
  Heartbreak Ridge: 5066
  Icarus: 4492 - 4900 - 5278
  Jade: 4559 - 5102 - 5634
  La Mancha: 4441 - 4950 - 5715; 4441 - 4998 - 6003
  Luna: 4559 - 5102 - 5634
  Neo Moon Glaive: 4687 - 4750 - 4846
  Python: 4096 -  4458 - 5312
  Roadrunner: 4567 - 4858 - 5402
  Tau Cross: 5433 - 5800 - 6236
  
  Alchemist: 4073 - 4640 - 5151
  Arcadia: 4216 - 5216 - 6008
  Destination 1.1: 6604? 5216?
  Hitchhiker: 4438
  Pathfinder: 4716 - 4767 - 4850
  Luna the Final: 4641 - 5114 - 5841
  Match Point: 6000
  Neo Aztec: 5107 - 5305 - 5484
  Neo Chupung-Ryeong: 4569
  Neo Sniper Ridge: 4640 - 5163 - 5881
  Ride of the Valkyries: ???
  Tau Cross: 5318 - 5538 - 5731
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
          With.mapFileName,
          "",
          distances.min.toInt.toString,
          PurpleMath.mean(distances).toInt.toString,
          distances.max.toInt.toString
        )))
    DrawScreen.table(
      x,
      y + With.visualization.lineHeightSmall,
      Vector(
        Vector(
          "Ramp width",
          "")
        ++ With.geography.ourMain.zone.edges.map(_.radiusPixels.toInt.toString)))
  }
  
}
