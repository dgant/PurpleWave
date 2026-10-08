package Gameplans.Protoss.PvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPCoreExpand, PvPNexusFirst}
import Utilities.Time.{GameTime, Seconds}

class PvPOpeningVsFE extends GameplanImperative {

  trait Response
  object NexusFirst extends Response
  object RoboAllIn  extends Response

  var response: Option[Response] = None

  override def activated: Boolean = {
    if (PvPNexusFirst()) return false
    if (PvPCoreExpand()) return false
    if ( ! enemyStrategy(With.fingerprints.nexusFirst, With.fingerprints.forgeFe)) return false

    if (response.isEmpty && (enemiesHaveComplete(Protoss.PhotonCannon) || With.scouting.enemyNatural.exists(_.townHallTile.lastSeen > 0))) {
      if (With.frame < GameTime(2, 0)() + Seconds(10)() * enemies(Protoss.PhotonCannon, Protoss.Forge)) {
        response = Some(NexusFirst)
      } else if (With.fingerprints.forgeFe()) {
        if (unitsComplete(Protoss.Gateway) > 1) {
          response = Some(RoboAllIn)
        } else if (haveComplete(Protoss.Assimilator)) {
          response = Some(RoboAllIn)
        } else {
          response = Some(NexusFirst)
        }
      } else if (With.fingerprints.nexusFirst()) {
        response = Some(RoboAllIn)
      }
    }
    if (enemyRecentStrategy(With.fingerprints.cannonRush) && ! With.scouting.enemyNatural.exists(_.townHall.isDefined)) {
      response = None
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
    } else {
      once(Protoss.Gateway)
      once(12, Protoss.Probe)
      once(Protoss.Assimilator)
      once(13, Protoss.Probe)
      once(Protoss.CyberneticsCore)
      once(14, Protoss.Probe)
      get(2, Protoss.Pylon)
      once(16, Protoss.Probe)
      once(Protoss.RoboticsFacility)
    }
  }

  var attackTrigger: Boolean = false
  override def executeMain(): Unit = {
    attackTrigger ||= unitsEver(u => Protoss.Reaver(u) && u.complete) >= 2
    attack(attackTrigger)

    if (PvPIdeas.enemyDarkTemplarLikely) {
      get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.RoboticsFacility, Protoss.Observatory)
      once(Protoss.Observer)
    }

    if (armySupply200 >= 24) {
      requireMiningBases(2)
    }

    get(Protoss.Assimilator, Protoss.CyberneticsCore, Protoss.RoboticsFacility)
    once(Protoss.Shuttle)
    pumpShuttleAndReavers()
    get(Protoss.RoboticsSupportBay)
    get(Protoss.DragoonRange)
    pump(Protoss.Dragoon)
    get(3, Protoss.Gateway)
    requireMiningBases(2)
    get(5, Protoss.Gateway)
    pump(Protoss.Zealot)
  }

}
