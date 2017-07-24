package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Lifecycle.With

object ShowBattleDetails extends View {
  val x1 = 5
  val x2 = 400
  val y1 = 7 * With.visualization.lineHeightSmall
  val y2 = 200
  
  override def renderScreen() {
    With.game.drawTextScreen(x1, 5 * With.visualization.lineHeightSmall, "Avatar")
    
    renderBattle(
      "Global:",
      With.battles.global.estimationAbstract,
      x1,
      y1)
    
    ShowBattles.localBattle.foreach(battle => {
      renderBattle(
        "Local:",
        battle.estimationGeometricOffense,
        x1,
        y2)
      
      renderBattle(
        "Local:",
        battle.estimationMatchups,
        x2,
        y2)
    })
  }
  
  def renderBattle(title: String, estimation: Estimation, x: Int, y: Int) {
   val table = Vector(
     Vector[String](title, With.self.name, With.enemy.name),
     Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
     Vector[String]("Seconds", "" + estimation.frames / 24, ""),
     Vector[String]("Value", "+" + estimation.costToUs.toInt, "-" + estimation.costToEnemy.toInt, "(" + estimation.netValue.toInt + ")"),
     Vector[String]("Survivors",
       survivorPercentage(estimation.deathsUs,    estimation.avatarUs.totalUnits),
       survivorPercentage(estimation.deathsEnemy, estimation.avatarEnemy.totalUnits)))
    
    DrawScreen.table(x, y, table)
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%%"
  }
}

