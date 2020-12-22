package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.{LocalBattleMetrics, PredictionLocal}
import Information.Battles.Types.Team
import Lifecycle.With
import Mathematics.Points.Pixel
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

class Simulation(val prediction: PredictionLocal) {
  
  private def buildSimulacra(team: Team): Vector[Simulacrum] = {
    if (With.blackboard.mcrs()) return Vector.empty

    def legalForSimulation(unit: UnitInfo): Boolean = {
      val effectiveRangePixels = unit.effectiveRangePixels
      (
        unit.complete
          && ! unit.unitClass.isSpell
          && ! unit.invincible          // No stasised units
          && ! unit.is(if (With.performance.danger) Protoss.Interceptor else Protoss.Carrier) // Interceptors produce more accurate results but are slower
          && ! unit.is(Protoss.Scarab)
          && ! (unit.unitClass.isWorker && unit.gathering && unit.isOurs)
          && ! (unit.unitClass.isBuilding && ! unit.canAttack && ! unit.unitClass.isSpellcaster)
          // Is close enough to interact with anyone on the other team
          && team.opponent.units.exists(other =>
            unit.pixelDistanceEdge(other)
            - Math.max(effectiveRangePixels, other.effectiveRangePixels)
            < With.configuration.simulationFrames * (unit.topSpeed + other.topSpeed))
      )
    }

    team.units.view.filter(legalForSimulation).map(new Simulacrum(this, _)).toVector
  }

  val focus       : Pixel               = prediction.battle.focus
  val unitsOurs   : Vector[Simulacrum]  = buildSimulacra(prediction.battle.us)
  val unitsEnemy  : Vector[Simulacrum]  = buildSimulacra(prediction.battle.enemy)
  val everyone    : Vector[Simulacrum]  = unitsOurs ++ unitsEnemy
  var updated     : Boolean             = true
  var fleeing     : Boolean             = ! prediction.weAttack
  
  val simulacra: Map[UnitInfo, Simulacrum] = if (With.blackboard.mcrs()) Map.empty else
    (unitsOurs.filter(_.canMove) ++ unitsEnemy)
      .map(simulacrum => (simulacrum.realUnit, simulacrum))
      .toMap

  def complete: Boolean = (
    prediction.frames > With.configuration.simulationFrames
    || ! updated
    || unitsOurs.forall(_.dead)
    || unitsEnemy.forall(_.dead)
    || everyone.forall(u => u.dead || ! u.fighting)
  )
  
  def step() {
    updated = false
    prediction.frames += 1
    snipe()
    everyone.foreach(_.step())
    everyone.foreach(_.updateDeath())

    if (complete) {
      cleanup()
    } else if (prediction.frames - prediction.localBattleMetrics.lastOption.map(_.framesIn).getOrElse(0) >= With.configuration.simulationEstimationPeriod) {
      recordMetrics()
    }
  }
  
  def snipe() {
    if (prediction.weAttack
      && prediction.weSnipe
      && ! fleeing
      && unitsEnemy.exists(e => e.dead && ! e.realUnit.unitClass.suicides)) {
      fleeing = true
      unitsOurs.foreach(_.cooldownMoving = 24)
    }
  }

  def recordMetrics() {
    prediction.localBattleMetrics += new LocalBattleMetrics(this, prediction.localBattleMetrics.lastOption)
  }
  
  def cleanup() {
    prediction.deathsUs     = unitsOurs.count(_.dead)
    if (With.configuration.debugging) {
      prediction.debugReport ++= everyone.map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
      prediction.events = everyone.flatMap(_.events).sortBy(_.frame)
    }
    recordMetrics()
  }
}
