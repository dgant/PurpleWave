package Debugging.Visualizations.Views.Economy

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.DebugView
import Lifecycle.With
import bwapi.Color

object ShowEconomicModel extends DebugView {

  override def renderScreen(): Unit = {
    DrawScreen.barChart(
      Seq(
        Seq(
          (With.sense.ourMinedMinerals, With.self.colorBright, "Our minerals")
        ),
        Seq(
          (With.sense.enemyMinedMinerals, With.enemy.colorBright, "Foe minerals")
        ),
        Seq(
          (With.sense.ourLostMinerals, Color.White, "Our loss"),
          (With.sense.ourTechMinerals, Colors.BrightViolet, "Our tech"),
          (With.sense.ourLostMinerals, Colors.BrightYellow, "Our upgr"),
          (With.sense.ourMinedMinerals, Colors.BrightRed, "Our war"),
          (With.sense.ourMinedMinerals, Colors.BrightBlue, "Our peace"),
        ),
        Seq(
          (With.sense.enemyLostMinerals, Color.White, "Foe loss"),
          (With.sense.enemyTechMinerals, Colors.BrightViolet, "Foe tech"),
          (With.sense.enemyLostMinerals, Colors.BrightYellow, "Foe upgr"),
          (With.sense.enemyMinedMinerals, Colors.BrightRed, "Foe war"),
          (With.sense.enemyMinedMinerals, Colors.BrightBlue, "Foe peace"),
        ),
        Seq(
          (With.sense.ourMinedGas, With.self.colorBright, "Our gas")
        ),
        Seq(
          (With.sense.enemyMinedGas, With.enemy.colorBright, "Foe gas")
        ),
        Seq(
          (With.sense.ourLostGas, Color.White, "Our loss"),
          (With.sense.ourTechGas, Colors.BrightViolet, "Our tech"),
          (With.sense.ourLostGas, Colors.BrightYellow, "Our upgr"),
          (With.sense.ourMinedGas, Colors.BrightRed, "Our war"),
          (With.sense.ourMinedGas, Colors.BrightBlue, "Our peace"),
        ),
        Seq(
          (With.sense.enemyLostGas, Color.White, "Foe loss"),
          (With.sense.enemyTechGas, Colors.BrightViolet, "Foe tech"),
          (With.sense.enemyLostGas, Colors.BrightYellow, "Foe upgr"),
          (With.sense.enemyMinedGas, Colors.BrightRed, "Foe war"),
          (With.sense.enemyMinedGas, Colors.BrightBlue, "Foe peace"),
        ),
      ))
  }
}
