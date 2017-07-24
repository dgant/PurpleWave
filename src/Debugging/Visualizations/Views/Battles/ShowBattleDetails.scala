package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Lifecycle.With

object ShowBattleDetails extends View {

  override def renderScreen() {
    renderBattle(
      "Global:",
      With.battles.global.estimationAbstract,
      5 * With.visualization.lineHeightSmall)
    
    ShowBattles.localBattle.foreach(battle =>
      renderBattle(
        "Local:",
        battle.estimationGeometricOffense,
        200))
  }
  
  def renderBattle(title: String, estimation: Estimation, y: Int) {
   val table = Vector(
     Vector[String](title, With.self.name, With.enemy.name),
     Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
     Vector[String]("Seconds", "" + estimation.frames / 24, ""),
     Vector[String]("Value", "+" + estimation.costToUs.toInt, "-" + estimation.costToEnemy.toInt, "(" + estimation.netValue.toInt + ")"),
     Vector[String]("Survivors",
       survivorPercentage(estimation.deathsUs,    estimation.avatarUs.totalUnits),
       survivorPercentage(estimation.deathsEnemy, estimation.avatarEnemy.totalUnits)),
     Vector[String]("Value", "" + estimation.damageToUs.toInt, "" + estimation.damageToEnemy.toInt))
    
    DrawScreen.table(5, y, table)
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%"
  }
}

