package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.PredictionLocal

case class ReportCard(
                       simulacrum      : Simulacrum,
                       estimation      : PredictionLocal,
                       valueDealt      : Double,
                       valueReceived   : Double,
                       damageDealt     : Double,
                       damageReceived  : Double,
                       dead            : Boolean,
                       kills           : Int,
                       events          : Iterable[SimulationEvent]) {
  
  lazy val netValuePerFrame: Double = (valueDealt - valueReceived) / Math.max(1, estimation.frames)
}
