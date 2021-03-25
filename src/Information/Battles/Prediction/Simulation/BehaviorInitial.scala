package Information.Battles.Prediction.Simulation

import ProxyBwapi.Races.Terran

object BehaviorInitial extends SimulacrumBehavior {
  @inline override def act(simulacrum: NewSimulacrum): Unit = {
    if (simulacrum.unitClass == Terran.Medic) {
      simulacrum.doBehavior(BehaviorHeal)
      // TODO: Populate targets
      return
    }
    if (simulacrum.unitClass == Terran.SCV) {
      // TODO: Populate repair targets
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorRepair)
        return
      }
    }
    if (simulacrum.unitClass.isWorker) {
      if (simulacrum.pixel.base.exists(_.harvestingArea.contains(simulacrum.pixel))) {
        simulacrum.doBehavior(BehaviorGather)
        return
      }
    }
    // TODO: Idle units go idle
    // TODO: Pick targets
    if (simulacrum.targets.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
    } else {
      simulacrum.doBehavior(BehaviorFight)
    }
  }
}
