package Information.Battles.Prediction.Simulation

import ProxyBwapi.Races.{Protoss, Terran}

object BehaviorInitial extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    if (simulacrum.unitClass == Terran.Medic) {
      simulacrum.targets.addAll(simulacrum.simulation.simulacraAlliesOf(simulacrum).filter(_.unitClass.isOrganic))
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorHeal)
        return
      }
    }
    if (simulacrum.unitClass == Terran.SCV) {
      // TODO: Add repair targets
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorRepair)
        return
      }
    }
    if (simulacrum.unitClass.isWorker) {
      if (simulacrum.pixel.base.exists(b => b.harvestingArea.contains(simulacrum.pixel) && b.owner == simulacrum.player)) {
        simulacrum.doBehavior(BehaviorGather)
        return
      }
    }
    if (simulacrum.unitClass == Protoss.HighTemplar) {
      simulacrum.targets.addAll(simulacrum.simulation.simulacraEnemiesOf(simulacrum).view.filterNot(_.unitClass.canBeStormed))
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorStorm)
        return
      }
    }
    if (simulacrum.unitClass.isDetector && simulacrum.canMove) {
      simulacrum.targets.addAll(simulacrum.simulation.simulacraEnemiesOf(simulacrum).view.filter(_.cloaked))
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorDetect)
        return
      }
    }
    if (simulacrum.attacksAgainstAir > 0 || simulacrum.attacksAgainstGround > 0) {
      simulacrum.targets.addAll(simulacrum.simulation.simulacraEnemiesOf(simulacrum).view.filter(simulacrum.canAttack))
    }
    if (simulacrum.targets.isEmpty) {
      if (simulacrum.unitClass.isBuilding) {
        simulacrum.measureHealth = false
        simulacrum.doBehavior(BehaviorIdle)
      } else {
        simulacrum.doBehavior(BehaviorFlee)
      }
    } else {
      simulacrum.doBehavior(BehaviorFight)
    }
  }
}
