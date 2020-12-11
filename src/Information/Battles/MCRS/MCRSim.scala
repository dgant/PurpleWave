package Information.Battles.MCRS

import Mathematics.PurpleMath
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Seconds

object MCRSim {

  def getSimValue(unit: UnitInfo): MCRSOutput = {
    val newOutput = new MCRSOutput(unit)
    simulate(unit, newOutput)
    newOutput
  }

  private def simulate(unit: UnitInfo, output: MCRSOutput): Unit = {
    if (unit.topSpeed <= 0) return

    // McRave uses 5s and thinks scaling with fight size is a good idea
    val simulationTime = Seconds(5)() + Math.max(0.0, unit.pixelDistanceCenter(unit.mcrs.engagePosition()) / unit.topSpeed)

    var enemyStrengthGround: Double = 0.0
    var enemyStrengthAir: Double = 0.0
    var friendlyStrengthGround: Double = 0.0
    var friendlyStrengthAir: Double = 0.0

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

      enemyStrengthGround += simRatio * enemy.mcrs.strengthGround()
      enemyStrengthAir += simRatio * enemy.mcrs.strengthAir()
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

      // Synchronize check
      output.synchronizeAirAndGround = output.synchronizeAirAndGround || (simRatio > 0.0 && unit.flying != ally.flying)

      friendlyStrengthGround += ally.mcrs.strengthGround() * simRatio
      friendlyStrengthAir += ally.mcrs.strengthAir() * simRatio
    }

    unit.matchups.alliesInclSelf.foreach(countAlly)
    unit.matchups.enemies.foreach(countEnemy)

    output.attackAirAsAir = PurpleMath.nanToInfinity(friendlyStrengthAir / enemyStrengthAir)
    output.attackAirAsGround = PurpleMath.nanToInfinity(friendlyStrengthAir / enemyStrengthGround)
    output.attackGroundAsAir = PurpleMath.nanToInfinity(friendlyStrengthGround / enemyStrengthAir)
    output.attackGroundasGround = PurpleMath.nanToInfinity(friendlyStrengthGround / enemyStrengthGround)
  }
}
