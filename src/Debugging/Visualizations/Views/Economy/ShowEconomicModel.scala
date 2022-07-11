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
          (With.sense.ourUpgradeMinerals, Colors.BrightYellow, "Our upgr"),
          (With.sense.ourWarUnitMinerals, Colors.BrightRed, "Our war"),
          (With.sense.ourPeaceMinerals, Colors.BrightBlue, "Our peace"),
        ),
        Seq(
          (With.sense.enemyLostMinerals, Color.White, "Foe loss"),
          (With.sense.enemyTechMinerals, Colors.BrightViolet, "Foe tech"),
          (With.sense.enemyUpgradeMinerals, Colors.BrightYellow, "Foe upgr"),
          (With.sense.enemyWarUnitMinerals, Colors.BrightRed, "Foe war"),
          (With.sense.enemyPeaceMinerals, Colors.BrightBlue, "Foe peace"),
          (With.sense.enemySecretMinerals, Colors.BrightGreen, "Foe mystery")
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
          (With.sense.ourUpgradeGas, Colors.BrightYellow, "Our upgr"),
          (With.sense.ourWarUnitGas, Colors.BrightRed, "Our war"),
          (With.sense.ourPeaceGas, Colors.BrightBlue, "Our peace"),
        ),
        Seq(
          (With.sense.enemyLostGas, Color.White, "Foe loss"),
          (With.sense.enemyTechGas, Colors.BrightViolet, "Foe tech"),
          (With.sense.enemyUpgradeGas, Colors.BrightYellow, "Foe upgr"),
          (With.sense.enemyWarUnitGas, Colors.BrightRed, "Foe war"),
          (With.sense.enemyPeaceGas, Colors.BrightBlue, "Foe peace"),
        ),
      ))
  }
}
