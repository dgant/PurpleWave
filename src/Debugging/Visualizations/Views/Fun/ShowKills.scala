package Debugging.Visualizations.Views.Fun

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.Players.Players

class ShowKills extends View {
  
  override def renderMap() {
    With.units.all
  }
  
  override def renderScreen() {
    val classes = With.damageCredit.value.flatMap(_._2.keys)
    val players = Players.all.filterNot(_.isNeutral).toVector.sortBy(_.isEnemy).sortBy( ! _.isUs)
    val values  = classes.toSeq.map(c => Vector(c.toString) ++ players.map(With.damageCredit.value).map(_(c).toInt.toString))
    
    val table: Seq[Seq[String]] = Seq(players.map(_.name)) ++ values
    
    DrawScreen.table(5, 7 * With.visualization.lineHeightSmall, table)
  }
}
