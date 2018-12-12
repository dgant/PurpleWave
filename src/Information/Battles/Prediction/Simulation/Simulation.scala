package Information.Battles.Prediction.Simulation

import Information.Battles.Prediction.Prediction
import Information.Battles.Types.{BattleLocal, Team}
import Information.Geography.Types.Zone
import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.PurpleMath
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo

class Simulation(
  val battle    : BattleLocal,
  val weAttack  : Boolean,
  val weSnipe   : Boolean) {
  
  private def buildSimulacra(team: Team) = if (With.configuration.enableMCRS) Vector.empty else team.units.filter(legalForSimulation).map(new Simulacrum(this, _))
  private def legalForSimulation(unit: UnitInfo): Boolean = (
    ! unit.invincible             // No stasised units
    && ! unit.is(Protoss.Carrier) // Simulate the Interceptors only -- produces more reliable results
    && ! unit.is(Protoss.Scarab)
  )
  
  val estimation            : Prediction          = new Prediction
  val focus                 : Pixel               = battle.focus
  val unitsOurs             : Vector[Simulacrum]  = buildSimulacra(battle.us)
  val unitsEnemy            : Vector[Simulacrum]  = buildSimulacra(battle.enemy)
  val everyone              : Vector[Simulacrum]  = unitsOurs ++ unitsEnemy
  var updated               : Boolean             = true
  var fleeing               : Boolean             = ! weAttack
  lazy val ourWidth         : Double              = battle.us.units.filterNot(_.flying).map(unit => if (unit.flying) 0.0 else unit.unitClass.dimensionMin + unit.unitClass.dimensionMax).sum
  lazy val chokeMobility    : Map[Zone, Double]   = battle.us.units.map(_.zone).distinct.map(zone => (zone, getChokeMobility(zone))).toMap
  
  val simulacra: Map[UnitInfo, Simulacrum] = if (With.configuration.enableMCRS) Map.empty else
    (unitsOurs.filter(_.canMove) ++ unitsEnemy)
      .map(simulacrum => (simulacrum.realUnit, simulacrum))
      .toMap
  
  def complete: Boolean = (
    estimation.frames > With.configuration.simulationFrames
    || ! updated
    || unitsOurs.forall(_.dead)
    || unitsEnemy.forall(_.dead)
    || everyone.forall(e => e.dead || ! e.fighting)
  )
  
  def run() {
    if (!With.configuration.enableMCRS) {
      while (!complete) step()
    }
    cleanup()
  }
  
  def step() {
    updated = false
    estimation.frames += 1
    snipe()
    everyone.foreach(_.step())
    everyone.foreach(_.updateDeath())
  }
  
  def snipe() {
    if (weAttack
      && weSnipe
      && ! fleeing
      && unitsEnemy.exists(e => e.dead && ! e.realUnit.unitClass.suicides)) {
      fleeing = true
      unitsOurs.foreach(_.cooldownMoving = 24)
    }
  }
  
  def cleanup() {
    estimation.costToUs         = unitsOurs   .map(_.valueReceived).sum
    estimation.costToEnemy      = unitsEnemy  .map(_.valueReceived).sum
    estimation.damageToUs       = unitsOurs   .map(_.damageReceived).sum
    estimation.damageToEnemy    = unitsEnemy  .map(_.damageReceived).sum
    estimation.deathsUs         = unitsOurs   .count(_.dead)
    estimation.deathsEnemy      = unitsEnemy  .count(_.dead)
    estimation.totalUnitsUs     = unitsOurs   .size
    estimation.totalUnitsEnemy  = unitsEnemy  .size
    estimation.reportCards      ++= everyone  .map(simulacrum => (simulacrum.realUnit, simulacrum.reportCard))
    estimation.simulation       = Some(this)
    estimation.events           = everyone.flatMap(_.events).sortBy(_.frame)
  }
  
  private def getChokeMobility(zoneUs: Zone): Double = {
    val zoneEnemy = battle.enemy.centroid.zone
    if (zoneUs == zoneEnemy) return 1.0
    val edge      = zoneUs.edges.find(_.zones.contains(zoneEnemy))
    val edgeWidth = Math.max(32.0, edge.map(_.radiusPixels * 2.0).getOrElse(32.0 * 10.0))
    PurpleMath.nanToOne(2.5 * edgeWidth / ourWidth)
  }
}
