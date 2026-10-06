package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{Simulacrum, Simulation}
import Lifecycle.With
import Mathematics.Maff
import Utilities.?

class SimulationCheckpoint(simulation: Simulation, previous: Option[SimulationCheckpoint]) {

  @inline final def valueMax            (simulacrum: Simulacrum)  : Double  = simulacrum.subjectiveValue
  @inline final def valueLeft           (simulacrum: Simulacrum)  : Double  = ?(simulacrum.dead, 0.0, simulacrum.subjectiveValue)
  @inline final def unitHealthValueLeft (simulacrum: Simulacrum)  : Double  = ?( ! simulacrum.measureHealth || simulacrum.dead, 0.0, simulacrum.subjectiveValue * (simulacrum.hitPointsExact + simulacrum.shieldPointsExact) / Math.max(1, simulacrum.unitClass.maxTotalHealth))
  @inline final def unitHealthValueMax  (simulacrum: Simulacrum)  : Double  = ?( ! simulacrum.measureHealth, 0.0, simulacrum.subjectiveValue * (simulacrum.hitPointsInitial + simulacrum.shieldPointsInitial) / Math.max(1, simulacrum.unitClass.maxTotalHealth))

  val weightLife    = 1.0
  val weightHealth  = 0.1

  private val us  = simulation.realUnitsOurs
  private val foe = simulation.realUnitsEnemy

  val framesIn                  : Int    = simulation.battle.simulationFrames
  val aggression                : Double = With.blackboard.aggressionRatio()
  val valueTotalUs              : Double = CPCount(us,   valueMax)
  val valueTotalEnemy           : Double = CPCount(foe,  valueMax)
  val valueLeftUs               : Double = CPCount(us,   valueLeft)
  val valueLeftEnemy            : Double = CPCount(foe,  valueLeft)
  val valueLostUs               : Double =  valueTotalUs    - valueLeftUs
  val valueLostEnemy            : Double = (valueTotalEnemy - valueLeftEnemy) * aggression
  val valueLostRatio            : Double = Maff.nanToZero(2 * valueLostEnemy / (valueLostUs + valueLostEnemy) - 1)
  val healthValueMaxUs          : Double = CPCount(us,   unitHealthValueMax)
  val healthValueMaxEnemy       : Double = CPCount(foe,  unitHealthValueMax)
  val healthValueLeftUs         : Double = CPCount(us,   unitHealthValueLeft)
  val healthValueLeftEnemy      : Double = CPCount(foe,  unitHealthValueLeft)
  val healthValueLostUs         : Double =  healthValueMaxUs    - healthValueLeftUs
  val healthValueLostEnemy      : Double = (healthValueMaxEnemy - healthValueLeftEnemy) * aggression
  val healthValueLostRatio      : Double = Maff.nanToZero(2 * healthValueLostEnemy / (healthValueLostUs + healthValueLostEnemy) - 1)
  val totalScore                : Double =
    Maff.weightedMean(Seq(
      (valueLostRatio,           weightLife),
      (healthValueLostRatio,     weightHealth)))

  val cumulativeTotalDecisiveness: Double = (valueLostUs + valueLostEnemy) * weightLife + (healthValueLostUs + healthValueLostEnemy) * weightHealth
}
