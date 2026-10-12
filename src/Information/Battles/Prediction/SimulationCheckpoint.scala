package Information.Battles.Prediction

import Information.Battles.Prediction.Simulation.{Simulacrum, Simulation}
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.?

object SimulationCheckpoint {
  // Immutable because each UnitInfo's simulacrum is reused by subsequent forecasts.
  private case class Snapshot(valueMax: Double, valueLeft: Double, healthMax: Double, healthLeft: Double, dead: Boolean)
  private val emptySnapshot = Snapshot(0.0, 0.0, 0.0, 0.0, dead = false)
  private def capture(simulation: Simulation): Map[UnitInfo, Snapshot] = simulation.realUnits.map { unit =>
    val s = unit.simulacrum
    val value = if (s.shouldScoreIfOurs || s.isEnemy) s.subjectiveValue else 0.0
    val healthMax = if (s.measureHealth) value * (s.hitPointsInitial + s.shieldPointsInitial) / Math.max(1, s.unitClass.maxTotalHealth) else 0.0
    val healthLeft = if (s.measureHealth && s.alive) value * (s.hitPoints + s.shieldPoints) / Math.max(1, s.unitClass.maxTotalHealth) else 0.0
    unit -> Snapshot(value, if (s.alive) value else 0.0, healthMax, healthLeft, s.dead)
  }.toMap
}

class SimulationCheckpoint private (
  snapshots: Map[UnitInfo, SimulationCheckpoint.Snapshot],
  us: Seq[UnitInfo], foe: Seq[UnitInfo],
  val framesIn: Int, val aggression: Double) {

  def this(simulation: Simulation, previous: Option[SimulationCheckpoint]) = this(
    SimulationCheckpoint.capture(simulation), simulation.realUnitsOurs, simulation.realUnitsEnemy,
    simulation.battle.simulationFrames, With.blackboard.aggressionRatio())

  def subset(ours: Seq[UnitInfo], enemies: Seq[UnitInfo]): SimulationCheckpoint =
    new SimulationCheckpoint(snapshots, ours, enemies, framesIn, aggression)

  def deaths(units: Seq[UnitInfo]): Int = units.count(u => snapshots.get(u).exists(_.dead))

  // Resolve each unit once per checkpoint/subset, instead of looking it up in every aggregate.
  private def select(units: Seq[UnitInfo]): Array[SimulationCheckpoint.Snapshot] = {
    val output = new Array[SimulationCheckpoint.Snapshot](units.size)
    val iterator = units.iterator
    var i = 0
    while (iterator.hasNext) {
      output(i) = snapshots.getOrElse(iterator.next(), SimulationCheckpoint.emptySnapshot)
      i += 1
    }
    output
  }
  private val snapshotsUs = select(us)
  private val snapshotsEnemy = select(foe)

  @inline final def valueMax            (simulacrum: Simulacrum)  : Double  = simulacrum.subjectiveValue
  @inline final def valueLeft           (simulacrum: Simulacrum)  : Double  = ?(simulacrum.dead, 0.0, simulacrum.subjectiveValue)
  @inline final def unitHealthValueLeft (simulacrum: Simulacrum)  : Double  = ?( ! simulacrum.measureHealth || simulacrum.dead, 0.0, simulacrum.subjectiveValue * (simulacrum.hitPoints + simulacrum.shieldPoints) / Math.max(1, simulacrum.unitClass.maxTotalHealth))
  @inline final def unitHealthValueMax  (simulacrum: Simulacrum)  : Double  = ?( ! simulacrum.measureHealth, 0.0, simulacrum.subjectiveValue * (simulacrum.hitPointsInitial + simulacrum.shieldPointsInitial) / Math.max(1, simulacrum.unitClass.maxTotalHealth))

  val weightSurvival = 0.25
  val weightHealth   = 1.0 - weightSurvival

  val valueTotalUs              : Double = CPCount(snapshotsUs)(_.valueMax)
  val valueTotalEnemy           : Double = CPCount(snapshotsEnemy)(_.valueMax)
  val valueLeftUs               : Double = CPCount(snapshotsUs)(_.valueLeft)
  val valueLeftEnemy            : Double = CPCount(snapshotsEnemy)(_.valueLeft)
  val valueLostUs               : Double =  valueTotalUs    - valueLeftUs
  val valueLostEnemy            : Double = (valueTotalEnemy - valueLeftEnemy) * aggression
  val valueLostRatio            : Double = Maff.nanToZero(2 * valueLostEnemy / (valueLostUs + valueLostEnemy) - 1)
  val healthValueMaxUs          : Double = CPCount(snapshotsUs)(_.healthMax)
  val healthValueMaxEnemy       : Double = CPCount(snapshotsEnemy)(_.healthMax)
  val healthValueLeftUs         : Double = CPCount(snapshotsUs)(_.healthLeft)
  val healthValueLeftEnemy      : Double = CPCount(snapshotsEnemy)(_.healthLeft)
  val healthValueLostUs         : Double =  healthValueMaxUs    - healthValueLeftUs
  val healthValueLostEnemy      : Double = (healthValueMaxEnemy - healthValueLeftEnemy) * aggression
  val healthValueLostRatio      : Double = Maff.nanToZero(2 * healthValueLostEnemy / (healthValueLostUs + healthValueLostEnemy) - 1)
  val lossUs                    : Double = valueLostUs * weightSurvival + healthValueLostUs * weightHealth
  val lossEnemy                 : Double = valueLostEnemy * weightSurvival + healthValueLostEnemy * weightHealth
  // Five percent of initial combined value keeps tiny exchanges from producing extreme scores.
  private val lossFloor         : Double = 0.05 * (
    (valueTotalUs + valueTotalEnemy * aggression) * weightSurvival +
    (healthValueMaxUs + healthValueMaxEnemy * aggression) * weightHealth)
  val totalScore                : Double = Maff.nanToZero((lossEnemy - lossUs) / Math.max(lossUs + lossEnemy, lossFloor))

  val cumulativeTotalDecisiveness: Double = lossUs + lossEnemy
}
