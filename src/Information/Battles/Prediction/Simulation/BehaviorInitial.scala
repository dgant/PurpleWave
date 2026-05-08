package Information.Battles.Prediction.Simulation

import ProxyBwapi.Races.{Protoss, Terran}

object BehaviorInitial extends SimulacrumBehavior {
  val fighting: Boolean = true
  @inline override def act(simulacrum: Simulacrum): Unit = {
    if (simulacrum.canMove && simulacrum.realUnit.friendly.exists( ! _.intent.canFight)) {
      simulacrum.doBehavior(BehaviorFlee)
      return
    }
    if (simulacrum.unitClass == Terran.Medic) {
      addOrganicAllies(simulacrum)
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
      if ( ! simulacrum.visible || simulacrum.realUnit.gathering || simulacrum.pixel.base.exists(b => b.owner == simulacrum.player && (b.harvestingArea.contains(simulacrum.pixel)))) {
        simulacrum.doBehavior(BehaviorGather)
        simulacrum.shouldScoreIfOurs = false
        return
      }
    }
    if (simulacrum.unitClass == Protoss.HighTemplar) {
      addStormTargets(simulacrum)
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorStorm)
        return
      }
    }
    if (simulacrum.unitClass.isDetector && simulacrum.canMove) {
      addCloakedEnemies(simulacrum)
      if (simulacrum.targets.nonEmpty) {
        simulacrum.doBehavior(BehaviorDetect)
        return
      }
    }
    if (simulacrum.attacksAgainstAir > 0 || simulacrum.attacksAgainstGround > 0) {
      addAttackableEnemies(simulacrum)
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

  @inline private def addOrganicAllies(simulacrum: Simulacrum): Unit = {
    val units = if (simulacrum.isFriendlyCached) simulacrum.simulation.realUnitsOurs else simulacrum.simulation.realUnitsEnemy
    var i = 0
    while (i < units.length) {
      val target = units(i).simulacrum
      if ((target ne simulacrum) && target.unitClass.isOrganic) {
        simulacrum.targets.add(target)
      }
      i += 1
    }
  }

  @inline private def addStormTargets(simulacrum: Simulacrum): Unit = {
    val units = if (simulacrum.isFriendlyCached) simulacrum.simulation.realUnitsEnemy else simulacrum.simulation.realUnitsOurs
    var i = 0
    while (i < units.length) {
      val target = units(i).simulacrum
      if (target.unitClass.canBeStormed) {
        simulacrum.targets.add(target)
      }
      i += 1
    }
  }

  @inline private def addCloakedEnemies(simulacrum: Simulacrum): Unit = {
    val units = if (simulacrum.isFriendlyCached) simulacrum.simulation.realUnitsEnemy else simulacrum.simulation.realUnitsOurs
    var i = 0
    while (i < units.length) {
      val target = units(i).simulacrum
      if (target.cloaked) {
        simulacrum.targets.add(target)
      }
      i += 1
    }
  }

  @inline private def addAttackableEnemies(simulacrum: Simulacrum): Unit = {
    val units = if (simulacrum.isFriendlyCached) simulacrum.simulation.realUnitsEnemy else simulacrum.simulation.realUnitsOurs
    var i = 0
    while (i < units.length) {
      val target = units(i).simulacrum
      if (simulacrum.canAttack(target)) {
        simulacrum.targets.add(target)
      }
      i += 1
    }
  }
}
