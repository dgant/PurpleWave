package Information.Battles.Simulation.Strategies

import Information.Battles.Simulation.Strategies.BattleStrategyFleeWounded.BattleStrategyFleeWounded
import Information.Battles.Simulation.Strategies.BattleStrategyFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.Strategies.BattleStrategyMovement.BattleStrategyMovement
import Information.Battles.Simulation.Strategies.BattleStrategyWorkers.BattleStrategyWorkers

class BattleStrategy(
  val fleeWounded:      BattleStrategyFleeWounded,
  val focusAirOrGround: BattleStrategyFocusAirOrGround,
  val movement:         BattleStrategyMovement,
  val workersFighting:  BattleStrategyWorkers)