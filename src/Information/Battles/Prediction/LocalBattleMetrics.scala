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

  val framesIn                             : Int    = simulation.prediction.frames
  lazy val localValueTotalUs               : Double = simulation.unitsOurs.view.map(ValueMax).sum
  lazy val localValueTotalEnemy            : Double = simulation.unitsEnemy.view.map(ValueMax).sum
  lazy val localValueLeftUs                : Double = simulation.unitsOurs.view.map(ValueLeft).sum
  lazy val localValueLeftEnemy             : Double = simulation.unitsEnemy.view.map(ValueLeft).sum
  lazy val localValueLostUs                : Double = localValueTotalUs - localValueLeftUs
  lazy val localValueLostEnemy             : Double = localValueTotalEnemy - localValueLeftEnemy
  lazy val localValueLostRatio             : Double = PurpleMath.nanToZero(2 * localValueLostEnemy / (localValueLostUs + localValueLostEnemy) - 1)
  lazy val localValueLostUsHere            : Double = localValueLostUs - previous.map(_.localValueLostUs).getOrElse(0.0)
  lazy val localValueLostEnemyHere         : Double = localValueLostEnemy - previous.map(_.localValueLostEnemy).getOrElse(0.0)
  lazy val localValueDecisiveness          : Double = PurpleMath.nanToZero((localValueLostUsHere + localValueLostEnemyHere) / (localValueTotalUs + localValueTotalEnemy))
  lazy val localHealthTotalUs              : Double = simulation.unitsOurs.view.map(unitHealthMax).sum
  lazy val localHealthTotalEnemy           : Double = simulation.unitsEnemy.view.map(unitHealthMax).sum
  lazy val localHealthLeftUs               : Double = simulation.unitsOurs.view.map(unitHealthLeft).sum
  lazy val localHealthLeftEnemy            : Double = simulation.unitsEnemy.view.map(unitHealthLeft).sum
  lazy val localHealthLostUs               : Double = localHealthTotalUs - localHealthLeftUs
  lazy val localHealthLostEnemy            : Double = localHealthTotalEnemy - localHealthLeftEnemy
  lazy val localHealthLostRatio            : Double = PurpleMath.nanToZero(2 * localHealthLostEnemy / (localHealthLostUs + localHealthLostEnemy) - 1)
  lazy val localHealthLostUsHere           : Double = localHealthLostUs - previous.map(_.localHealthLostUs).getOrElse(0.0)
  lazy val localHealthLostEnemyHere        : Double = localHealthLostEnemy - previous.map(_.localHealthLostEnemy).getOrElse(0.0)
  lazy val localHealthDecisiveness         : Double = PurpleMath.nanToZero((localHealthLostUsHere + localHealthLostEnemyHere) / (localHealthTotalUs + localHealthTotalEnemy))
  lazy val localHealthValueTotalUs         : Double = simulation.unitsOurs.view.map(unitHealthValueMax).sum
  lazy val localHealthValueTotalEnemy      : Double = simulation.unitsEnemy.view.map(unitHealthValueMax).sum
  lazy val localHealthValueLeftUs          : Double = simulation.unitsOurs.view.map(unitHealthValueLeft).sum
  lazy val localHealthValueLeftEnemy       : Double = simulation.unitsEnemy.view.map(unitHealthValueLeft).sum
  lazy val localHealthValueLostUs          : Double = localHealthValueTotalUs - localHealthValueLeftUs
  lazy val localHealthValueLostEnemy       : Double = localHealthValueTotalEnemy - localHealthValueLeftEnemy
  lazy val localHealthValueLostUsHere      : Double = localHealthValueLostUs - previous.map(_.localHealthValueLostUs).getOrElse(0.0)
  lazy val localHealthValueLostEnemyHere   : Double = localHealthValueLostEnemy - previous.map(_.localHealthValueLostEnemy).getOrElse(0.0)
  lazy val localHealthValueDecisiveness    : Double = PurpleMath.nanToZero((localHealthValueLostUsHere + localHealthValueLostEnemyHere) / (localHealthValueTotalUs + localHealthValueTotalEnemy))
  lazy val localHealthValueLostRatio       : Double = PurpleMath.nanToZero(2 * localHealthValueLostEnemy / (localHealthValueLostUs + localHealthValueLostEnemy) - 1)
  lazy val ratioLocalValueLostUs           : Double = PurpleMath.nanToZero(localValueLostUs / localValueTotalUs)
  lazy val ratioLocalValueLostEnemy        : Double = PurpleMath.nanToZero(localValueLostEnemy / localValueTotalEnemy)
  lazy val ratioLocalValueLostNet          : Double = ratioLocalValueLostEnemy - ratioLocalValueLostUs
  lazy val ratioLocalHealthLostUs          : Double = PurpleMath.nanToZero(localHealthLostUs / localHealthTotalUs)
  lazy val ratioLocalHealthLostEnemy       : Double = PurpleMath.nanToZero(localHealthLostEnemy / localHealthTotalEnemy)
  lazy val ratioLocalHealthLostNet         : Double = ratioLocalHealthLostEnemy - ratioLocalHealthLostUs
  lazy val ratioLocalHealthValueLostUs     : Double = PurpleMath.nanToZero(localHealthValueLostUs / localHealthValueTotalUs)
  lazy val ratioLocalHealthValueLostEnemy  : Double = PurpleMath.nanToZero(localHealthValueLostEnemy / localHealthValueTotalEnemy)
  lazy val ratioLocalHealthValueLostNet    : Double = ratioLocalHealthValueLostEnemy - ratioLocalHealthValueLostUs
  val weightLife = 1.0
  val weightHealth = 0.1
  lazy val totalScore: Double = if (With.performance.danger)
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
  lazy val totalDecisiveness: Double = PurpleMath.mean(Seq(
    localValueDecisiveness,
    localHealthDecisiveness,
    localHealthValueDecisiveness
  ))
}
