package Micro.Actions.Combat.Techniques.Common

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ActionTechniqueEvaluator {

  def applicabilitiesOther(
    unit: FriendlyUnitInfo,
    technique: ActionTechnique)
      : Vector[Double] = {
    unit.matchups.others.flatMap(technique.applicabilityOther(unit, _)).map(PurpleMath.clampToOne)
  }

  def totalApplicabilitiesOther(
    unit: FriendlyUnitInfo,
    technique: ActionTechnique)
      : Double = {
    PurpleMath.nanToZero(technique.activator(unit, unit.matchups.others).getOrElse(0.0))
  }

  def totalApplicabilitySelf(
    unit: FriendlyUnitInfo,
    technique: ActionTechnique)
      : Double = {
    PurpleMath.clampToOne(technique.applicabilitySelf(unit))
  }

  def totalApplicability(
    unit: FriendlyUnitInfo,
    technique: ActionTechnique)
      : Double = {

    // For performance
    if (technique.applicabilityBase <= 0.0) return 0.0
    val totalSelf = totalApplicabilitySelf(unit, technique)

    // For performance
    if (totalSelf <= 0.0) return 0.0

    val totalOther = totalApplicabilitiesOther(unit, technique)

    val output = PurpleMath.clampToOne(
      PurpleMath.clampToOne(technique.applicabilityBase)
      * totalSelf
      * totalOther)
    output
  }
}