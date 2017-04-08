package Information.Battles.Simulation.Tactics

import Information.Battles.Simulation.Tactics.TacticWounded.TacticWounded
import Information.Battles.Simulation.Tactics.TacticFocus.TacticFocus
import Information.Battles.Simulation.Tactics.TacticMovement.TacticMovement
import Information.Battles.Simulation.Tactics.TacticWorkers.TacticWorkers

case class Tactics(
                    val fleeWounded:      TacticWounded,
                    val focusAirOrGround: TacticFocus,
                    val movement:         TacticMovement,
                    val workersFighting:  TacticWorkers)