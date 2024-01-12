package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{Simulacrum, Simulation}
import Lifecycle.With
import Mathematics.Maff

class SimulationCheckpoint(simulation: Simulation, previous: Option[SimulationCheckpoint]) {

  @inline final def valueMax            (simulacrum: Simulacrum)  : Double  = simulacrum.subjectiveValue
  @inline final def valueLeft           (simulacrum: Simulacrum)  : Double  = if (simulacrum.dead) 0.0 else simulacrum.subjectiveValue
  @inline final def unitHealthLeft      (simulacrum: Simulacrum)  : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.hitPoints
  @inline final def unitHealthMax       (simulacrum: Simulacrum)  : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.hitPointsInitial
  @inline final def unitHealthValueLeft (simulacrum: Simulacrum)  : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.subjectiveValue * simulacrum.hitPoints + simulacrum.shieldPoints
  @inline final def unitHealthValueMax  (simulacrum: Simulacrum)  : Double  = if ( ! simulacrum.measureHealth) 0.0 else simulacrum.subjectiveValue * simulacrum.hitPointsInitial + simulacrum.shieldPointsInitial
  
  val us  = simulation.realUnitsOurs
  val foe = simulation.realUnitsEnemy

  val framesIn                  : Int    = simulation.battle.simulationFrames
  val valueTotalUs              : Double = CPCount(us,   valueMax)
  val valueTotalEnemy           : Double = CPCount(foe,  valueMax)
  val valueLeftUs               : Double = CPCount(us,   valueLeft)
  val valueLeftEnemy            : Double = CPCount(foe,  valueLeft)
  val valueLostUs               : Double = valueTotalUs    - valueLeftUs
  val valueLostEnemy            : Double = valueTotalEnemy - valueLeftEnemy
  val valueLostRatio            : Double = Maff.nanToZero(2 * valueLostEnemy / (valueLostUs + valueLostEnemy) - 1)
  val valueLostUsHere           : Double = valueLostUs     - previous.map(_.valueLostUs)    .getOrElse(0.0)
  val valueLostEnemyHere        : Double = valueLostEnemy  - previous.map(_.valueLostEnemy) .getOrElse(0.0)
  val valueDecisiveness         : Double = Math.max(Maff.nanToZero(valueLostUsHere / valueTotalUs), Maff.nanToZero(valueLostEnemyHere / valueTotalEnemy))
  val healthMaxUs               : Double = CPCount(us,   unitHealthMax)
  val healthMaxEnemy            : Double = CPCount(foe,  unitHealthMax)
  val healthLeftUs              : Double = CPCount(us,   unitHealthLeft)
  val healthLeftEnemy           : Double = CPCount(foe,  unitHealthLeft)
  val healthLostUs              : Double = healthMaxUs     - healthLeftUs
  val healthLostEnemy           : Double = healthMaxEnemy  - healthLeftEnemy
  val healthLostRatio           : Double = Maff.nanToZero(2 * healthLostEnemy / (healthLostUs + healthLostEnemy) - 1)
  val healthLostUsHere          : Double = healthLostUs    - previous.map(_.healthLostUs)   .getOrElse(0.0)
  val healthLostEnemyHere       : Double = healthLostEnemy - previous.map(_.healthLostEnemy).getOrElse(0.0)
  val healthDecisiveness        : Double = Math.max(Maff.nanToZero(healthLostUsHere / healthMaxUs), Maff.nanToZero(healthLostEnemyHere / healthMaxEnemy))
  val healthValueMaxUs          : Double = CPCount(us,   unitHealthValueMax)
  val healthValueMaxEnemy       : Double = CPCount(foe,  unitHealthValueMax)
  val healthValueLeftUs         : Double = CPCount(us,   unitHealthValueLeft)
  val healthValueLeftEnemy      : Double = CPCount(foe,  unitHealthValueLeft)
  val healthValueLostUs         : Double = healthValueMaxUs    - healthValueLeftUs
  val healthValueLostEnemy      : Double = healthValueMaxEnemy - healthValueLeftEnemy
  val healthValueLostUsHere     : Double = healthValueLostUs     - previous.map(_.healthValueLostUs)    .getOrElse(0.0)
  val healthValueLostEnemyHere  : Double = healthValueLostEnemy  - previous.map(_.healthValueLostEnemy) .getOrElse(0.0)
  val healthValueDecisiveness   : Double = Math.max(Maff.nanToZero(healthValueLostUsHere / healthValueMaxUs), Maff.nanToZero(healthValueLostEnemyHere / healthValueMaxEnemy))
  val healthValueLostRatio      : Double = Maff.nanToZero(2 * healthValueLostEnemy / (healthValueLostUs + healthValueLostEnemy) - 1)
  val ratioValueLostUs          : Double = Maff.nanToZero(valueLostUs    / valueTotalUs)
  val ratioValueLostEnemy       : Double = Maff.nanToZero(valueLostEnemy / valueTotalEnemy)
  val ratioValueLostNet         : Double = ratioValueLostEnemy - ratioValueLostUs
  val ratioHealthLostUs         : Double = Maff.nanToZero(healthLostUs     / healthMaxUs)
  val ratioHealthLostEnemy      : Double = Maff.nanToZero(healthLostEnemy  / healthMaxEnemy)
  val ratioHealthLostNet        : Double = ratioHealthLostEnemy - ratioHealthLostUs
  val ratioHealthValueLostUs    : Double = Maff.nanToZero(healthValueLostUs    / healthValueMaxUs)
  val ratioHealthValueLostEnemy : Double = Maff.nanToZero(healthValueLostEnemy / healthValueMaxEnemy)
  val ratioHealthValueLostNet   : Double = ratioHealthValueLostEnemy - ratioHealthValueLostUs
  val weightLife    = 1.0
  val weightHealth  = 0.1
  val totalScore                 : Double = if (With.performance.disqualificationDanger)
    Maff.weightedMean(Seq(
      (valueLostRatio,           weightLife),
      (healthValueLostRatio,     weightHealth)))
    else
      Maff.weightedMean(Seq(
        (valueLostRatio,           weightLife),
        //(HealthLostRatio,          weightHealth),
        (healthValueLostRatio,     weightHealth),
        (ratioValueLostNet,        weightLife),
        (ratioHealthLostNet,       weightHealth),
        (ratioHealthValueLostNet,  weightHealth)))
  val totalDecisiveness          : Double = Maff.mean(Seq(
    valueDecisiveness,
    healthDecisiveness,
    healthValueDecisiveness
  ))
  val cumulativeTotalDecisiveness: Double = previous.view.map(_.cumulativeTotalDecisiveness).sum + totalDecisiveness
}
