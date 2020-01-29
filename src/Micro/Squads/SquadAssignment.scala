package Micro.Squads

import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable.ArrayBuffer

class SquadAssignment(val squad: Squad) {
  private val assignedUnits = new ArrayBuffer[FriendlyUnitInfo]

  def units: Seq[FriendlyUnitInfo] = assignedUnits

  def addUnit(unit: FriendlyUnitInfo): Unit = {
    assignedUnits += unit
    squad.goal.addCandidate(unit)
  }

  override def toString: String = "SquadAssignment: " + squad.client.toString + " (" + units.size + " units)"
}