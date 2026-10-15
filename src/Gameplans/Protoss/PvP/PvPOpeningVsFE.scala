package Gameplans.Protoss.PvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvP1012, PvP3GateGoon, PvP4GateGoon, PvPCoreExpand, PvPDT, PvPObs, PvPReaver, PvPRobo, PvPNexusFirst => PvPNexusFirstStrategy}
import Utilities.DoQueue
import Utilities.Time.{GameTime, Seconds}

class PvPOpeningVsFE extends GameplanImperative {

  trait Response
  object NexusFirst extends Response
  object Robo  extends Response

  var response: Option[Response] = None

  override def activated: Boolean = {
    if (PvPNexusFirstStrategy()) return false
    if (PvPCoreExpand()) return false
    if ( ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)) return false

    if (response.isEmpty && (enemiesHaveComplete(Protoss.PhotonCannon) || With.scouting.enemyNatural.exists(_.townHallTile.lastSeen > 0))) {
      if (With.frame < GameTime(2, 0)() + Seconds(10)() * enemies(Protoss.PhotonCannon, Protoss.Forge)) {
        response = Some(NexusFirst)
      } else if (With.fingerprints.forgeFe()) {
        if (unitsComplete(Protoss.Gateway) > 1) {
          response = Some(Robo)
        } else if (haveComplete(Protoss.Assimilator)) {
          response = Some(Robo)
        } else {
          response = Some(NexusFirst)
        }
      } else if (With.fingerprints.nexusFirst()) {
        response = Some(Robo)
      }
    }
    if (enemyRecentStrategy(With.fingerprints.cannonRush) && ! With.scouting.enemyNatural.exists(_.townHall.isDefined)) {
      response = None
    }

    if (response.isDefined) {
      PvPCoreExpand.swapOut()
      PvPDT.swapOut()
      PvP3GateGoon.swapOut()
      PvP4GateGoon.swapOut()

      if (response.contains(NexusFirst)) {
        PvPNexusFirstStrategy.swapIn()
      }
      if (response.contains(Robo)) {
        PvPRobo.swapIn()
        PvPReaver.swapIn()
        PvPObs.swapOut()
      } else {
        PvPRobo.swapOut()
        PvPReaver.swapOut()
        PvPObs.swapOut()
      }
    }

    response.isDefined
  }

  override def completed: Boolean = bases >= 2 && units(Protoss.Gateway) + units(Protoss.RoboticsSupportBay) >= 4

  override def executeBuild(): Unit = {
    scout(1)
    once(8, Protoss.Probe)
    once(Protoss.Pylon)
    once(10, Protoss.Probe)

    if (response.contains(NexusFirst)) {
      if (miningBases < 2 && minerals < 400) {
        cancel(Protoss.Gateway, Protoss.Forge, Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.Zealot)
      }
      once(13, Protoss.Probe)
      requireMiningBases(2)
      once(14, Protoss.Probe)
      get(Protoss.Gateway)
      stealGas()
    } else {
      once(Protoss.Gateway)
      once(12, Protoss.Probe)
      once(Protoss.Assimilator)
      once(13, Protoss.Probe)
      once(Protoss.CyberneticsCore)
      once(14, Protoss.Probe)
      get(2, Protoss.Pylon)
      stealGas()
      once(16, Protoss.Probe)
      once(Protoss.RoboticsFacility)
    }
  }

  var attackTrigger: Boolean = false
  override def executeMain(): Unit = {
    //attackTrigger ||= unitsEver(u => Protoss.Reaver(u) && u.complete) >= 2
    attack(safePushing)

    val doArmy = new DoQueue(() => {
      pump(Protoss.Observer, 1)
      pumpShuttleAndReavers()
      pump(Protoss.Dragoon)
    })

    if (PvPIdeas.enemyDarkTemplarLikely) {
      takeOurGas()
      get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.RoboticsFacility, Protoss.Observatory)
      once(Protoss.Observer)
    }

    if (armySupply200 >= 24) {
      requireMiningBases(2)
    }

    get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.RoboticsFacility)
    if (enemies(Protoss.PhotonCannon) >= 2) {
      requireMiningBases(2)
      once(Protoss.Shuttle)
    }

    doArmy()
    get(Protoss.DragoonRange)
    get(Protoss.Observatory)
    once(Protoss.Observer)
    get(Protoss.RoboticsSupportBay)
    once(Protoss.Reaver)
    get(Protoss.ShuttleSpeed)
    pumpShuttleAndReavers()
    pump(Protoss.Dragoon)
    requireMiningBases(2)
    get(5, Protoss.Gateway)
    pumpGasPumps()
    pump(Protoss.Zealot)
  }
}
