package Information.Battles.Prediction.Simulation

import ProxyBwapi.Races.{Protoss, Terran}

object BehaviorInitial extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: NewSimulacrum): Unit = {
    if (simulacrum.unitClass == Terran.Medic) {
      simulacrum.targets ++= simulacrum.simulation.simulacraOurs.filter(_.unitClass.isOrganic)
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorHeal)
        return
      }
    }
    if (simulacrum.unitClass == Terran.SCV) {
      // TODO
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
    if (simulacrum.unitClass == Protoss.HighTemplar) {
      simulacrum.targets ++= simulacrum.simulation.simulacraEnemy.filterNot(_.unitClass.isBuilding)
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorStorm)
        return
      }
    }

    if (simulacrum.attacksAgainstAir > 0 || simulacrum.attacksAgainstGround > 0) {
      simulacrum.targets ++= simulacrum.simulation.simulacraEnemy.filter(simulacrum.attacksAgainst(_) > 0)
    }
    if (simulacrum.targets.isEmpty) {
      simulacrum.doBehavior(BehaviorFlee)
    } else {
      simulacrum.doBehavior(BehaviorFight)
    }
  }
}
