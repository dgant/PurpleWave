package Information.Battles.Prediction.Simulation

import Information.Battles.Types.Battle

class ReportCard(simulacrum: Simulacrum, val estimation: Battle) {
  val pixel = simulacrum.pixel
  val attackingEnemy = simulacrum.target.exists(_.isEnemyOf(simulacrum.realUnit))
  val damageDealt     : Double                    = simulacrum.damageDealt
  val damageReceived  : Double                    = simulacrum.damageReceived
  val valueDealt      : Double                    = simulacrum.valueDealt
  val valueReceived   : Double                    = simulacrum.valueReceived
  val alive           : Boolean                   = simulacrum.alive
  val kills           : Int                       = simulacrum.kills
  val events          : Iterable[SimulationEvent] = simulacrum.events.toVector
}

