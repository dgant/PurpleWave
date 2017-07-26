package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Information.Battles.Types.Battle
import Lifecycle.With

object ShowBattleDetails extends View {
  val x1 = 5
  val x2 = 215
  val x3 = 425
  lazy val y1: Int = 7 * With.visualization.lineHeightSmall
  
  override def renderScreen() {
    With.game.drawTextScreen(x1, 5 * With.visualization.lineHeightSmall, "Avatar")
    
    renderBattle(
      "Global:",
      With.battles.global,
      With.battles.global.estimationAbstract,
      x1,
      y1)
    
    ShowBattles.localBattle.foreach(battle => {
      renderBattle(
        "Local:",
        battle,
        battle.estimationGeometricOffense,
        x2,
        y1)
      
      renderBattle(
        "Local:",
        battle,
        battle.estimationMatchups,
        x3,
        y1)
    })
  }
  
  def renderBattle(title: String, battle: Battle, estimation: Estimation, x: Int, y: Int) {
   val table = Vector(
     Vector[String](title, With.self.name, With.enemy.name),
     Vector[String]("", if (estimation.weSurvive) "Survives" else "", if (estimation.enemySurvives) "Survives" else ""),
     Vector[String]("Seconds", "" + estimation.frames / 24, ""),
     Vector[String]("Value", "" + estimation.costToEnemy.toInt, "" + estimation.costToUs.toInt),
     Vector[String]("Survivors",
       survivorPercentage(estimation.deathsUs,    estimation.totalUnitsUs),
       survivorPercentage(estimation.deathsEnemy, estimation.totalUnitsEnemy)),
     Vector[String]("Desire", "%1.2f".format(battle.desire)))
    
    DrawScreen.table(x, y, table)
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%%"
  }
}

