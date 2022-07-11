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
          (With.sense.ourMinedMinerals,         With.self.colorBright,  "Our minerals")
        ),
        Seq(
          (With.sense.enemyMinedMinerals,       With.enemy.colorBright, "Foe minerals")
        ),
        Seq(
          (With.sense.ourLostMinerals,          Color.White,            "Our loss"),
          (With.self.minerals,                  Colors.BrightTeal,      "Our float"),
          (With.sense.ourTechMinerals,          Colors.BrightViolet,    "Our tech"),
          (With.sense.ourUpgradeMinerals,       Colors.BrightIndigo,    "Our upgr"),
          (With.sense.ourWorkerMinerals,        Colors.BrightBlue,      "Our workers"),
          (With.sense.ourBaseMinerals,          Colors.BrightTeal,      "Our bases"),
          (With.sense.ourSupplyMinerals,        Colors.BrightGreen,     "Our supply"),
          (With.sense.ourScienceMinerals,       Colors.BrightYellow,    "Our sci"),
          (With.sense.ourDefenseMinerals,       Colors.BrightGray,      "Our def"),
          (With.sense.ourProductionMinerals,    Colors.BrightOrange,    "Our prod"),
          (With.sense.ourWarUnitMinerals,       Colors.BrightRed,       "Our war"),
        ),
        Seq(
          (With.sense.enemyLostMinerals,        Color.White,            "Foe loss"),
          (With.sense.enemySecretMinerals,      Colors.BrightTeal,      "Foe secret"),
          (With.sense.enemyTechMinerals,        Colors.BrightViolet,    "Foe tech"),
          (With.sense.enemyUpgradeMinerals,     Colors.BrightIndigo,    "Foe upgr"),
          (With.sense.enemyWorkerMinerals,      Colors.BrightBlue,      "Foe workers"),
          (With.sense.enemyBaseMinerals,        Colors.BrightTeal,      "Foe bases"),
          (With.sense.enemySupplyMinerals,      Colors.BrightGreen,     "Foe supply"),
          (With.sense.enemyScienceMinerals,     Colors.BrightYellow,    "Foe sci"),
          (With.sense.enemyDefenseMinerals,     Colors.BrightGray,      "Foe def"),
          (With.sense.enemyProductionMinerals,  Colors.BrightOrange,    "Foe prod"),
          (With.sense.enemyWarUnitMinerals,     Colors.BrightRed,       "Foe war"),
        ),
        Seq(
          (With.sense.ourMinedGas,              With.self.colorBright,  "Our gas")
        ),
        Seq(
          (With.sense.enemyMinedGas,            With.enemy.colorBright, "Foe gas")
        ),
        Seq(
          (With.sense.ourLostGas,               Color.White,            "Our loss"),
          (With.self.gas,                       Colors.BrightTeal,      "Our float"),
          (With.sense.ourTechGas,               Colors.BrightViolet,    "Our tech"),
          (With.sense.ourUpgradeGas,            Colors.BrightIndigo,    "Our upgr"),
          (With.sense.ourScienceGas,            Colors.BrightYellow,    "Our sci"),
          (With.sense.ourProductionGas,         Colors.BrightOrange,    "Our prod"),
          (With.sense.ourWarUnitGas,            Colors.BrightRed,       "Our war"),
        ),
        Seq(
          (With.sense.enemyLostGas,             Color.White,            "Foe loss"),
          (With.sense.enemySecretGas,           Colors.BrightTeal,      "Foe secret"),
          (With.sense.enemyTechGas,             Colors.BrightViolet,    "Foe tech"),
          (With.sense.enemyUpgradeGas,          Colors.BrightIndigo,    "Foe upgr"),
          (With.sense.enemyScienceGas,          Colors.BrightYellow,    "Foe sci"),
          (With.sense.enemyProductionGas,       Colors.BrightOrange,    "Foe prod"),
          (With.sense.enemyWarUnitGas,          Colors.BrightRed,       "Foe war"),
        ),
      ))
  }
}
