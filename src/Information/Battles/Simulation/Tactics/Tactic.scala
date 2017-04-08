package Information.Battles.Simulation.Tactics

import Information.Battles.Simulation.Tactics.TacticWounded.BattleStrategyWounded
import Information.Battles.Simulation.Tactics.TacticFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Tactics.TacticMovement.BattleStrategyMovement
import Information.Battles.Simulation.Tactics.TacticWorkers.BattleStrategyWorkers

case class Tactic(
                           val fleeWounded:      BattleStrategyWounded,
                           val focusAirOrGround: BattleStrategyFocusAirOrGround,
                           val movement:         BattleStrategyMovement,
                           val workersFighting:  BattleStrategyWorkers)