package Information.Battles.Prediction.Simulation

import Information.Battles.Types.Battle

class ReportCard(val simulacrum: Simulacrum, val estimation: Battle) {
  val damageDealt     : Double                    = simulacrum.damageDealt
  val damageReceived  : Double                    = simulacrum.damageReceived
  val alive           : Boolean                   = simulacrum.alive
  val kills           : Int                       = simulacrum.kills
  val events          : Iterable[SimulationEvent] = simulacrum.events.toVector
}

