package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Information.Battles.Estimations.Estimation
import Information.Battles.Types.{Battle, Team}
import Lifecycle.With

object ShowBattleDetails extends View {
  val x1 = 5
  val x2 = 215
  val x3 = 425
  lazy val y1: Int = 5 * With.visualization.lineHeightSmall
  val y2 = 220
  
  override def renderScreen() {
    ShowBattles.localBattle.foreach(battle => {
      renderBattle(
        "Simulated:",
        battle,
        battle.estimationSimulation,
        x1,
        y1)
      
      renderBattle(
        "Avatar:",
        battle,
        battle.estimationGeometricOffense,
        x2,
        y1)
      
      renderBattle(
        "Matchups:",
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
      Vector[String]("Participants", "" + estimation.totalUnitsUs, "" + estimation.totalUnitsEnemy),
      Vector[String]("Survivors",
        survivorPercentage(estimation.deathsUs,    estimation.totalUnitsUs),
        survivorPercentage(estimation.deathsEnemy, estimation.totalUnitsEnemy)),
      Vector[String]("Desire", "%1.2f".format(battle.desire)))
    DrawScreen.table(x, y, table)
    
    With.game.drawTextScreen(x1, y2, describeTeam(battle.us))
    With.game.drawTextScreen(x1, y2 + With.visualization.lineHeightSmall, describeTeam(battle.enemy))
  }
  
  def describeTeam(team: Team): String = {
    team.units.groupBy(_.unitClass).toVector.sortBy( - _._2.size).map(p => p._2.size + " " + p._1).mkString(", ")
  }
  
  def survivorPercentage(deaths: Double, total: Double): String = {
    (100 * (total - deaths) / total).toInt + "%%"
  }
}

