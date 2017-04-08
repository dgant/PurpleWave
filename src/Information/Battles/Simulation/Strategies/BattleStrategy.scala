package Information.Battles.Simulation.Strategies

import Information.Battles.Simulation.Strategies.BattleStrategyWounded.BattleStrategyWounded
import Information.Battles.Simulation.Strategies.BattleStrategyFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Strategies.BattleStrategyMovement.BattleStrategyMovement
import Information.Battles.Simulation.Strategies.BattleStrategyWorkers.BattleStrategyWorkers

case class BattleStrategy(
                           val fleeWounded:      BattleStrategyWounded,
                           val focusAirOrGround: BattleStrategyFocusAirOrGround,
                           val movement:         BattleStrategyMovement,
                           val workersFighting:  BattleStrategyWorkers)