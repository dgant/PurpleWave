package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Views.View
import Information.Battles.Types.BattleLocal
import Lifecycle.With

object ShowJudgment extends View {
  override def renderScreen(): Unit = {
    ShowBattles.localBattle.foreach(render)
  }

  private def render(battle: BattleLocal): Unit = {
    val x = 5
    val y = 40
    val mx = 2
    val my = 2
    battle.judgmentModifiers.zipWithIndex.foreach(p => {
      val yb = y + p._2 * 15
      val s = p._1.toString
      val w = s.length * 9 / 2
      With.game.drawBoxScreen(x, yb, x + w + 2 * mx, yb + 2 * my + With.visualization.lineHeightSmall, p._1.color, true)
      With.game.drawTextScreen(x + mx, yb + my, s)
    })
  }
}
