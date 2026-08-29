package Gameplans.Protoss.PvP

import Gameplans.All.GameplanImperative
import Lifecycle.With
import Mathematics.Maff
import Placement.Access.{Foundation, PlaceLabels, PlacementQuery}
import Placement.Access.PlaceLabels.{Proxy4Gate, ProxyForwardRobo}
import ProxyBwapi.Races.Protoss
import Strategery.Strategies.Protoss.{PvPCoreExpand, PvPNexusFirst}
import Utilities.?
import Utilities.Time.{GameTime, Seconds}

class PvPOpeningVsFE extends GameplanImperative {

  trait Response
  object NexusFirst extends Response
  object RoboProxy  extends Response
  object FourGateProxy extends Response
  object FourGateHome extends Response

  var response: Option[Response] = None

  def getProxyRoboPlace : Option[Foundation] = With.placement.get(PlaceLabels.ProxyForwardRobo).find(foundation => With.geography.mains.minBy(_.groundPixels(foundation.tile)) == With.geography.mains.minBy(_.groundPixels(With.scouting.enemyHome)))
  def getProxyGatePlace : Option[Foundation] = Maff.minBy(With.placement.get(PlaceLabels.Proxy4Gate))(_.tile.groundTiles(With.scouting.enemyHome))

  override def activated: Boolean = {
    if (PvPNexusFirst()) return false
    if (PvPCoreExpand()) return false

    lazy val proxyRoboPlace = getProxyRoboPlace
    lazy val proxyGatePlace = getProxyGatePlace
    lazy val canProxyRobo   = proxyRoboPlace.isDefined
    lazy val canProxyGate   = proxyGatePlace.isDefined

    if (response.isEmpty && (enemiesHaveComplete(Protoss.PhotonCannon) || With.scouting.enemyNatural.exists(_.townHallTile.lastSeen > 0))) {
      if (With.frame < GameTime(2, 0)() + Seconds(10)() * enemies(Protoss.PhotonCannon, Protoss.Forge)) {
        response = Some(NexusFirst)
      } else if (With.fingerprints.forgeFe()) {
        if (unitsComplete(Protoss.Gateway) > 1) {
          response = Some(?(canProxyGate, FourGateProxy, FourGateHome))
        } else if (unitsComplete(Protoss.Gateway) > 0) {
          response = Some(?(canProxyRobo && framesUntilUnit(Protoss.CyberneticsCore) > Seconds(30)(), RoboProxy, ?(canProxyGate, FourGateProxy, FourGateHome)))
        } else {
          response = Some(NexusFirst)
        }
      } else if (With.fingerprints.nexusFirst()) {
        if (canProxyGate) {
          response = Some(FourGateProxy)
        } else if (haveComplete(Protoss.Assimilator)) {
          response = Some(FourGateHome)
        } else {
          response = Some(NexusFirst)
        }
        if (haveComplete(Protoss.Gateway)) {
          response = Some(?(canProxyGate, FourGateProxy, ?(haveComplete(Protoss.Assimilator), FourGateHome, NexusFirst)))
        }
      }
    }
    if (enemyRecentStrategy(With.fingerprints.cannonRush) && ! With.scouting.enemyNatural.exists(_.townHall.isDefined)) {
      response = None
    }
    if (PvPNexusFirst()) {
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

      // Unlikely to be reached, but just in case:
      once(Protoss.Gateway)
      once(15, Protoss.Probe)
      once(2, Protoss.Pylon)
      once(16, Protoss.Probe)
      once(Protoss.Assimilator)
      once(17, Protoss.Probe)
      once(Protoss.CyberneticsCore)
      once(18, Protoss.Probe)

    } else {
      once(Protoss.Gateway)
      once(12, Protoss.Probe)
      once(Protoss.Assimilator)
      once(13, Protoss.Probe)
      once(Protoss.CyberneticsCore)
      once(14, Protoss.Probe)

      if (response.contains(RoboProxy)) {
        get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
          .preferZone(With.scouting.enemyNatural.map(_.zone).toSeq: _*)
          .preferBase(With.scouting.enemyNatural.toSeq: _*)
          .requireLabelYes(ProxyForwardRobo))

        once(17, Protoss.Probe)
        get(1, Protoss.RoboticsFacility, new PlacementQuery(Protoss.RoboticsFacility)
          .preferZone(With.scouting.enemyNatural.map(_.zone).toSeq: _*)
          .preferBase(With.scouting.enemyNatural.toSeq: _*)
          .requireLabelYes(ProxyForwardRobo))
        once(Protoss.Dragoon)

      } else if (response.contains(FourGateProxy)) {
        get(1, Protoss.Pylon, new PlacementQuery(Protoss.Pylon)
          .preferZone(With.scouting.enemyNatural.map(_.zone).toSeq: _*)
          .preferBase(With.scouting.enemyNatural.toSeq: _*)
          .requireLabelYes(Proxy4Gate))

        once(15, Protoss.Probe)
      }
    }
  }

  var attackTrigger: Boolean = false
  override def executeMain(): Unit = {
    attackTrigger ||= response.contains(RoboProxy) && haveEverComplete(Protoss.Reaver)
    attackTrigger ||= response.contains(FourGateProxy) && unitsComplete(Protoss.Dragoon) >= 5
    attackTrigger ||= response.contains(FourGateHome) && unitsComplete(Protoss.Dragoon) >= 5
    attack(attackTrigger)

    if (PvPIdeas.enemyDarkTemplarLikely && ! response.contains(RoboProxy)) {
      get(Protoss.Forge)
      buildInWall(1, Protoss.Pylon)
      buildInWall(2, Protoss.PhotonCannon)
      requireMiningBases(2)
    }

    if (armySupply200 >= 24) {
      requireMiningBases(2)
    }

    if (response.contains(RoboProxy)) {
      get(Protoss.RoboticsSupportBay)
      once(Protoss.Reaver)
      get(Protoss.Observatory)
      once(Protoss.Observer)
      if (enemyDarkTemplarLikely) {
        get(2, Protoss.Observer)
      }
      pump(Protoss.Reaver)
      get(Protoss.DragoonRange)
      pump(Protoss.Dragoon)
      get(3, Protoss.Gateway)
    } else {
      if (response.contains(FourGateProxy)) {
        get(Protoss.DragoonRange)
        get(4, Protoss.Gateway, new PlacementQuery(Protoss.Gateway).preferZone().preferBase().preferLabelYes(Proxy4Gate))
        pump(Protoss.Dragoon)
      } else {
        get(Protoss.DragoonRange)
        get(4, Protoss.Gateway)
        pump(Protoss.Dragoon)
      }
      buildInWall(1, Protoss.Pylon)
      buildInWall(1, Protoss.Forge)
    }

    pump(Protoss.Zealot)
    requireMiningBases(2)
  }

}
