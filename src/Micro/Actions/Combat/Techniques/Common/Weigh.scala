package Micro.Actions.Combat.Techniques.Common

import Debugging.Visualizations.Views.Micro.ShowTechniques
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
    
    val allowed = techniques.filter(_.allowed(unit))
    val evaluations = allowed.map(technique => (technique, new ActionTechniqueEvaluation(unit, technique))).toMap
    if (ShowTechniques.inUse) {
      unit.agent.techniques ++= evaluations.values
    }
    ByOption.maxBy(evaluations)(_._2.totalApplicability).map(_._1)
  }
}
