package Debugging.Visualizations.Views.Battles

import Debugging.Visualizations.Views.View
import Information.Battles.Types.Battle
import Lifecycle.With
import Mathematics.Maff


object ShowJudgment extends View {
  override def renderScreen(): Unit = {
    ShowBattles.localBattle.foreach(render)
  }

  private def render(battle: Battle): Unit = {
    val x = 540
    val y = 40
    val mx = 2
    val my = 2
    val smax: Int = Maff.max(battle.judgmentModifiers.view.map(_.name.length)).getOrElse(1)
    battle.judgmentModifiers.zipWithIndex.foreach(p => {
      val yb = y + p._2 * 15
      val s = p._1.toString
      val sp = s + " " * (smax - s.length)
      val w = 95
      With.game.drawBoxScreen(x, yb, x + w + 2 * mx + 1, yb + 2 * my + With.visualization.lineHeightSmall + 1, p._1.color, true)
      With.game.drawTextScreen(x + mx, yb + my, sp)
    })
  }
}
