package ProxyBwapi.UnitTracking

import Lifecycle.{PurpleEventListener, With}
import ProxyBwapi.UnitInfo.UnitInfo
import bwapi.{Position, UnitType}

/**
  * Determine whether a unit is a "ghost unit", eg https://github.com/bwapi/bwapi/issues/702
  */
object GhostUnit {
  def fromBwapi(unit: bwapi.Unit): Boolean = {
    (PurpleEventListener.gameCount > 1
      && unit.getType.id == UnitType.Unknown.id
      && unit.getInitialType.id == UnitType.Unknown.id
      && unit.getPosition == Position.Unknown
      && unit.getInitialPosition == Position.Unknown)
  }

  def apply(unit: UnitInfo): Boolean = {
    PurpleEventListener.gameCount > 1 && (
      fromBwapi(unit.bwapiUnit)
      || unit.frameDiscovered > With.frame
      || unit.lastSeen > With.frame)
  }
}
