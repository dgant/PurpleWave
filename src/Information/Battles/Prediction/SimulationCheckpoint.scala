package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{Simulacrum, Simulation}
import Lifecycle.With
import Mathematics.Maff

class SimulationCheckpoint(simulation: Simulation, previous: Option[SimulationCheckpoint]) {

  @inline final def ValueMax(simulacrum: Simulacrum)            : Double  = simulacrum.subjectiveValue
  @inline final def ValueLeft(simulacrum: Simulacrum)           : Double  = if (simulacrum.dead) 0.0 else simulacrum.subjectiveValue
  @inline final def unitHealthLeft(simulacrum: Simulacrum)      : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.hitPoints
  @inline final def unitHealthMax(simulacrum: Simulacrum)       : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.hitPointsInitial
  @inline final def unitHealthValueLeft(simulacrum: Simulacrum) : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.subjectiveValue * simulacrum.hitPoints + simulacrum.shieldPoints
  @inline final def unitHealthValueMax(simulacrum: Simulacrum)  : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.subjectiveValue * simulacrum.hitPointsInitial + simulacrum.shieldPointsInitial

  val gainedValueMultiplier           : Double = simulation.prediction.battle.gainedValueMultiplier
  val framesIn                        : Int    = simulation.prediction.simulationFrames
  val localValueTotalUs               : Double = simulation.simulacraOurs.view.map(ValueMax).sum
  val localValueTotalEnemy            : Double = simulation.simulacraEnemy.view.map(ValueMax).sum * gainedValueMultiplier
  val localValueLeftUs                : Double = simulation.simulacraOurs.view.map(ValueLeft).sum
  val localValueLeftEnemy             : Double = simulation.simulacraEnemy.view.map(ValueLeft).sum * gainedValueMultiplier
  val localValueLostUs                : Double = localValueTotalUs - localValueLeftUs
  val localValueLostEnemy             : Double = localValueTotalEnemy - localValueLeftEnemy
  val localValueLostRatio             : Double = Maff.nanToZero(2 * localValueLostEnemy / (localValueLostUs + localValueLostEnemy) - 1)
  val localValueLostUsHere            : Double = localValueLostUs - previous.map(_.localValueLostUs).getOrElse(0.0)
  val localValueLostEnemyHere         : Double = localValueLostEnemy - previous.map(_.localValueLostEnemy).getOrElse(0.0)
  val localValueDecisiveness          : Double = Math.max(Maff.nanToZero(localValueLostUsHere / localValueTotalUs), Maff.nanToZero(localValueLostEnemyHere / localValueTotalEnemy))
  val localHealthTotalUs              : Double = simulation.simulacraOurs.view.map(unitHealthMax).sum
  val localHealthTotalEnemy           : Double = simulation.simulacraEnemy.view.map(unitHealthMax).sum * gainedValueMultiplier
  val localHealthLeftUs               : Double = simulation.simulacraOurs.view.map(unitHealthLeft).sum
  val localHealthLeftEnemy            : Double = simulation.simulacraEnemy.view.map(unitHealthLeft).sum * gainedValueMultiplier
  val localHealthLostUs               : Double = localHealthTotalUs - localHealthLeftUs
  val localHealthLostEnemy            : Double = localHealthTotalEnemy - localHealthLeftEnemy
  val localHealthLostRatio            : Double = Maff.nanToZero(2 * localHealthLostEnemy / (localHealthLostUs + localHealthLostEnemy) - 1)
  val localHealthLostUsHere           : Double = localHealthLostUs - previous.map(_.localHealthLostUs).getOrElse(0.0)
  val localHealthLostEnemyHere        : Double = localHealthLostEnemy - previous.map(_.localHealthLostEnemy).getOrElse(0.0)
  val localHealthDecisiveness         : Double = Math.max(Maff.nanToZero(localHealthLostUsHere / localHealthTotalUs), Maff.nanToZero(localHealthLostEnemyHere / localHealthTotalEnemy))
  val localHealthValueTotalUs         : Double = simulation.simulacraOurs.view.map(unitHealthValueMax).sum
  val localHealthValueTotalEnemy      : Double = simulation.simulacraEnemy.view.map(unitHealthValueMax).sum * gainedValueMultiplier
  val localHealthValueLeftUs          : Double = simulation.simulacraOurs.view.map(unitHealthValueLeft).sum
  val localHealthValueLeftEnemy       : Double = simulation.simulacraEnemy.view.map(unitHealthValueLeft).sum * gainedValueMultiplier
  val localHealthValueLostUs          : Double = localHealthValueTotalUs - localHealthValueLeftUs
  val localHealthValueLostEnemy       : Double = localHealthValueTotalEnemy - localHealthValueLeftEnemy
  val localHealthValueLostUsHere      : Double = localHealthValueLostUs - previous.map(_.localHealthValueLostUs).getOrElse(0.0)
  val localHealthValueLostEnemyHere   : Double = localHealthValueLostEnemy - previous.map(_.localHealthValueLostEnemy).getOrElse(0.0)
  val localHealthValueDecisiveness    : Double = Math.max(Maff.nanToZero(localHealthValueLostUsHere / localHealthValueTotalUs), Maff.nanToZero(localHealthValueLostEnemyHere / localHealthValueTotalEnemy))
  val localHealthValueLostRatio       : Double = Maff.nanToZero(2 * localHealthValueLostEnemy / (localHealthValueLostUs + localHealthValueLostEnemy) - 1)
  val ratioLocalValueLostUs           : Double = Maff.nanToZero(localValueLostUs / localValueTotalUs)
  val ratioLocalValueLostEnemy        : Double = Maff.nanToZero(localValueLostEnemy / localValueTotalEnemy)
  val ratioLocalValueLostNet          : Double = ratioLocalValueLostEnemy - ratioLocalValueLostUs
  val ratioLocalHealthLostUs          : Double = Maff.nanToZero(localHealthLostUs / localHealthTotalUs)
  val ratioLocalHealthLostEnemy       : Double = Maff.nanToZero(localHealthLostEnemy / localHealthTotalEnemy)
  val ratioLocalHealthLostNet         : Double = ratioLocalHealthLostEnemy - ratioLocalHealthLostUs
  val ratioLocalHealthValueLostUs     : Double = Maff.nanToZero(localHealthValueLostUs / localHealthValueTotalUs)
  val ratioLocalHealthValueLostEnemy  : Double = Maff.nanToZero(localHealthValueLostEnemy / localHealthValueTotalEnemy)
  val ratioLocalHealthValueLostNet    : Double = ratioLocalHealthValueLostEnemy - ratioLocalHealthValueLostUs
  val weightLife = 1.0
  val weightHealth = 0.1
  val totalScore: Double = if (With.performance.danger)
    Maff.weightedMean(Seq(
      (localValueLostRatio,           weightLife),
      (localHealthValueLostRatio,     weightHealth)))
    else
      Maff.weightedMean(Seq(
        (localValueLostRatio,           weightLife),
        //(localHealthLostRatio,          weightHealth),
        (localHealthValueLostRatio,     weightHealth),
        (ratioLocalValueLostNet,        weightLife),
        (ratioLocalHealthLostNet,       weightHealth),
        (ratioLocalHealthValueLostNet,  weightHealth)))
  val localTotalDecisiveness: Double = Maff.mean(Seq(
    localValueDecisiveness,
    localHealthDecisiveness,
    localHealthValueDecisiveness
  ))
  val cumulativeTotalDecisiveness: Double = previous.map(_.cumulativeTotalDecisiveness).sum + localTotalDecisiveness
}
