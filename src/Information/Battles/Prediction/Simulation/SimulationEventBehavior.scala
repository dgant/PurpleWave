package Information.Battles.Prediction.Simulation
import Mathematics.Points.Pixel

final case class SimulationEventBehavior(
  sim: Simulacrum,
  behaviorOld: SimulacrumBehavior,
  behaviorNew: SimulacrumBehavior)
    extends SimulationEvent(sim) {

  override def toString: String = f"$frame: ${describe(sim)} transitions from $behaviorOld to $behaviorNew"

  override val to: Pixel = from
}
