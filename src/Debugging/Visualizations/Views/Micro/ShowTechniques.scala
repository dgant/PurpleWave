package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawScreen
import Debugging.Visualizations.Views.View
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShowTechniques extends View {
  
  override def renderScreen() {
    With.units.ours.find(_.selected).foreach(renderUnit)
  }
  
  def renderUnit(unit: FriendlyUnitInfo) {
    val techniques = unit.agent.techniques.toArray.sortBy(-_.totalApplicability)
    DrawScreen.table(
      5,
      7 * With.visualization.lineHeightSmall,
      Vector(Vector("Technique", "Activator", "Total", "Base", "Self", "Others")) ++
      techniques.map(t =>
        Vector(
          t.technique.getClass.getSimpleName.replaceAllLiterally("$", ""),
          t.technique.activator.getClass.getSimpleName.replaceAllLiterally("$", ""),
          format(t.totalApplicability),
          format(t.technique.applicabilityBase),
          format(t.totalApplicabilitySelf),
          format(t.totalApplicabilityOther)
      )))
  }
  
  def format(value: Double): String = "%1.2f".format(value)
}
