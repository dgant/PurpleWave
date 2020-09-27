package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{Simulacrum, Simulation}
import Lifecycle.With
import Mathematics.PurpleMath

class LocalBattleMetrics(simulation: Simulation, previous: Option[LocalBattleMetrics]) {

  @inline final def ValueMax(simulacrum: Simulacrum)            : Double  = simulacrum.value
  @inline final def ValueLeft(simulacrum: Simulacrum)           : Double  = if (simulacrum.dead) 0.0 else simulacrum.value
  @inline final def unitHealthLeft(simulacrum: Simulacrum)      : Double  = if (simulacrum.nonCombat) 0.0 else simulacrum.hitPoints
  @inline final def unitHealthMax(simulacrum: Simulacrum)       : Double  = if (simulacrum.nonCombat) 0.0 else simulacrum.hitPointsInitial
  @inline final def unitHealthValueLeft(simulacrum: Simulacrum) : Double  = if (simulacrum.nonCombat) 0.0 else simulacrum.value * simulacrum.hitPoints + simulacrum.shieldPoints
  @inline final def unitHealthValueMax(simulacrum: Simulacrum)  : Double  = if (simulacrum.nonCombat) 0.0 else simulacrum.value * simulacrum.hitPointsInitial + simulacrum.shieldPointsInitial

  val framesIn                        : Int    = simulation.prediction.frames
  val localValueTotalUs               : Double = simulation.unitsOurs.view.map(ValueMax).sum
  val localValueTotalEnemy            : Double = simulation.unitsEnemy.view.map(ValueMax).sum
  val localValueLeftUs                : Double = simulation.unitsOurs.view.map(ValueLeft).sum
  val localValueLeftEnemy             : Double = simulation.unitsEnemy.view.map(ValueLeft).sum
  val localValueLostUs                : Double = localValueTotalUs - localValueLeftUs
  val localValueLostEnemy             : Double = localValueTotalEnemy - localValueLeftEnemy
  val localValueLostRatio             : Double = PurpleMath.nanToZero(2 * localValueLostEnemy / (localValueLostUs + localValueLostEnemy) - 1)
  val localValueLostUsHere            : Double = localValueLostUs - previous.map(_.localValueLostUs).getOrElse(0.0)
  val localValueLostEnemyHere         : Double = localValueLostEnemy - previous.map(_.localValueLostEnemy).getOrElse(0.0)
  val localValueDecisiveness          : Double = PurpleMath.nanToZero((localValueLostUsHere + localValueLostEnemyHere) / (localValueTotalUs + localValueTotalEnemy))
  val localHealthTotalUs              : Double = simulation.unitsOurs.view.map(unitHealthMax).sum
  val localHealthTotalEnemy           : Double = simulation.unitsEnemy.view.map(unitHealthMax).sum
  val localHealthLeftUs               : Double = simulation.unitsOurs.view.map(unitHealthLeft).sum
  val localHealthLeftEnemy            : Double = simulation.unitsEnemy.view.map(unitHealthLeft).sum
  val localHealthLostUs               : Double = localHealthTotalUs - localHealthLeftUs
  val localHealthLostEnemy            : Double = localHealthTotalEnemy - localHealthLeftEnemy
  val localHealthLostRatio            : Double = PurpleMath.nanToZero(2 * localHealthLostEnemy / (localHealthLostUs + localHealthLostEnemy) - 1)
  val localHealthLostUsHere           : Double = localHealthLostUs - previous.map(_.localHealthLostUs).getOrElse(0.0)
  val localHealthLostEnemyHere        : Double = localHealthLostEnemy - previous.map(_.localHealthLostEnemy).getOrElse(0.0)
  val localHealthDecisiveness         : Double = PurpleMath.nanToZero((localHealthLostUsHere + localHealthLostEnemyHere) / (localHealthTotalUs + localHealthTotalEnemy))
  val localHealthValueTotalUs         : Double = simulation.unitsOurs.view.map(unitHealthValueMax).sum
  val localHealthValueTotalEnemy      : Double = simulation.unitsEnemy.view.map(unitHealthValueMax).sum
  val localHealthValueLeftUs          : Double = simulation.unitsOurs.view.map(unitHealthValueLeft).sum
  val localHealthValueLeftEnemy       : Double = simulation.unitsEnemy.view.map(unitHealthValueLeft).sum
  val localHealthValueLostUs          : Double = localHealthValueTotalUs - localHealthValueLeftUs
  val localHealthValueLostEnemy       : Double = localHealthValueTotalEnemy - localHealthValueLeftEnemy
  val localHealthValueLostUsHere      : Double = localHealthValueLostUs - previous.map(_.localHealthValueLostUs).getOrElse(0.0)
  val localHealthValueLostEnemyHere   : Double = localHealthValueLostEnemy - previous.map(_.localHealthValueLostEnemy).getOrElse(0.0)
  val localHealthValueDecisiveness    : Double = PurpleMath.nanToZero((localHealthValueLostUsHere + localHealthValueLostEnemyHere) / (localHealthValueTotalUs + localHealthValueTotalEnemy))
  val localHealthValueLostRatio       : Double = PurpleMath.nanToZero(2 * localHealthValueLostEnemy / (localHealthValueLostUs + localHealthValueLostEnemy) - 1)
  val ratioLocalValueLostUs           : Double = PurpleMath.nanToZero(localValueLostUs / localValueTotalUs)
  val ratioLocalValueLostEnemy        : Double = PurpleMath.nanToZero(localValueLostEnemy / localValueTotalEnemy)
  val ratioLocalValueLostNet          : Double = ratioLocalValueLostEnemy - ratioLocalValueLostUs
  val ratioLocalHealthLostUs          : Double = PurpleMath.nanToZero(localHealthLostUs / localHealthTotalUs)
  val ratioLocalHealthLostEnemy       : Double = PurpleMath.nanToZero(localHealthLostEnemy / localHealthTotalEnemy)
  val ratioLocalHealthLostNet         : Double = ratioLocalHealthLostEnemy - ratioLocalHealthLostUs
  val ratioLocalHealthValueLostUs     : Double = PurpleMath.nanToZero(localHealthValueLostUs / localHealthValueTotalUs)
  val ratioLocalHealthValueLostEnemy  : Double = PurpleMath.nanToZero(localHealthValueLostEnemy / localHealthValueTotalEnemy)
  val ratioLocalHealthValueLostNet    : Double = ratioLocalHealthValueLostEnemy - ratioLocalHealthValueLostUs
  val weightLife = 1.0
  val weightHealth = 0.1
  val totalScore: Double = if (With.performance.danger)
    PurpleMath.weightedMean(Seq(
      (localValueLostRatio,           weightLife),
      (localHealthValueLostRatio,     weightHealth)))
    else
      PurpleMath.weightedMean(Seq(
        (localValueLostRatio,           weightLife),
        //(localHealthLostRatio,          weightHealth),
        (localHealthValueLostRatio,     weightHealth),
        (ratioLocalValueLostNet,        weightLife),
        (ratioLocalHealthLostNet,       weightHealth),
        (ratioLocalHealthValueLostNet,  weightHealth)))
  val localTotalDecisiveness: Double = PurpleMath.mean(Seq(
    localValueDecisiveness,
    localHealthDecisiveness,
    localHealthValueDecisiveness
  ))
  val cumulativeTotalDecisiveness: Double = previous.map(_.cumulativeTotalDecisiveness).sum + localTotalDecisiveness
}
