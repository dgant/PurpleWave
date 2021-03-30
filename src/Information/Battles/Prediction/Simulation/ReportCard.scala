package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.PredictionLocal

case class ReportCard(
                       simulacrum      : Simulacrum,
                       estimation      : PredictionLocal,
                       damageDealt     : Double,
                       damageReceived  : Double,
                       dead            : Boolean,
                       kills           : Int,
                       events          : Iterable[SimulationEvent])
