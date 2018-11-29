package Information.Battles.MCRS

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo

object MCRSim {

  def getSimValue(unit: UnitInfo): MCRSOutput = {
    val newOutput = new MCRSOutput(unit)
    simulate(unit, newOutput)
    newOutput
  }

  private def simulate(unit: UnitInfo, output: MCRSOutput): Unit = {
    if (unit.topSpeed <= 0) return

    // McRave uses 5s and thinks scaling with fight size is a good idea
    val simulationTime = GameTime(0, 5)() + Math.max(0.0, unit.pixelDistanceCenter(unit.mcrs.engagePosition()) / unit.topSpeed)

    var enemyGroundStrength: Double = 0.0
    var enemyAirStrength: Double = 0.0
    var friendlyGroundStrength: Double = 0.0
    var friendlyAirStrength: Double = 0.0

    // Check every enemy unit being in range of the target
    def countEnemy(enemy: UnitInfo) {

      // Ignore workers and useless units
		  if (enemy.unitClass.isWorker && ! enemy.isBeingViolent) return
      if (enemy.matchups.targets.isEmpty) return

      // Distance parameters// Distance parameters
      val distance = enemy.pixelDistanceEdge(unit) - enemy.pixelRangeAgainst(unit)

      // Sim values
      var enemyToEngage: Double = 0.0

      // If enemy can move, distance/speed is time to engage
      if (enemy.topSpeed > 0.0) {
        enemyToEngage = Math.max(0.0, distance / enemy.topSpeed)
      }
      // If enemy can't move, it must be in range of our engage position to be added
      else if (enemy.inRangeToAttack(unit, unit.mcrs.engagePosition())) {
        enemyToEngage = PurpleMath.nanToZero(Math.max(0.0, distance / unit.topSpeed))

      }
      else return

      var simRatio = Math.max(0.0, simulationTime - enemyToEngage)

      // High ground check
      if (enemy.flying && With.grids.altitudeBonus.get(enemy.tileIncludingCenter) > With.grids.altitudeBonus.get(unit.mcrs.engagePosition().tileIncluding)) {
        simRatio *= 2
      }

      enemyGroundStrength += enemy.mcrs.visibleGroundStrength() * simRatio
      enemyAirStrength += enemy.mcrs.visibleAirStrength() * simRatio
    }

    // Check every ally being in range of the target
    def countAlly(ally: UnitInfo) {
      if (ally.mcrs.target().isEmpty || ! ally.canDoAnything) return
      // This is original
      if (ally.unitClass.isWorker && ! ally.isBeingViolent) return
      if (PurpleMath.nanToInfinity(ally.pixelDistanceCenter(unit.mcrs.engagePosition()) / ally.topSpeed) > simulationTime) return

      // Setup true distance
      val distance = ally.pixelDistanceCenter(ally.mcrs.engagePosition()) - ally.unitClass.radialHypotenuse

      val allyToEngage = PurpleMath.nanToInfinity(Math.max(0.0, (distance / ally.topSpeed)))
      var simRatio = Math.max(0.0, simulationTime - allyToEngage)

      // High ground check
      // TODO: This is kind of nonsensical -- punishes air units for no reason, right?
      if (!ally.flying && With.grids.altitudeBonus.get(ally.tileIncludingCenter) > With.grids.altitudeBonus.get(ally.mcrs.target().get.tileIncludingCenter)) {
        simRatio *= 2
      }

      // Synchronize check
      output.synchronizeAirAndGround = output.synchronizeAirAndGround || (simRatio > 0.0 && unit.flying != ally.flying)

      friendlyGroundStrength += ally.mcrs.visibleGroundStrength() * simRatio
      friendlyAirStrength += ally.mcrs.visibleAirStrength() * simRatio
    }

    unit.matchups.alliesInclSelf.foreach(countAlly)
    unit.matchups.enemies.foreach(countEnemy)

    output.attackAirAsAir = PurpleMath.nanToInfinity(friendlyAirStrength / enemyAirStrength)
    output.attackAirAsGround = PurpleMath.nanToInfinity(friendlyAirStrength / enemyGroundStrength)
    output.attackGroundAsAir = PurpleMath.nanToInfinity(friendlyGroundStrength / enemyAirStrength)
    output.attackGroundasGround = PurpleMath.nanToInfinity(friendlyGroundStrength / enemyGroundStrength)
  }
}
