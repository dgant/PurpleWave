package Micro.Agency

import Mathematics.Points.{Pixel, Tile}
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Destination(val unit: FriendlyUnitInfo, val level: Int) {
  var pixelRaw      : Option[Pixel] = None
  var pixelWaypoint : Option[Pixel] = None
  var pixelAdjusted : Option[Pixel] = None

  def apply(): Pixel = pixel.get
  def pixel: Option[Pixel] = pixelAdjusted
    .orElse(pixelWaypoint)
    .orElse(pixelRaw)
  def isDefined: Boolean = pixel.isDefined

  def clear(): Unit = {
    pixelRaw      = None
    pixelWaypoint = None
    pixelAdjusted = None
  }

  def set(value: Pixel): Destination = {
    if ( ! pixelRaw.contains(value)) { clear()    }
    pixelRaw = Some(value)
    this
  }
  def set(value: Option[Pixel]): Destination = {
    if (pixelRaw != value) { clear() }
    pixelRaw = value
    this
  }
  def set(value: Tile): Destination = {
    set(value.center)
  }
  def set(other: Destination): Destination = {
    pixelRaw      = other.pixelRaw
    pixelWaypoint = other.pixelWaypoint
    pixelAdjusted = other.pixelAdjusted
    this
  }

  def setWaypoint(value: Pixel): Destination = {
    pixelWaypoint = Some(value)
    this
  }
  def setWaypoint(value: Option[Pixel]): Destination = {
    pixelWaypoint = value
    this
  }

  def setAsWaypoint(value: Pixel): Destination = {
    set(value)
    setWaypoint(pixelRaw)
    this
  }
  def setAsWaypoint(value: Option[Pixel]): Destination = {
    set(value)
    pixelWaypoint = pixelRaw
    this
  }

  def pickWaypoint(): Destination = {
    pixelWaypoint = pixelWaypoint.orElse(pixelRaw.map(MicroPathing.getWaypointToPixel(unit, _)))
    this
  }

  def adjust(): Destination = {
    pixelAdjusted = pixelAdjusted.orElse(
      pixelWaypoint
        .orElse(pixelRaw)
        .map(Commander.adjustDestination(unit, _)))
    this
  }
}
