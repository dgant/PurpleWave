package Micro.Actions.Combat.Techniques.Common

import Debugging.Visualizations.Views.Micro.ShowTechniques
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Weigh {
  
  def delegate(unit: FriendlyUnitInfo, techniques: ActionTechnique*) {
    best(unit, techniques).foreach(_.delegate(unit))
  }
  
  def consider(unit: FriendlyUnitInfo, techniques: ActionTechnique*) {
    best(unit, techniques).foreach(_.consider(unit))
  }
  
  private def best(
    unit: FriendlyUnitInfo,
    techniques: Iterable[ActionTechnique])
      : Option[ActionTechnique] = {

    val allowed = techniques.view.filter(_.allowed(unit))
    if (ShowTechniques.inUse && (unit.selected || With.viewport.contains(unit.tileIncludingCenter))) {
      unit.agent.techniques ++=
        allowed.map(technique => new ActionTechniqueEvaluation(unit, technique))
    }

    ByOption
      .maxBy(allowed.map(t => (t, ActionTechniqueEvaluator.totalApplicability(unit, t))))(_._2)
      .map(_._1)
  }
}
