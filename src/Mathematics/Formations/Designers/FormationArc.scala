package Mathematics.Formations.Designers

import Lifecycle.With
import Mathematics.Formations.Arcs.{Arc, ArcPlacementState}
import Mathematics.Formations.{FormationAssigned, FormationSlot, FormationUnassigned}
import Mathematics.Points.{Pixel, SpecificPoints}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable.ListBuffer

class FormationArc(
  val targetStart : Pixel,
  val targetEnd   : Pixel,
  val origin      : Pixel)
    extends FormationDesigner{
  
  def form(units: Seq[FriendlyUnitInfo]): FormationAssigned = {

    val formationSlots = units
      .map(_.unitClass)
      .toSet[UnitClass]
      .map(uc => (uc, new ListBuffer[Pixel]))
      .toMap

    static(units, targetStart, targetEnd, origin)
      .groupBy(_.unitClass)
      .foreach(pair =>  pair._2.map(_.pixelAfter).foreach(pixel => formationSlots(pair._1).append(pixel)))

    new FormationUnassigned(formationSlots).assign(units)
  }

  def static(
    units       : Iterable[UnitInfo],
    targetStart : Pixel,
    targetEnd   : Pixel,
    origin      : Pixel)
    : Seq[FormationSlot] = {

    val targetCenter  = targetStart.midpoint(targetEnd)
    val targetRadians = targetStart.radiansTo(targetEnd)
    val centerToArcRadians = List(
      targetRadians - Math.PI / 2.0,
      targetRadians + Math.PI / 2.0)
      .sortBy(radians => -targetCenter.radiateRadians(radians, 128.0).pixelDistance(SpecificPoints.middle))
      .minBy(radians => targetCenter.radiateRadians(radians, 128.0).zone != origin.zone)

    val arc = Arc(
      Math.PI, // Configurable.
      targetCenter,
      centerToArcRadians,
      targetStart.pixelDistance(targetEnd))

    val arcPlacement = new ArcPlacementState(arc)

    val walkers = units.filterNot(_.flying)
    val flyers  = units.filter(_.flying)
    val ranks   = walkers
      .map(new FormationSlot(_))
      .groupBy(_.idealPixels)
      .values
      .toList
      .sortBy(_.head.idealPixels)

    ranks.foreach(rank => {
      arcPlacement.startRank(rank.head.idealPixels)
      rank.foreach(participant => {
        participant.pixelAfter = arcPlacement.reserveSpace(With.configuration.concaveMarginPixels + 2.0 * participant.unitClass.radialHypotenuse)
      })
    })

    val walkerSlots = ranks.flatten
    val flyerSlots  = flyers.map(flyer => new FormationSlot(flyer))
    flyerSlots.foreach(slot => slot.pixelAfter = targetCenter.radiateRadians(centerToArcRadians, slot.idealPixels))

    val output = walkerSlots ++ flyerSlots
    output
  }
}
