package Information.Battles.Simulation.Construction

import Information.Battles.Simulation.Execution.BattleSimulator
import Information.Battles.BattleTypes.{Battle, BattleGroup}
import Information.Battles.TacticsTypes.{Tactics, TacticsOptionsDefault}

object BattleSimulationBuilder {
  
  def build(battle:Battle):Vector[BattleSimulation] = {
    buildOurTacticVariants(battle.us).flatten(ourTactics =>
      buildEnemyTacticVariants(battle.enemy).map(enemyTactics =>
        buildSimulation(ourTactics, enemyTactics)))
  }
  
  def buildOurTacticVariants(ourGroup:BattleGroup):Vector[BattleSimulationGroup] = {
   ourGroup.tacticsAvailable.map(tactics => new BattleSimulationGroup(ourGroup, tactics))
  }
  
  def buildEnemyTacticVariants(enemyGroup:BattleGroup):Vector[BattleSimulationGroup] = {
    Vector(new BattleSimulationGroup(enemyGroup, new TacticsOptionsDefault))
  }
  
  def buildSimulation(ourGroup:BattleSimulationGroup, enemyGroup:BattleSimulationGroup):BattleSimulation = {
    instructWorkers(ourGroup)
    instructWorkers(enemyGroup)
    new BattleSimulation(ourGroup, enemyGroup)
  }
  
  def instructWorkers(group:BattleSimulationGroup) {
    var workersNotMining = 0
    val workers = group.units.filter(_.unit.unitClass.isWorker)
    
    if (group.tactics.has(Tactics.Workers.Flee)) {
      workersNotMining = workers.size
      workers.foreach(worker => { worker.fleeing = true; worker.fighting = false })
    }
    else if (group.tactics.has(Tactics.Workers.Ignore)) {
      workers.toVector.foreach(worker => {worker.fighting = false; worker.ignoring = true})
    }
    else if (group.tactics.has(Tactics.Workers.FightHalf)) {
      workersNotMining = workers.size / 2
      workers.toVector.sortBy(_.totalLife).take(workersNotMining).foreach(_.fighting = false)
    }
    else if (group.tactics.has(Tactics.Workers.FightAll)) {
      workersNotMining = workers.size
    }
    
    group.lostValuePerSecond += BattleSimulator.costPerSecondOfNotMining(workersNotMining)
  }
}
