package Information.Battles.Simulation

import Information.Battles.Simulation.BattleStrategyFleeWounded.BattleStrategyFleeWounded
import Information.Battles.Simulation.BattleStrategyFocusAirOrGround.BattleStrategyFocusAirOrGround
import Information.Battles.Simulation.BattleStrategyMovement.BattleStrategyMovement
import Information.Battles.Simulation.BattleStrategyWorkersFighting.BattleStrategyWorkersFighting

class BattleStrategy(
  val fleeWounded:      BattleStrategyFleeWounded,
  val focusAirOrGround: BattleStrategyFocusAirOrGround,
  val movement:         BattleStrategyMovement,
  val workersFighting:  BattleStrategyWorkersFighting)